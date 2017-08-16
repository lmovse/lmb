package info.lmovse.blog.service.impl;

import info.lmovse.blog.exception.ServiceException;
import info.lmovse.blog.pojo.po.User;
import info.lmovse.blog.service.IUserService;
import info.lmovse.blog.util.TaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Create By lmovse on 2017/08/13
 */
@Service
public class UserServiceImpl extends AbstractServiceImpl<User> implements IUserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public void save(User user) {
        if (StringUtils.isBlank(user.getUsername()) && StringUtils.isBlank(user.getEmail())) {
            return;
        }
        String encodePwd = TaleUtils.MD5encode(user.getUsername() + user.getPassword());
        user.setPassword(encodePwd);
        super.save(user);
    }

    @Override
    public User findById(String uid) {
        User user = null;
        if (uid != null) {
            user = super.findById(uid);
        }
        return user;
    }

    @Override
    public User login(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new ServiceException("用户名和密码不能为空");
        }
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        List<User> users = super.findByExample(example);
        if (users.isEmpty()) {
            throw new ServiceException("不存在该用户");
        }
        String pwd = TaleUtils.MD5encode(username + password);
        assert pwd != null;
        if (!pwd.equals(users.get(0).getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        return users.get(0);
    }

    @Override
    public void update(User user) {
        if (null == user || null == user.getUid()) {
            throw new ServiceException("不存在该用户");
        }
        super.update(user);
    }

}
