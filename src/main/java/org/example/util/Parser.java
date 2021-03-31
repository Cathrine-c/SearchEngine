package org.example.util;


import org.example.model.DocInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 第一步：构建正排索引
 * 从本地api目录，遍历静态html文件
 * 每一个html文件需要构建正文索引
 * 正文索引信息[DocInfo]（id，title,content,url)
 */
public class Parser {

    //api目录
    public static final String API_PATH = "D:\\java_chongci\\jdk-8u261-docs-all\\docs\\api";
   //构建本地文件正排索引
    public static final String RAW_DATA = "D:/raw_data.txt";
   //官方api文档的根路径
    public static final String API_BASE_PATH= "https://docs.oracle.com/javase/8/docs/api";


    public static void main(String[] args) throws IOException {
        //找到api本地路径下所有的html文件
        List<File> htmls = listHtml(new File(API_PATH));
       // List<DocInfo> docs = new ArrayList<>();

        //打印输出流
        FileWriter fw = new FileWriter(RAW_DATA);
     //   BufferedWriter bw = new BufferedWriter(fw);或者下面这个更好
        PrintWriter pw = new PrintWriter(fw,true);//自动刷新缓冲区

        for (File html:htmls){
            //测试获取html文件
           //  System.out.println(html.getAbsoluteFile());

            //一个html解析成DocInfo有的属性，
            DocInfo doc = parseHtml(html);
         //   docs.add(doc);
            //验证doc是否正确(重写toString)
            System.out.println(doc);

            //保存到(输出流输出到)目标文件
            //格式：一行为一个doc，title+\3+url+\3+content
            pw.println(doc.getTitle()+"\3"+doc.getUrl()+"\3"+doc.getContent());
            String uri = html.getAbsolutePath().substring(API_PATH.length());
            System.out.println("Parse:"+uri);
        }
    }


    //html解析为DocInfo属性
    private static DocInfo parseHtml(File html) {
        DocInfo doc = new DocInfo();
        //doc.setTitle(html.getName().substring(0,html.getName().length()-".html".length()));
        //或者这样写
        doc.setTitle(html.getName().substring(0,html.getName().length()-5));
       //获取相对路径:/java/lang/String.html
        String uri = html.getAbsolutePath().substring(API_PATH.length());
        doc.setUrl(API_BASE_PATH+uri);

        //获取正文
        doc.setContent(parseContent(html));

        return doc;
    }


    /**
     * 解析html文件内容：
     * <标签>内容</标签>
     * 只取内容:有多个标签就拼接
     */


    private static String parseContent(File html) {
        StringBuilder sb = new StringBuilder();
        try {
            FileReader fr = new FileReader(html);
            int i;
            boolean isContent = false;
            //一个字符一个字符读取
            while ((i = fr.read()) != -1) {
                char c = (char) i;
                //根据读取的字符c是否是'<','>',isContent决定是否拼接字符、continue
                if (isContent) {
                    if (c == '<') {
                        isContent=false;
                        continue;
                    }else if (c=='\n'||c=='\r'){
                        sb.append(" ");
                    }else {
                        sb.append(c);
                    }

                }else if (c=='>'){//当前不是正文，并且读取到'>',之后就是正文

                    isContent=true;
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }


    //递归遍历html文件
    private static List<File> listHtml(File dir){
       List<File> list = new ArrayList<>();

        //获取子文件和子文件夹
        File[] chilren = dir.listFiles();

        for (File chil:chilren){
            if (chil.isDirectory()) {//子文件夹:递归调用获取子文件夹内的html文件
                list.addAll(listHtml(chil));
            }else if (chil.getName().endsWith(".html")){
                list.add(chil);
            }
        }
        return list;
    }

}
