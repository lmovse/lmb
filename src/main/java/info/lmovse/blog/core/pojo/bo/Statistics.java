package info.lmovse.blog.core.pojo.bo;

import java.io.Serializable;

/**
 * 后台统计对象
 */
public class Statistics implements Serializable {

    private Integer articles;
    private Integer comments;
    private Integer links;
    private Integer attachs;

    public Integer getArticles() {
        return articles;
    }

    public void setArticles(Integer articles) {
        this.articles = articles;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public Integer getLinks() {
        return links;
    }

    public void setLinks(Integer links) {
        this.links = links;
    }

    public Integer getAttachs() {
        return attachs;
    }

    public void setAttachs(Integer attachs) {
        this.attachs = attachs;
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "articles=" + articles +
                ", comments=" + comments +
                ", links=" + links +
                ", attachs=" + attachs +
                '}';
    }
}
