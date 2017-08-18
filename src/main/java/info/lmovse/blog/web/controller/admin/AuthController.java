package info.lmovse.blog.web.controller.admin;

import info.lmovse.blog.constant.AppConst;
import info.lmovse.blog.exception.ServiceException;
import info.lmovse.blog.pojo.bo.RestResponse;
import info.lmovse.blog.pojo.dto.LogActions;
import info.lmovse.blog.pojo.po.User;
import info.lmovse.blog.service.ILogService;
import info.lmovse.blog.service.IUserService;
import info.lmovse.blog.util.TaleUtils;
import info.lmovse.blog.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 用户后台登录/登出
 * Created by BlueT on 2017/3/11.
 */
@Controller
@RequestMapping("/admin")
@Transactional(rollbackFor = ServiceException.class)
public class AuthController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Resource
    private IUserService usersService;

    @Resource
    private ILogService logService;

    @GetMapping(value = "/login")
    public String login() {
        return "admin/login";
    }

    @PostMapping(value = "/login")
    @ResponseBody
    public RestResponse login(String username, String password,
                              @RequestParam(required = false) String remeber_me,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        Integer error_count = cache.get("login_error_count");
        try {
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();
            request.getSession().setAttribute(AppConst.LOGIN_SESSION_KEY, user);
            if (StringUtils.isNotBlank(remeber_me)) {
                TaleUtils.setCookie(response, user.getUid());
            }
            logService.insertLog(LogActions.LOGIN.getAction(), null, request.getRemoteAddr(), user.getUid());
        } catch (Exception e) {
            error_count = null == error_count ? 1 : error_count + 1;
            if (error_count > 3) {
                return RestResponse.fail("您输入密码已经错误超过3次，请10分钟后尝试");
            }
            cache.set("login_error_count", error_count, 10 * 60);
            String msg = "登录失败";
            if (e instanceof ServiceException) {
                msg = e.getMessage();
            } else {
                LOGGER.error(msg, e);
            }
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

    /**
     * 注销
     *
     * @param session
     * @param response
     */
    @RequestMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) {
        session.removeAttribute(AppConst.LOGIN_SESSION_KEY);
        Cookie cookie = new Cookie(AppConst.USER_IN_COOKIE, "");
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        cookie.setValue(null);
        cookie.setMaxAge(0);// 立即销毁cookie
        cookie.setPath("/");
        response.addCookie(cookie);
        try {
            response.sendRedirect("/admin/login");
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("注销失败", e);
        }
    }

}
