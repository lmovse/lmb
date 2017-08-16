package info.lmovse.blog.service;

import info.lmovse.blog.pojo.po.User;

/**
 * Created by BlueT on 2017/3/3.
 */
public interface IUserService extends IService<User> {

    /**
     * 用戶登录
     * @param username
     * @param password
     * @return
     */
    User login(String username, String password);

}
