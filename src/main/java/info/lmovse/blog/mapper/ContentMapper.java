package info.lmovse.blog.mapper;

import info.lmovse.blog.configurer.AppMapper;
import info.lmovse.blog.pojo.bo.Archive;
import info.lmovse.blog.pojo.po.Content;

import java.util.List;

public interface ContentMapper extends AppMapper<Content> {

    /**
     * 通过分类 ID 查找对应类别的文章
     * @param mid
     * @return 0 个或多个 content
     */
    List<Content> selectByCategory(Integer mid);

    /**
     * 归档查找
     * @return 0 个或多个 archive
     */
    List<Archive> findArchives();

    /**
     * 获取 ID 集合
     * @return
     */
    List<Integer> selectIds();

}