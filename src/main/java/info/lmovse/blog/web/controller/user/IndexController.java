package info.lmovse.blog.web.controller.user;

import com.github.pagehelper.PageInfo;
import info.lmovse.blog.pojo.dto.Types;
import info.lmovse.blog.pojo.po.Content;
import info.lmovse.blog.service.ICommentService;
import info.lmovse.blog.service.IContentService;
import info.lmovse.blog.service.IMetaService;
import info.lmovse.blog.service.ISiteService;
import info.lmovse.blog.util.TaleUtils;
import info.lmovse.blog.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static info.lmovse.blog.constant.AppConst.PAGE_SIZE;

/**
 * 首页
 * Created by Administrator on 2017/3/8 008.
 */
@Controller
public class IndexController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @Resource
    private IContentService contentService;

    @Resource
    private ICommentService commentService;

    @Resource
    private IMetaService metaService;

    @Resource
    private ISiteService siteService;

    /**
     * 首页
     *
     * @return 首页
     */
    @GetMapping(value = "/")
    public String index(Model model) {
        Example example = new Example(Content.class);
        example.setOrderByClause("created desc");
        example.createCriteria()
                .andEqualTo("type", Types.ARTICLE.getType())
                .andEqualTo("status", Types.PUBLISH.getType());
        PageInfo<Content> articles = contentService.findPageByExample(example, 1, PAGE_SIZE);
        model.addAttribute("articles", articles);
        model.addAttribute("title", "第 1 頁");
        return themePrefix("index");
    }

    /**
     * 注销
     *
     * @param session
     * @param response
     */
    @RequestMapping("logout")
    public void logout(HttpSession session, HttpServletResponse response) {
        TaleUtils.logout(session, response);
    }

}
