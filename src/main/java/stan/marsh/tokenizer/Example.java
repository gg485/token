package stan.marsh.tokenizer;

import java.util.List;

public class Example {
    public static void main(String[] args) {
        Dict dict=Dict.getInstance();
        dict.load();
        Tokenizer tokenizer=new Tokenizer(dict);
        List<String> tokens = tokenizer.tokenize("江泽民同志2000年2月25日在广东省考察工作时，从全面总结党的历史经验和如何适应新形势新任务的要求出发，首次对“三个代表”重要思想进行了比较全面的阐述。提出：总结中国共产党七十多年的历史；" +
                "可以得出一个重要的结论，这就是：中国共产党之所以赢得人民的拥护，是因为中国共产党在革命、建设、改革的各个历史时期，总是代表着中国先进生产力的发展要求,代表着中国先进文化的前进方向，代表着中国最广大人民的根本利益，并通过制定正确的路线方针政策，" +
                "为实现国家和人民的根本利益而不懈奋斗。人类又来到一个新的世纪之交和新的千年之交。在新的历史条件下，中国共产党如何更好地檄到这\"三个代表\"，是一个需要全党同志特别是党的高级干部深刻思考的重大课题。" +
                "可以说，“三个代表”的重要论述具有鲜明的时代特征，不仅是中国共产党的建设的重大课题，同时，它事关改革开放和两个文明建设的成败，事关中国共产党、中国工作大局，事关党和国家的前途命运，是中国共产党的立党之本、执政之基、力量之源。" +
                "中国共产党八十年的奋斗历程充分证明，中国共产党要继续站在时代前列，带领人民胜利前进，就必须始终代表中国先进生产力的发展要求，代表中国先进文化的前进方向，代表中国最广大人民的根本利益。");
        System.out.println(tokens);

        stan.marsh.tokenizer.Analyzer analyzer=new stan.marsh.tokenizer.Analyzer(tokenizer);
        System.out.println(analyzer.getTopNKeywords(tokens, 20));
    }
}
