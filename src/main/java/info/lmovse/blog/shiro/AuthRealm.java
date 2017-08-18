package info.lmovse.blog.shiro;

import info.lmovse.blog.mapper.UserMapper;
import info.lmovse.blog.pojo.po.User;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by lmovse on 2017/5/19.
 * Tomorrow is a nice day.
 * Authentication: 身份认证
 * Authorization: 身份授权
 * credentials: 认证，证书，凭证
 */
public class AuthRealm extends AuthorizingRealm {
    private static Logger logger = LoggerFactory.getLogger(AuthRealm.class);

    @Autowired
    private UserMapper userMapper;

    /**
     * 授权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        logger.info("正在授权中。。。");
        User user = (User) principalCollection.fromRealm(this.getName()).iterator().next();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        return info;
    }

    /**
     * 认证
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        logger.info("正在认证中。。。");
        UsernamePasswordToken passwordToken = (UsernamePasswordToken) authenticationToken;
        User user = userMapper.findUserByName(passwordToken.getUsername());
        if (user == null) {
            return null;
        }
        return new SimpleAuthenticationInfo(user, user.getPassword(), this.getName());
    }

}
