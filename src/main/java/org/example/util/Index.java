package org.example.util;


import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.example.model.DocInfo;
import org.example.model.Weight;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 构建索引:
 *正排索引:从本地文件数据中读取到java内存
 * 倒排索引：构建Map<String ,list<信息>>（类似数据库hash索引）
 * map键：关键词（分词来做)
 * map值-信息:
 * (1)docInfo对象引用或是docinfo的id
 *
 * (2)权重（标题对应关键词数量*10+正文对应关键词数量*1
 *
 * (3)关键词（是否需要，待定）
 */

public class Index {

    //正排索引
    private static final List<DocInfo> FORWARD_INDEX = new ArrayList<>();

    //倒排索引
    private static final Map<String, List<Weight>> INVERTED_INDEX = new HashMap<>();


    /**
     * 构建正排索引内容：从本地raw_data.txt读取并保存
     */
    public static void buildForwardIndex() {

        try {
            FileReader fr = new FileReader(Parser.RAW_DATA);

            BufferedReader br = new BufferedReader(fr);
            int id = 0;//行号设置为docInfo的id
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals("")) continue;
                //一行对应一个DocInfo对象，类似数据库一行数据对应java对象
                DocInfo doc = new DocInfo();
                doc.setId(++id);
                //每一行按\3间隔符切割
                String[] parts = line.split("\3");
                doc.setTitle(parts[0]);
                doc.setUrl(parts[1]);
                doc.setContent(parts[2]);


                FORWARD_INDEX.add(doc);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);//让线程不捕获异常，从而结束程序
        }
    }


    /**
     * 构建倒排索引：依赖正排索引构建
     * 从java内存中正排索引获取文档信息来构建
     */

    public static void buildInvertedIndex() {
        for (DocInfo doc : FORWARD_INDEX) {
            //一个doc，分别对标题和正文分词，每一个分词生产一个weight对象，需要计算权重
            //如标题为：陕西/科技/大学/计算机/专业/使用/计算机/控制/机器/炒菜
            //第一次出现的关键词，要new Weight对象，之后出现要获取之前的相同的关键词对象，更新权重
            //doc+分词对应weight，doc和分词一对多，
            //先构造一个hashmap，保存分词（键）和weight对象（值），

            Map<String, Weight> cache = new HashMap<>();
            List<Term> titlesfen = ToAnalysis.parse(doc.getTitle()).getTerms();

            for (Term titlefen : titlesfen) {
                Weight w = cache.get(titlefen.getName());//获取标题分词到键对应的weight对象
                if (w == null) {//如果没有就创建一个放到map中
                    w = new Weight();
                    w.setDoc(doc);
                    w.setKeyword(titlefen.getName());
                    cache.put(titlefen.getName(), w);
                }

                //标题分词，权重+10
                w.setWeight(w.getWeight() + 10);
            }

            List<Term> contentFencis = ToAnalysis.parse(doc.getContent()).getTerms();
            for (Term contemnfenci : contentFencis) {
                Weight w = cache.get(contemnfenci.getName());
                if (w == null) {
                    w = new Weight();
                    w.setDoc(doc);
                    w.setKeyword(contemnfenci.getName());
                    cache.put(contemnfenci.getName(), w);

                }
                //正文分词，权重+2
                w.setWeight(w.getWeight() + 1);
            }

            //把临时保存的map数据（keyword-weight）全部保存到倒排索引中

            for (Map.Entry<String, Weight> e : cache.entrySet()) {
                String keyword = e.getKey();
                Weight w = e.getValue();
                //更新保存到倒排索引Map<String,List<Weight>>-->多个文档，同一个关键词，保存在一个list里
                List<Weight> weights = INVERTED_INDEX.get(keyword);
                if (weights == null) {//如果拿不到，就创建一个，并存放到倒排索引
                    weights = new ArrayList<>();
                    INVERTED_INDEX.put(keyword, weights);
                }

               // System.out.println(keyword+": （"+w.getDoc().getId()+"，");
                weights.add(w);//倒排中，添加当前文档，每个分词对应的weight对象
            }
        }
    }


    //通过分词映射到相应文档
    public static List<Weight> get(String keyword){
        return INVERTED_INDEX.get(keyword);
    }


    public static void main(String[] args) {
        //测试正排索引构建是否成功
//        Index.buildForwardIndex();
//        FORWARD_INDEX.stream()
//                .forEach(System.out::println);
//

        Index.buildForwardIndex();

        //测试倒排索引是否构建成功
        Index.buildInvertedIndex();
        for (Map.Entry<String, List<Weight>> e : INVERTED_INDEX.entrySet()) {
            String keyword = e.getKey();
            System.out.println(keyword + ";");
            List<Weight> weights = e.getValue();
            //不必打印所有内容，正排索引已经测验过了，

            weights.stream().map(w -> {//把list中的每一个对应转换为其他对象
                return "（" + w.getDoc().getId() + "," + w.getWeight() + "）";

            })//转换完，会变为list<String>
                    .collect(Collectors.toList())//返回list<String>
                    .forEach(System.out::print);
            System.out.println();

        }
    }
}

