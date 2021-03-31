import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.List;

public class TestAnsj {
    //测试分词

    public static void main(String[] args) {
        String str = "小鸭毕业于陕西科技大学计算机专业," +
                "后来去剑桥和哥伦比亚大学深造," +
                "擅长使用计算机控制机器炒菜";
        List<Term> terms = ToAnalysis.parse(str).getTerms();
        for (Term term : terms) {
            System.out.print(term.getName() + "/");
        }
    }
}


