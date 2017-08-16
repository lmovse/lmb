package info.lmovse.blog.web.controller.admin;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import info.lmovse.blog.constant.AppConst;
import info.lmovse.blog.exception.ServiceException;
import info.lmovse.blog.pojo.bo.RestResponse;
import info.lmovse.blog.pojo.bo.Statistics;
import info.lmovse.blog.pojo.dto.LogActions;
import info.lmovse.blog.pojo.po.Comment;
import info.lmovse.blog.pojo.po.Content;
import info.lmovse.blog.pojo.po.Log;
import info.lmovse.blog.pojo.po.User;
import info.lmovse.blog.service.ILogService;
import info.lmovse.blog.service.ISiteService;
import info.lmovse.blog.service.IUserService;
import info.lmovse.blog.util.TaleUtils;
import info.lmovse.blog.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 后台管理首页
 * Created by Administrator on 2017/3/9 009.
 */
@Controller("adminIndexController")
@RequestMapping("/admin")
public class IndexController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @Resource
    private ISiteService siteService;

    @Resource
    private ILogService logService;

    @Resource
    private IUserService userService;

    /**
     * 页面跳转
     *
     * @return
     */
    @GetMapping(value = {"", "/index"})
    public String index(Model model) {
        LOGGER.info("Enter admin index method");
        List<Comment> comments = siteService.recentComments(5);
        List<Content> contents = siteService.recentContents(5);
        Statistics statistics = siteService.getStatistics();

        // 取最新的 10 条日志
        Example example =  new Example(Log.class);
        example.setOrderByClause("created desc");
        PageInfo<Log> page = logService.findPageByExample(example, 1, 10);

        model.addAttribute("comments", comments);
        model.addAttribute("articles", contents);
        model.addAttribute("statistics", statistics);
        model.addAttribute("logs", page.getList());
        LOGGER.info("Exit admin index method");
        return "admin/index";
    }

    /**
     * 个人设置页面
     */
    @GetMapping(value = "profile")
    public String profile() {
        return "admin/profile";
    }


    /**
     * 保存个人信息
     */
    @PostMapping(value = "/profile")
    @ResponseBody
    public RestResponse saveProfile(@RequestParam String screenName, @RequestParam String email, HttpServletRequest request, HttpSession session) {
        User users = this.getUser(request);
        if (StringUtils.isNotBlank(screenName) && StringUtils.isNotBlank(email)) {
            User temp = new User();
            temp.setUid(users.getUid());
            temp.setScreenName(screenName);
            temp.setEmail(email);
            userService.update(temp);
            logService.insertLog(LogActions.UP_INFO.getAction(), JSON.toJSONString(temp), request.getRemoteAddr(), this.getUid(request));

            //更新session中的数据
            User original = (User) session.getAttribute(AppConst.LOGIN_SESSION_KEY);
            original.setScreenName(screenName);
            original.setEmail(email);
            session.setAttribute(AppConst.LOGIN_SESSION_KEY, original);
        }
        return RestResponse.ok();
    }

    /**
     * 修改密码
     */
    @PostMapping(value = "/password")
    @ResponseBody
    public RestResponse upPwd(@RequestParam String oldPassword, @RequestParam String password, HttpServletRequest request, HttpSession session) {
        User users = this.getUser(request);
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(password)) {
            return RestResponse.fail("请确认信息输入完整");
        }

        if (!users.getPassword().equals(TaleUtils.MD5encode(users.getUsername() + oldPassword))) {
            return RestResponse.fail("旧密码错误");
        }
        if (password.length() < 6 || password.length() > 14) {
            return RestResponse.fail("请输入6-14位密码");
        }

        try {
            User temp = new User();
            temp.setUid(users.getUid());
            String pwd = TaleUtils.MD5encode(users.getUsername() + password);
            temp.setPassword(pwd);
            userService.update(temp);
            logService.insertLog(LogActions.UP_PWD.getAction(), null, request.getRemoteAddr(), this.getUid(request));

            //更新session中的数据
            User original = (User) session.getAttribute(AppConst.LOGIN_SESSION_KEY);
            original.setPassword(pwd);
            session.setAttribute(AppConst.LOGIN_SESSION_KEY, original);
            return RestResponse.ok();
        } catch (Exception e) {
            String msg = "密码修改失败";
            if (e instanceof ServiceException) {
                msg = e.getMessage();
            } else {
                LOGGER.error(msg, e);
            }
            return RestResponse.fail(msg);
        }
    }

}
