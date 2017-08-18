package info.lmovse.blog.core.mapper;

import info.lmovse.blog.base.mapper.AppMapper;
import info.lmovse.blog.core.pojo.po.User;
import org.apache.ibatis.exceptions.TooManyResultsException;

public interface UserMapper extends AppMapper<User> {

    User findUserByName(String userName) throws TooManyResultsException;

}