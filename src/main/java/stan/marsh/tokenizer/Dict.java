package stan.marsh.tokenizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Dict {
    private static volatile Dict instance;
    private final DictNode root = new DictNode((char) 0);
    private final Map<String, Double> freqs = new HashMap<>();
    private final Set<InputStream> loadedPath = new HashSet<>();
    private double minFreq = Double.MAX_VALUE;
    private double total = 0.0;

    public static Dict getInstance() {
        if(instance==null){
            synchronized (Dict.class){
                if(instance==null){
                    instance=new Dict();
                }
            }
        }
        return instance;
    }

    public void load(){
        double start=System.currentTimeMillis();
        this.load(this.getClass().getResourceAsStream("/dict.txt"));
        System.out.println("字典加载时间(ms): "+(System.currentTimeMillis()-start));
    }

    public void load(InputStream is){
        if(loadedPath.contains(is))return;
        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(
                            is, StandardCharsets.UTF_8));
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");

                if (tokens.length < 2) continue;

                String word = tokens[0];
                double freq = Double.parseDouble(tokens[1]);
                total += freq;
                addWord(word);
                freqs.put(word, freq);
            }
            for (Map.Entry<String, Double> entry : freqs.entrySet()) {
                entry.setValue((Math.log(entry.getValue() / total)));
                minFreq = Math.min(entry.getValue(), minFreq);
            }
            loadedPath.add(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public DictNode getDict() {
        return root;
    }

    public void addWord(String word) {
        if (word != null && !"".equals(word.trim())) {
            root.addWord(word.toCharArray(), 0, word.length());
        }
    }

    public boolean containsWord(String word){
        return freqs.containsKey(word);
    }

    public double getFreq(String word) {
        return freqs.getOrDefault(word,minFreq);
    }

    static class DictNode {
        private static final int CHILD_ARRAY_SIZE = 10;
        public volatile DictNode[] childArray;
        public volatile Map<Character, DictNode> childMap;
        private final AtomicInteger arrayLen = new AtomicInteger();
        private final char thisCh;
        private boolean isEnd;

        public DictNode(char ch) {
            this.thisCh = ch;
        }

        public Hit match(char[]chars,int start,int len){
            return this.match(chars, start, len,null);
        }

        private Hit match(char[]chars,int start,int len,Hit hit){
            if(hit==null){
                hit=new Hit();
                hit.setBegin(start);
            }else{
                hit.setUnmatch();
            }
            hit.setEnd(start);

            DictNode childNode=getChild(chars[start]);
            if(childNode==null){
                return hit;
            }
            if (len > 1) {
                return childNode.match(chars, start + 1, len - 1, hit);
            }
            else if (len == 1) {
                if (childNode.isEnd) {
                    hit.setMatch();
                }
                if (childNode.hasNextNode()) {
                    hit.setPrefix();
                    hit.setMatchedDictSegment(childNode);
                }
            }
            return hit;
        }

        private boolean hasNextNode() {
            return arrayLen.get()>0;
        }

        private DictNode getChild(char ch){
            if(childArray!=null){
                for (DictNode node : childArray) {
                    if (node!=null&&node.thisCh == ch) {
                        return node;
                    }
                }
            }else if(childMap!=null){
                return childMap.get(ch);
            }
            return null;
        }

        private DictNode createOrGetChild(char ch) {
            DictNode childNode = null;
            for (; ; ) {
                if (arrayLen.get() < CHILD_ARRAY_SIZE) {
                    if (childArray == null) {
                        synchronized (this) {
                            if (childArray == null) {
                                childArray = new DictNode[CHILD_ARRAY_SIZE];
                            }
                        }
                    }
                    for (DictNode node : childArray) {
                        if (node!=null&&node.thisCh == ch) {
                            childNode = node;
                            break;
                        }
                    }
                    if (childNode == null) {
                        if (arrayLen.get() > CHILD_ARRAY_SIZE) {
                            continue;
                        }
                        synchronized (this) {
                            childNode = new DictNode(ch);
                            childArray[arrayLen.getAndIncrement()] = childNode;
                        }
                    }
                } else {
                    if (childMap == null) {
                        synchronized (this) {
                            if (childMap == null) {
                                childMap = new ConcurrentHashMap<>();
                            }
                        }
                    }
                    childNode=childMap.get(ch);
                    if(childNode==null){
                        synchronized (this){
                            childNode=new DictNode(ch);
                            childMap.put(ch,childNode);
                            if(childArray!=null) {
                                for (DictNode node : childArray) {
                                    childMap.put(node.thisCh, node);
                                }
                                childArray = null;
                            }
                        }
                    }
                }
                return childNode;
            }
        }

        private void addWord(char[] chars, int start, int len) {
            DictNode childNode = createOrGetChild(chars[start]);
            assert childNode != null;
            if (len > 1) {
                childNode.addWord(chars, start + 1, len - 1);
            } else if (len == 1) {
                childNode.isEnd = true;
            }
        }

        @Override
        public String toString() {
            return thisCh+"";
        }
    }
}
