package stan.marsh.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Analyzer {
    private static Map<String, Double> idfMap;
    private static double idfMedium;
    private static Set<String> stopWords;
    private final Tokenizer tokenizer;

    public Analyzer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    static {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void init() throws IOException {
        Map<String, Double> ret = new HashMap<>();
        PriorityQueue<Double> queue1 = new PriorityQueue<>(Comparator.reverseOrder());
        PriorityQueue<Double> queue2 = new PriorityQueue<>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(
                            Analyzer.class.getResourceAsStream("/idf_dict.txt"), StandardCharsets.UTF_8));
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");

                if (tokens.length < 2) continue;

                String word = tokens[0];
                double idf = Double.parseDouble(tokens[1]);
                ret.put(word, idf);

                if (queue1.isEmpty() || (!queue2.isEmpty() && queue2.peek() > idf)) {
                    queue1.offer(idf);
                    if (queue1.size() - queue2.size() > 1) {
                        queue2.offer(queue1.poll());
                    }
                } else {
                    queue2.offer(idf);
                    if (queue2.size() - queue1.size() > 0) {
                        queue1.offer(queue2.poll());
                    }
                }
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Double a = queue1.peek(), b = queue2.peek();
        if (queue1.size() != queue2.size()) {
            if (a == null) {
                throw new RuntimeException("queue.peek() is null");
            }
            idfMedium = a;
        } else {
            if (a == null || b == null) {
                throw new RuntimeException("queue.peek() is null");
            }
            idfMedium = (queue1.peek() + queue2.peek()) / 2;
        }
        idfMap = ret;

        stopWords = new HashSet<>();
        try {
            br = new BufferedReader(
                    new InputStreamReader(
                            Analyzer.class.getResourceAsStream("/stop_words.txt"), StandardCharsets.UTF_8));
            while (br.ready()) {
                String line = br.readLine();
                stopWords.add(line.trim());
            }
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, Double> getTF(List<String> tokens) {
        int wordCount = 0;
        Map<String, Integer> freqMap = new HashMap<>();
        Map<String, Double> ret = new HashMap<>();
        for (String token : tokens) {
            if (!stopWords.contains(token) && token.length() > 1) {
                wordCount++;
                freqMap.merge(token, 1, Integer::sum);
            }
        }
        for (String key : freqMap.keySet()) {
            ret.put(key, freqMap.get(key) / (double) wordCount);
        }
        return ret;
    }

    public List<Keyword> getTopNKeywords(List<String> tokens, int n) {
        Map<String, Double> tfMap = getTF(tokens);
        List<Keyword> ret = new ArrayList<>();
        for (String word : tfMap.keySet()) {
            Keyword keyword;
            if (idfMap.containsKey(word)) keyword = new Keyword(word, 0.1 * idfMap.get(word) * tfMap.get(word));
            else keyword = new Keyword(word, 0.1 * idfMedium * tfMap.get(word));
            ret.add(keyword);
        }
        Collections.sort(ret);
        return ret.size() > n ? ret.subList(0, n) : ret;
    }

    static class Keyword implements Comparable<Keyword> {
        String word;
        double tfidf;

        public Keyword(String word, double tfidf) {
            this.word = word;
            this.tfidf = tfidf;
        }

        @Override
        public String toString() {
            return "{" + word + ", " + tfidf + '}';
        }

        @Override
        public int compareTo(Keyword o) {
            return tfidf - o.tfidf > 0 ? -1 : 1;
        }
    }
}
