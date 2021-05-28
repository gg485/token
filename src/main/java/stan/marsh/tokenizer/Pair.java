package stan.marsh.tokenizer;

public class Pair<K> {
    K key;
    double freq;

    public Pair(K key, double freq) {
        this.key = key;
        this.freq = freq;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public double getFreq() {
        return freq;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    @Override
    public String toString() {
        return "Candidate [key=" + key + ", freq=" + freq + "]";
    }
}
