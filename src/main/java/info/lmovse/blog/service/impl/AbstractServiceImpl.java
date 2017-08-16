package info.lmovse.blog.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.lmovse.blog.configurer.AppMapper;
import info.lmovse.blog.exception.ServiceException;
import info.lmovse.blog.service.IService;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by lmovse on 2017/7/29.
 * Tomorrow is a nice day.
 */
public abstract class AbstractServiceImpl<T> implements IService<T> {

    @Autowired
    protected AppMapper<T> mapper;

    @Override
    public T findById(String id) {
        return NumberUtils.isDigits(id) ? mapper.selectByPrimaryKey(Integer.valueOf(id)) : mapper.selectByPrimaryKey(id);
    }

    @Override
    public List<T> findByExample(Example example) {
        return mapper.selectByExample(example);
    }

    @Override
    public List<T> findAll() {
        return mapper.selectAll();
    }

    @Override
    public PageInfo<T> findPage(Integer pageSize, Integer pageNum) {
        if (pageSize == null || pageNum == null) {
            throw new ServiceException("参数不能为空！");
        }
        PageHelper.startPage(pageNum, pageSize);
        return (PageInfo<T>) mapper.selectAll();
    }

    @Override
    public PageInfo<T> findPageByExample(Example example, Integer pageNum, Integer pageSize) {
        if (pageSize == null || pageNum == null) {
            throw new ServiceException("参数不能为空！");
        }
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(example == null ? mapper.selectAll() : mapper.selectByExample(example));
    }

    @Override
    public void save(T t) {
        mapper.insertSelective(t);
    }

    @Override
    public void deleteById(String id) {
        if (id == null) {
            throw new ServiceException("必要参数不能为空！");
        }
        int rows = NumberUtils.isDigits(id) ? mapper.deleteByPrimaryKey(Integer.valueOf(id)) : mapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(T t) {
        mapper.updateByPrimaryKeySelective(t);
    }

    @Override
    public T findOneByProps(T t) throws TooManyResultsException {
        return mapper.selectOne(t);
    }

}
