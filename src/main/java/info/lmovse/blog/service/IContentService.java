package info.lmovse.blog.service;

import com.github.pagehelper.PageInfo;
import info.lmovse.blog.pojo.po.Content;

/**
 * Created by Administrator on 2017/3/13 013.
 */
public interface IContentService extends IService<Content> {

    /**
     * 发布文章
     * @param contents
     */
    void publish(Content contents);

    /**
     * 查询分类/标签下的文章归档
     * @param page page
     * @param limit limit
     * @return Content
     */
    PageInfo<Content> getArticles(Integer mid, int page, int limit);

    /**
     * 搜索、分页
     * @param keyword keyword
     * @param page page
     * @param limit limit
     * @return Content
     */
    PageInfo<Content> getArticles(String keyword, Integer page, Integer limit);

}
