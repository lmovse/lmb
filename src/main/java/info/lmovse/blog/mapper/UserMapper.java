package info.lmovse.blog.mapper;

import info.lmovse.blog.configurer.AppMapper;
import info.lmovse.blog.pojo.po.User;
import org.apache.ibatis.exceptions.TooManyResultsException;

public interface UserMapper extends AppMapper<User> {

    User findUserByName(String userName) throws TooManyResultsException;

}