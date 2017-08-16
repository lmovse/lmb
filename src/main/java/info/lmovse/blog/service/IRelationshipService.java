package info.lmovse.blog.service;

import info.lmovse.blog.pojo.po.Relationship;

import java.util.List;

/**
 * Created by BlueT on 2017/3/18.
 */
public interface IRelationshipService extends IService<Relationship> {
    /**
     * 按住键删除
     * @param cid
     * @param mid
     */
    void deleteById(Integer cid, Integer mid);

    /**
     * 按主键统计条数
     * @param cid
     * @param mid
     * @return 条数
     */
    int countById(Integer cid, Integer mid);

    /**
     * 根据id搜索
     * @param cid
     * @param mid
     * @return
     */
    List<Relationship> getRelationshipById(Integer cid, Integer mid);

}
