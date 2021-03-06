# 一个简单的分词器
用法：  
```java
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
```
在我的破机器上（CPU i9-9900），为这段500字的文字分词花费了约5ms，效果如下  
```text
[江泽民, 同志, 2000, 年, 2, 月, 25, 日, 在, 广东省, 考察, 工作, 时, ，, 从, 全面, 总结, 党, 的, 历史, 经验, 和, 如何, 适应, 新, 形势, 新, 任务, 的, 要求, 出发, ，, 首次, 对, “, 三个代表, ”, 重要, 思想, 进行, 了, 比较, 全面, 的, 阐述, 。, 提出, ：, 总结, 中国共产党, 七十多年, 的, 历史, ；, 可以, 得出, 一个, 重要, 的, 结论, ，, 这, 就是, ：, 中国共产党, 之所以, 赢得, 人民, 的, 拥护, ，, 是因为, 中国共产党, 在, 革命, 、, 建设, 、, 改革, 的, 各个, 历史, 时期, ，, 总是, 代表, 着, 中国, 先进, 生产力, 的, 发展, 要求, ,, 代表, 着, 中国, 先进, 文化, 的, 前进方向, ，, 代表, 着, 中国, 最, 广大, 人民, 的, 根本利益, ，, 并, 通过, 制定, 正确, 的, 路线方针, 政策, ，, 为, 实现, 国家, 和, 人民, 的, 根本利益, 而, 不懈, 奋斗, 。, 人类, 又, 来到, 一个, 新, 的, 世纪之交, 和, 新, 的, 千年, 之交, 。, 在, 新, 的, 历史, 条件, 下, ，, 中国共产党, 如何, 更好, 地, 檄, 到, 这, ", 三个代表, "，, 是, 一个, 需要, 全党同志, 特别, 是, 党, 的, 高级干部, 深刻, 思考, 的, 重大, 课题, 。, 可以, 说, ，“, 三个代表, ”, 的, 重要, 论述, 具有, 鲜明, 的, 时代特征, ，, 不仅, 是, 中国共产党, 的, 建设, 的, 重大, 课题, ，, 同时, ，, 它, 事关, 改革开放, 和, 两个, 文明, 建设, 的, 成败, ，, 事关, 中国共产党, 、, 中国, 工作, 大局, ，, 事关, 党和国家, 的, 前途, 命运, ，, 是, 中国共产党, 的, 立党之本, 、, 执政之基, 、, 力量之源, 。, 中国共产党, 八十年, 的, 奋斗, 历程, 充分证明, ，, 中国共产党, 要, 继续, 站, 在, 时代, 前列, ，, 带领, 人民, 胜利, 前进, ，, 就, 必须, 始终, 代表, 中国, 先进, 生产力, 的, 发展, 要求, ，, 代表, 中国, 先进, 文化, 的, 前进方向, ，, 代表, 中国, 最, 广大, 人民, 的, 根本利益, 。]

[{中国共产党, 0.03939134329376471}, {代表, 0.019450143467411765}, {三个代表, 0.01943469334394118}, {根本利益, 0.018925847862607845}, {事关, 0.01730490948380392}, {人民, 0.017024062419379087}, {先进, 0.01637249518350327}, {前进方向, 0.013963139643267973}, {中国, 0.013850486801712417}, {历史, 0.01203462110627451}, {奋斗, 0.01076825774096732}, {课题, 0.010181974264666669}, {建设, 0.009632316335254904}, {生产力, 0.009321615261594772}, {立党之本, 0.009085410230065361}, {执政之基, 0.009085410230065361}, {路线方针, 0.009085410230065361}, {要求, 0.009032446577000001}, {总结, 0.008838968172588236}, {全党同志, 0.008367362982549021}]
```
第一行是分词结果，看着还行  
第二行是关键词提取，按权重排序
