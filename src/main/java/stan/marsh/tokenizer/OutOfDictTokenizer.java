package stan.marsh.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.Collections;
import java.util.regex.Pattern;

public class OutOfDictTokenizer {
    private static OutOfDictTokenizer singleInstance;
    private static final String PROB_EMIT = "/prob_emit.txt";
    private static final char[] states = new char[]{'B', 'M', 'E', 'S'};
    private static Map<Character, Map<Character, Double>> emit;
    private static Map<Character, Double> start;
    private static Map<Character, Map<Character, Double>> trans;
    private static Map<Character, char[]> prevStatus;
    private static final Pattern SKIP = Pattern.compile("(\\d+\\.\\d+|[a-zA-Z0-9]+)");

    private OutOfDictTokenizer() {
    }

    public synchronized static OutOfDictTokenizer getInstance() {
        if (null == singleInstance) {
            singleInstance = new OutOfDictTokenizer();
            singleInstance.loadModel();
        }
        return singleInstance;
    }


    private void loadModel() {
        prevStatus = new HashMap<>();
        prevStatus.put('B', new char[]{'E', 'S'});
        prevStatus.put('M', new char[]{'M', 'B'});
        prevStatus.put('S', new char[]{'S', 'E'});
        prevStatus.put('E', new char[]{'B', 'M'});

        start = new HashMap<>();
        start.put('B', -0.26268660809250016);
        start.put('E', -3.14e+100);
        start.put('M', -3.14e+100);
        start.put('S', -1.4652633398537678);

        trans = new HashMap<>();
        Map<Character, Double> transB = new HashMap<>();
        transB.put('E', -0.510825623765990);
        transB.put('M', -0.916290731874155);
        trans.put('B', transB);
        Map<Character, Double> transE = new HashMap<>();
        transE.put('B', -0.5897149736854513);
        transE.put('S', -0.8085250474669937);
        trans.put('E', transE);
        Map<Character, Double> transM = new HashMap<>();
        transM.put('E', -0.33344856811948514);
        transM.put('M', -1.2603623820268226);
        trans.put('M', transM);
        Map<Character, Double> transS = new HashMap<>();
        transS.put('B', -0.7211965654669841);
        transS.put('S', -0.6658631448798212);
        trans.put('S', transS);

        InputStream is = this.getClass().getResourceAsStream(PROB_EMIT);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            emit = new HashMap<>();
            Map<Character, Double> values = null;
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("\t");
                if (tokens.length == 1) {
                    values = new HashMap<>();
                    emit.put(tokens[0].charAt(0), values);
                } else {
                    assert values != null;
                    values.put(tokens[0].charAt(0), Double.valueOf(tokens[1]));
                }
            }
        } catch (IOException e) {
            System.err.println(String.format(Locale.getDefault(), "%s: load model failure!", PROB_EMIT));
        } finally {
            try {
                if (null != is)
                    is.close();
            } catch (IOException e) {
                System.err.println(String.format(Locale.getDefault(), "%s: close failure!", PROB_EMIT));
            }
        }
    }

    public void tokenize(String sentence, List<String> tokens) {
        int n = sentence.length(), state = -1, x = 0;
        for (int i = 0; i < n; i++) {
            char ch = sentence.charAt(i);
            if (isChineseLetter(ch)) {
                if (state == 1) {
                    processOtherUnknownWords(sentence.substring(x, i), tokens);
                    x = i;
                }
                state = 0;
            } else {
                if (state == 0) {
                    viterbi(sentence.substring(x, i), tokens);
                    x = i;
                }
                state = 1;
            }
        }
        if (x < n) {
            if (state == 0) viterbi(sentence.substring(x, n), tokens);
            else processOtherUnknownWords(sentence.substring(x, n), tokens);
        }
    }

    public void viterbi(String sentence, List<String> tokens) {
        Vector<Map<Character, Double>> v = new Vector<>();
        Map<Character, Node> path = new HashMap<>();

        Map<Character,Double>initStates=new HashMap<>();
        double MIN_FLOAT = -3.14e100;
        for (char state : states) {
            Double emP = emit.get(state).getOrDefault(sentence.charAt(0), MIN_FLOAT);
            initStates.put(state, start.get(state) + emP);
            path.put(state, new Node(state, null));
        }
        v.add(initStates);

        for (int i = 1; i < sentence.length(); ++i) {
            Map<Character, Double> vv = new HashMap<>();
            v.add(vv);
            Map<Character, Node> newPath = new HashMap<>();
            for (char curState : states) {
                double emp = emit.get(curState).getOrDefault(sentence.charAt(i), MIN_FLOAT);
                Pair<Character> candidate = new Pair<>(null, MIN_FLOAT);
                for (char preState : prevStatus.get(curState)) {
                    double tranp = trans.get(preState).getOrDefault(curState, MIN_FLOAT);
                    tranp += (emp + v.get(i - 1).get(preState));
                    if (candidate.freq <= tranp) {
                        candidate.freq = tranp;
                        candidate.key = preState;
                    }
                }
                vv.put(curState, candidate.freq);
                newPath.put(curState, new Node(curState, path.get(candidate.key)));
            }
            path = newPath;
        }

        double probE = v.get(sentence.length() - 1).get('E');
        double probS = v.get(sentence.length() - 1).get('S');
        Vector<Character> posList = new Vector<>(sentence.length());
        Node win = probE < probS ? path.get('S') : path.get('E');
        while (win != null) {
            posList.add(win.value);
            win = win.parent;
        }
        Collections.reverse(posList);

        int begin = 0, next = 0;
        for (int i = 0; i < sentence.length(); ++i) {
            char pos = posList.get(i);
            if (pos == 'B') begin = i;
            else if (pos == 'E') {
                tokens.add(sentence.substring(begin, i + 1));
                next = i + 1;
            } else if (pos == 'S') {
                tokens.add(sentence.substring(i, i + 1));
                next = i + 1;
            }
        }
        if (next < sentence.length()) tokens.add(sentence.substring(next));
    }


    private void processOtherUnknownWords(String other, List<String> tokens) {
        Matcher mat = SKIP.matcher(other);
        int offset = 0;
        while (mat.find()) {
            if (mat.start() > offset) {
                tokens.add(other.substring(offset, mat.start()));
            }
            tokens.add(mat.group());
            offset = mat.end();
        }
        if (offset < other.length())
            tokens.add(other.substring(offset));
    }

    private static boolean isChineseLetter(char ch) {
        return ch >= 0x4E00 && ch <= 0x9FA5;
    }

    static class Node {
        public Character value;
        public Node parent;

        public Node(Character value, Node parent) {
            this.value = value;
            this.parent = parent;
        }
    }
}
