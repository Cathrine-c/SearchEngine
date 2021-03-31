package org.example.model;

public class Result {

    /**
     * 构造返回的对象
     */

    private Integer id;
    private int weight;//权重
    //前两个排序用的字段，后三个前端用

    private String title;//DocInfo的标题
    private String url;//DocInfo的Url
    private String desc;//content


    @Override
    public String toString() {
        return "Result{" +
                "id=" + id +
                ", weight=" + weight +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
