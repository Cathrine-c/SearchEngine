package org.example.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.example.model.Result;
import org.example.model.Weight;
import org.example.util.Index;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


//根据前端请求路径，定义后端服务路径，
//loadOnStartup属性表示是否在启动时初始化
@WebServlet(value = "/search",loadOnStartup = 0)
public class SearchServlet extends HttpServlet {


    @Override
    public void init(ServletConfig config) throws ServletException {
        Index.buildForwardIndex();
        Index.buildInvertedIndex();
        System.out.println("init compelete");

    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");//ajax请求，响应json格式
        Map<String,Object> map = new HashMap<>();


        //解析请求数据
        String query = req.getParameter("query");//搜索框内容
        List<Result> results = new ArrayList<>();


        try{
            //根据搜索内容处理搜索业务
            //校验请求内容，搜索内容
            if (query==null||query.trim().length()==0){
                map.put("ok",false);
                map.put("msg","搜索内容为空");

            }else {
                //1，根据搜索内容，进行分词，遍历每个分词
                for (Term t: ToAnalysis.parse(query).getTerms()){
                    String fenci = t.getName();//搜索分词
                    //如果分词是无意义分词就不执行，跳过
                    //TODO 定义一个数组包含没有意义的关键词，如果当前分词在数组里有就跳过（continue）
                    //2.每个分词，在倒排中查找对应的文档，（一个分词对应多个文档）
                    List<Weight> weights = Index.get(fenci);
                    //3.一个文档转换为一个Result，（一个分词），不同分词可能存在相同文档需要合并
                    for (Weight w:weights){
                        //转换weight为result
                        Result r = new Result();
                        r.setId(w.getDoc().getId());
                        r.setTitle(w.getDoc().getTitle());
                        r.setWeight(w.getWeight());
                        r.setUrl(w.getDoc().getUrl());
                        //文档内容超过60个字符长度,超过部分就隐藏为....
                        String content = w.getDoc().getContent();
                        r.setDesc(content.length()<=60?content:content.substring(0,60)+"...");
                        //TODO 做合并：从已有的，判断docId是否相同，权重相加
                        results.add(r);
                    }

                }

                //4.合并完成后，对List<result>进行排序，权重降序排
                results.sort(new Comparator<Result>() {
                    @Override
                    public int compare(Result o1, Result o2) {
                       // return Integer.compare(o1.getWeight(),o2.getWeight());//权重升序
                        return Integer.compare(o2.getWeight(),o1.getWeight());//权重降序

                    }
                });

                //lambda表达式
//                results.sort((o1,o2)->{
//                    return Integer.compare(o2.getWeight(),o1.getWeight());
//                });

                map.put("ok",true);
                map.put("data",results);

            }

        }catch (Exception e){
            e.printStackTrace();
            map.put("ok",false);
            map.put("msg","未知错误");
        }


        PrintWriter pw = resp.getWriter();//获取输出流
        //设置响应体内容
        pw.println(new ObjectMapper().writeValueAsString(map));

    }
}
