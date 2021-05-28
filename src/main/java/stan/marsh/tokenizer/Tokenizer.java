package stan.marsh.tokenizer;

import java.util.*;

public class Tokenizer {
    private final Dict wordDict;
    private final static OutOfDictTokenizer outOfDictTokenizer=OutOfDictTokenizer.getInstance();

    public Tokenizer(Dict wordDict) {
        this.wordDict = wordDict;
    }

    private Map<Integer, List<Integer>> createDAG(String sentence) {
        Map<Integer, List<Integer>> dag = new HashMap<>();
        Dict.DictNode dict = wordDict.getDict();
        char[] chars = sentence.toCharArray();
        int n = chars.length;
        int i = 0, j = 0;
        while (i < n) {
            Hit hit = dict.match(chars, i, j - i + 1);
            if (hit.isPrefix() || hit.isMatch()) {
                if (hit.isMatch()) {
                    if (!dag.containsKey(i)) {
                        List<Integer> value = new ArrayList<>();
                        dag.put(i, value);
                        value.add(j);
                    }
                    else dag.get(i).add(j);
                }
                if (++j>=n) {
                    j=++i;
                }
            }else{
                j=++i;
            }
        }
        for (i = 0; i < n; i++) {
            dag.putIfAbsent(i, Collections.singletonList(i));
        }
        return dag;
    }

    private Map<Integer, Pair<Integer>> calc(String sentence, Map<Integer, List<Integer>> dag){
        Map<Integer,Pair<Integer>> route=new HashMap<>();//K:节点编号   V:A:下一跳节点编号 B:freq(对数)
        int n = sentence.length();
        route.put(n,new Pair<>(0,0.));
        int i;
        for(i=n-1;i>=0;i--){
            Pair<Integer>tmp=new Pair<>(0,-Double.MAX_VALUE);
            for (Integer i2 : dag.get(i)) {
                double newFreq=wordDict.getFreq(sentence.substring(i,i2+1))+route.get(i2+1).getFreq();
                if(newFreq>tmp.getFreq()){
                    tmp.setFreq(newFreq);
                    tmp.setKey(i2);
                }
            }
            route.put(i,tmp);
        }
        return route;
    }

    private void addToken(String token,List<String> tokens){
        if(token.length()==1||wordDict.containsWord(token)){
            tokens.add(token);
        }else{
            outOfDictTokenizer.tokenize(token,tokens);
        }
    }

    public List<String> tokenize(String sentence){
        long start=System.nanoTime();
        List<String> tokens=new ArrayList<>();
        int n=sentence.length();
        Map<Integer, List<Integer>> dag = createDAG(sentence);
        Map<Integer, Pair<Integer>> route = calc(sentence, dag);

        StringBuilder builder=new StringBuilder();
        String buf;
        int x=0,y;
        while(x<n){
            y=route.get(x).getKey();
            String curWord = sentence.substring(x, y+1);
            if(x==y){
                builder.append(curWord);
            }else{
                buf=builder.toString();
                builder.delete(0,builder.length());
                addToken(buf,tokens);
                tokens.add(curWord);
            }
            x=y+1;
        }
        buf=builder.toString();
        addToken(buf,tokens);
        System.out.println("分词时间(us): "+(System.nanoTime()-start)/1000);
        return tokens;
    }
}
