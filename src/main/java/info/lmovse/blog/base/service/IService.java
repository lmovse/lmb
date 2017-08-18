package info.lmovse.blog.base.service;

import com.github.pagehelper.PageInfo;
import org.apache.ibatis.exceptions.TooManyResultsException;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by lmovse on 2017/7/29.
 * Tomorrow is a nice day.
 */
public interface IService<T> {

    /**
     * 通过 ID 查找实体记录
     * @param id
     * @return null 或者对应主键的实体记录
     */
    T findById(String id);

    /**
     * 通过对象属性查找对象
     * @param t
     * @return
     * @throws TooManyResultsException
     */
    T findOneByProps(T t) throws TooManyResultsException;

    /**
     * 通过实体查询对象查询实体记录
     * @param example 实体查询对象
     * @return 0 或多个符合查询条件的实体记录
     */
    List<T> findByExample(Example example);

    /**
     * 查询所有实体记录
     * @return 0 个或多个实体对象
     */
    List<T> findAll();

    /**
     * 无条件分页查询
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页详情
     */
    PageInfo<T> findPage(Integer pageNum, Integer pageSize);

    /**
     * 有条件的分页查询
     * @param example 查询实体对象
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页详情
     */
    PageInfo<T> findPageByExample(Example example, Integer pageNum, Integer pageSize);

    /**
     * 保存
     * @param t
     */
    void save(T t);

    /**
     * 通过 ID 删除
     * @param id
     */
    void deleteById(String id);

    /**
     * 更新
     * @param t
     */
    void update(T t);

}
