package info.lmovse.blog.service;

import info.lmovse.blog.pojo.bo.Statistics;
import info.lmovse.blog.pojo.dto.MetaDto;
import info.lmovse.blog.pojo.bo.Archive;
import info.lmovse.blog.pojo.bo.BackResponse;
import info.lmovse.blog.pojo.po.Comment;
import info.lmovse.blog.pojo.po.Content;

import java.util.List;

/**
 * 站点服务
 *
 * Created by 13 on 2017/2/23.
 */
public interface ISiteService {


    /**
     * 最新收到的评论
     *
     * @param limit
     * @return
     */
    List<Comment> recentComments(int limit);

    /**
     * 最新发表的文章
     *
     * @param limit
     * @return
     */
    List<Content> recentContents(int limit);

    /**
     * 查询一条评论
     * @param coid
     * @return
     */
    Comment getComment(Integer coid);

    /**
     * 查询一篇文章
     * @param cid
     * @return
     */
    int getArticleIndex(Integer cid, String direct);

    /**
     * 系统备份
     * @param bk_type
     * @param bk_path
     * @param fmt
     * @return
     */
    BackResponse backup(String bk_type, String bk_path, String fmt) throws Exception;


    /**
     * 获取后台统计数据
     *
     * @return
     */
    Statistics getStatistics();

    /**
     * 查询文章归档
     *
     * @return
     */
    List<Archive> getArchives();

    /**
     * 获取分类/标签列表
     * @return
     */
    List<MetaDto> metas(String type, String orderBy, int limit);

}
