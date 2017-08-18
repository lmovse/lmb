package info.lmovse.blog.base.web.controller;

import info.lmovse.blog.core.pojo.po.User;
import info.lmovse.blog.core.util.MapCache;
import info.lmovse.blog.core.util.TaleUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lmovse on 2017/2/21.
 * Tomorrow is a nice day.
 */
public abstract class BaseController {
    protected static String THEME_PREFIX = "themes/default/";
    protected MapCache cache = MapCache.single();

    public BaseController title(HttpServletRequest request, String title) {
        request.setAttribute("title", title);
        return this;
    }

    public BaseController keywords(HttpServletRequest request, String keywords) {
        request.setAttribute("keywords", keywords);
        return this;
    }

    public String themePrefix(String viewName) {
        return THEME_PREFIX + viewName;
    }

    /**
     * 设置cookie
     *
     * @param name
     * @param value
     * @param maxAge
     * @param response
     */
    public void cookie(String name, String value, int maxAge, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

    /**
     * 获取请求绑定的登录对象
     * @param request request
     * @return user
     */
    public User getUser(HttpServletRequest request) {
        return TaleUtils.getLoginUser(request);
    }

    public Integer getUid(HttpServletRequest request){
        return this.getUser(request).getUid();
    }

    public String renderNotFound() {
        return "comm/error_404";
    }

}
