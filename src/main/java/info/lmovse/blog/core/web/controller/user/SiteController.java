package info.lmovse.blog.core.web.controller.user;

import com.github.pagehelper.PageInfo;
import info.lmovse.blog.core.constant.AppConst;
import info.lmovse.blog.core.pojo.bo.Archive;
import info.lmovse.blog.core.pojo.dto.Types;
import info.lmovse.blog.core.pojo.po.Comment;
import info.lmovse.blog.core.pojo.po.Content;
import info.lmovse.blog.core.pojo.po.Meta;
import info.lmovse.blog.core.service.ICommentService;
import info.lmovse.blog.core.service.IContentService;
import info.lmovse.blog.core.service.IMetaService;
import info.lmovse.blog.core.service.ISiteService;
import info.lmovse.blog.base.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static info.lmovse.blog.core.constant.AppConst.*;

/**
 * Created by lmovse on 2017/8/11.
 * Tomorrow is a nice day.
 */
@Controller
public class SiteController extends BaseController implements ErrorController {

    @Resource
    private IContentService contentService;

    @Resource
    private IMetaService metaService;

    @Resource
    private ISiteService siteService;

    @Resource
    private ICommentService commentService;

    private static final String ERROR_PATH = "/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    /**
     * 分类页
     *
     * @return
     */
    @GetMapping(value = "category/{keyword}")
    public ModelAndView categories(@PathVariable String keyword) {
        return categories(keyword, 1, PAGE_SIZE);
    }

    @GetMapping(value = "category/{keyword}/{pageNum}")
    public ModelAndView categories(@PathVariable String keyword,
                                   @PathVariable int pageNum,
                                   @RequestParam(value = "pageSize", defaultValue = PAGE_SIZE_STRING) int pageSize) {
        ModelAndView mv = new ModelAndView();
        pageNum = pageNum < 0 || pageNum > AppConst.MAX_PAGE ? 1 : pageNum;
        Meta meta = new Meta();
        meta.setType(Types.CATEGORY.getType());
        meta.setName(keyword);
        meta = metaService.findOneByProps(meta);

        if (meta == null) {
            mv.setViewName("comm/error_404");
            return mv;
        }
        PageInfo<Content> contentsPaginator = contentService.getArticles(meta.getMid(), pageNum, pageSize);
        mv.addObject("articles", contentsPaginator);
        mv.addObject("type", "分类");
        mv.addObject("keyword", keyword);
        mv.setViewName(themePrefix("page-category"));

        return mv;
    }

    /**
     * 归档页
     *
     * @return
     */
    @GetMapping(value = "archives")
    public String archives(Model model) {
        List<Archive> archives = siteService.getArchives();
        model.addAttribute("archives", archives);
        return themePrefix("archives");
    }

    /**
     * 友链页
     *
     * @return
     */
    @GetMapping(value = "candys")
    public String links(Model model) {
        List<Meta> candys = metaService.getMetas(Types.LINK.getType());
        model.addAttribute("candys", candys);
        return themePrefix("candys");
    }

    /**
     * 自定义页面, 如关于的页面
     */
    @GetMapping(value = "/{pageName}")
    public String page(@PathVariable String pageName, Integer cp, Model model) {
        Content content = contentService.findById(pageName);
        if (content == null) {
            return this.renderNotFound();
        }
        if (content.getAllowComment()) {
            Integer commentPage = cp == null ? 1 : cp;
            model.addAttribute("cp", commentPage);
            Example example = new Example(Comment.class);
            example.createCriteria()
                    .andEqualTo("cid", content.getCid())
                    .andEqualTo("parent", 0);
            example.setOrderByClause("coid desc");
            PageInfo<Comment> commentsPaginator = commentService.findPageByExample(example, commentPage, COMMENT_PAGE_SIZE);
            model.addAttribute("comments", commentsPaginator);
        }
        model.addAttribute("article", content);
        return themePrefix("page");
    }

    /**
     * 搜索页
     *
     * @param keyword
     * @return
     */
    @GetMapping(value = "search/{keyword}")
    public ModelAndView search(@PathVariable String keyword, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        return this.search(keyword, 1, limit);
    }

    @GetMapping(value = "search/{keyword}/{page}")
    public ModelAndView search(@PathVariable String keyword,
                               @PathVariable int page,
                               @RequestParam(value = "limit", defaultValue = AppConst.PAGE_SIZE_STRING) int limit) {
        page = page < 0 || page > AppConst.MAX_PAGE ? 1 : page;
        PageInfo<Content> articles = contentService.getArticles(keyword, page, limit);
        ModelAndView mv = new ModelAndView(themePrefix("page-category"));
        mv.addObject("articles", articles);
        mv.addObject("type", "搜索");
        mv.addObject("keyword", keyword);
        return mv;
    }

    /**
     * 标签页
     *
     * @param name
     * @return
     */
    @GetMapping(value = "tag/{name}")
    public String tags(HttpServletRequest request, @PathVariable String name, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        return this.tags(request, name, 1, limit);
    }

    /**
     * 标签分页
     *
     * @param request
     * @param name
     * @param page
     * @param limit
     * @return
     */
    @GetMapping(value = "tag/{name}/{page}")
    public String tags(HttpServletRequest request, @PathVariable String name, @PathVariable int page, @RequestParam(value = "limit", defaultValue = "12") int limit) {

        page = page < 0 || page > AppConst.MAX_PAGE ? 1 : page;
        // 对于空格的特殊处理
        name = name.replaceAll("\\+", " ");
        Meta meta = new Meta();
        meta.setType(Types.TAG.getType());
        meta.setName(name);
        meta = metaService.findOneByProps(meta);
        if (meta == null) {
            return this.renderNotFound();
        }
        PageInfo<Content> contentsPaginator = contentService.getArticles(meta.getMid(), page, limit);
        request.setAttribute("articles", contentsPaginator);
        request.setAttribute("type", "标签");
        request.setAttribute("keyword", name);

        return themePrefix("page-category");
    }

    /**
     * 自定義錯誤處理頁面
     */
    @RequestMapping(ERROR_PATH)
    public String error(HttpServletRequest request, Model model, Exception e) {
        Map<String, Object> errorMap = errorAttributes
                .getErrorAttributes(new ServletRequestAttributes(request), false);
        model.addAttribute("errors", errorMap);
        return "comm/error_404";
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

}
