package info.lmovse.blog.web.interceptor;

import info.lmovse.blog.constant.AppConst;
import info.lmovse.blog.pojo.dto.Types;
import info.lmovse.blog.pojo.po.Option;
import info.lmovse.blog.pojo.po.User;
import info.lmovse.blog.service.IOptionService;
import info.lmovse.blog.service.ISiteService;
import info.lmovse.blog.service.IUserService;
import info.lmovse.blog.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义拦截器
 * Created by BlueT on 2017/3/9.
 */
@Component
public class BaseInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGE = LoggerFactory.getLogger(BaseInterceptor.class);
    private static final String USER_AGENT = "getUser-agent";

    @Resource
    private IUserService userService;

    @Resource
    private IOptionService optionService;

    @Resource
    private ISiteService siteService;

    private MapCache cache = MapCache.single();

    @Resource
    private Commons commons;

    @Resource
    private AdminCommons adminCommons;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String uri = request.getRequestURI();

        LOGGE.info("UserAgent: {}", request.getHeader(USER_AGENT));
        LOGGE.info("用户访问地址: {}, 来路地址: {}", uri, IPKit.getIpAddress(request));

        //请求拦截处理
        User user = TaleUtils.getLoginUser(request);
        if (null == user) {
            Integer uid = TaleUtils.getCookieUid(request);
            if (null != uid) {
                //这里还是有安全隐患,cookie 是可以伪造的，后期可以改用 token 来验证
                user = userService.findById(String.valueOf(uid));
                request.getSession().setAttribute(AppConst.LOGIN_SESSION_KEY, user);
            }
        }

        // 管理员也是一个用户，登录时同样在此记录登录日志
        if (uri.startsWith("/admin") && !uri.startsWith("/admin/login") && null == user) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return false;
        }

        //设置当前浏览用户的 token
        if (request.getMethod().equals("GET")) {
            String csrf_token = UUID.UU64();

            // 默认存储 30 分钟
            cache.hset(Types.CSRF_TOKEN.getType(), csrf_token, uri, 30 * 60);
            request.setAttribute("_csrf_token", csrf_token);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse httpServletResponse,
                           Object o,
                           ModelAndView modelAndView) throws Exception {
        // 获取页面配置
        Option ov = optionService.findById("site_record");
        Commons.setSiteService(siteService);

        // 封装页面工具类到页面中
        modelAndView.addObject("commons", commons);
        modelAndView.addObject("option", ov);
        modelAndView.addObject("adminCommons", adminCommons);
    }

}
