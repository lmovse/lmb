package info.lmovse.blog.security;

import info.lmovse.blog.exception.ServiceException;
import info.lmovse.blog.mapper.UserMapper;
import info.lmovse.blog.pojo.po.User;
import info.lmovse.blog.util.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by lmovse on 2017/8/17.
 * Tomorrow is a nice day.
 */
@Configuration
@EnableWebSecurity
public class AppAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        String username = token.getName();
        User user = userMapper.findUserByName(username);
        if (user == null) {
            throw new ServiceException("用户不存在，请确认后重新输入！");
        }
        // 数据库用户的密码
        String password = user.getPassword();
        String pwdDigest = Md5Util.pwdDigest(token.getCredentials().toString());
        // 与 authentication 里面的 credentials 相比较
        if (!password.equals(pwdDigest)) {
            throw new BadCredentialsException("Invalid username/password");
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        // 授权
        return new UsernamePasswordAuthenticationToken(user, password);
    }

    public void config(WebSecurity web) {
        web.ignoring().antMatchers("/js/**", "/css/**", "/admin/**");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}
