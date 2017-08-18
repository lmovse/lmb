package info.lmovse.blog.shiro;

import info.lmovse.blog.util.Encrypt;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;

/**
 * Created by lmovse on 2017/5/19.
 * Tomorrow is a nice day.
 */
public class MyCredentialsMatcher extends SimpleCredentialsMatcher {

    /**
     * 密码验证策略
     * @param token
     * @param info
     * @return
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        UsernamePasswordToken passwordToken = (UsernamePasswordToken) token;
        String password = new String(passwordToken.getPassword());
        String tokenCredentials = Encrypt.md5(password, passwordToken.getUsername());
        Object accountCredentials = info.getCredentials();
        return true;
    }

}
