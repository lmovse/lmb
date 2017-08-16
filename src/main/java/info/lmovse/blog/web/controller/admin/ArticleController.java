package info.lmovse.blog.web.controller.admin;


import com.github.pagehelper.PageInfo;
import info.lmovse.blog.exception.ServiceException;
import info.lmovse.blog.pojo.bo.RestResponse;
import info.lmovse.blog.pojo.dto.LogActions;
import info.lmovse.blog.pojo.dto.Types;
import info.lmovse.blog.pojo.po.Content;
import info.lmovse.blog.pojo.po.Meta;
import info.lmovse.blog.pojo.po.User;
import info.lmovse.blog.service.IContentService;
import info.lmovse.blog.service.ILogService;
import info.lmovse.blog.service.IMetaService;
import info.lmovse.blog.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by 13 on 2017/2/21.
 */
@Controller
@RequestMapping("/admin/article")
@Transactional(rollbackFor = ServiceException.class)
public class ArticleController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleController.class);

    @Resource
    private IContentService contentsService;

    @Resource
    private IMetaService metasService;

    @Resource
    private ILogService logService;

    @GetMapping(value = "")
    public String index(@RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "15") int limit, HttpServletRequest request) {
        Example contentVoExample = new Example(Content.class);
        contentVoExample.setOrderByClause("created desc");
        contentVoExample.createCriteria().andEqualTo("type", Types.ARTICLE.getType());
        PageInfo<Content> contentsPaginator = contentsService.findPageByExample(contentVoExample, page, limit);
        request.setAttribute("articles", contentsPaginator);
        return "admin/article_list";
    }

    @GetMapping(value = "/publish")
    public String newArticle(Model model) {
        List<Meta> categories = metasService.getMetas(Types.CATEGORY.getType());
        model.addAttribute("categories", categories);
        return "admin/article_edit";
    }

    @GetMapping(value = "/{cid}")
    public String editArticle(@PathVariable  String cid, Model model) {
        Content contents = contentsService.findById(cid);
        model.addAttribute("contents", contents);
        List<Meta> categories = metasService.getMetas(Types.CATEGORY.getType());
        model.addAttribute("categories", categories);
        model.addAttribute("active", "article");
        return "admin/article_edit";
    }

    @PostMapping(value = "/publish")
    @ResponseBody
    public RestResponse publishArticle(Content contents, HttpServletRequest request) {
        User users = this.getUser(request);
        contents.setAuthorId(users.getUid());
        contents.setType(Types.ARTICLE.getType());
        if (StringUtils.isBlank(contents.getCategories())) {
            contents.setCategories("默认分类");
        }
        try {
            contentsService.publish(contents);
        } catch (Exception e) {
            String msg = "文章发布失败";
            LOGGER.error(e.getMessage());
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

    @PostMapping(value = "/modify")
    @ResponseBody
    public RestResponse modifyArticle(Content contents, HttpServletRequest request) {
        User users = this.getUser(request);
        contents.setAuthorId(users.getUid());
        contents.setType(Types.ARTICLE.getType());
        try {
            contentsService.update(contents);
        } catch (Exception e) {
            String msg = "文章编辑失败";
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

    @RequestMapping(value = "/delete")
    @ResponseBody
    public RestResponse delete(String cid, HttpServletRequest request) {
        try {
            contentsService.deleteById(cid);
            logService.insertLog(LogActions.DEL_ARTICLE.getAction(), cid + "", request.getRemoteAddr(), this.getUid(request));
        } catch (Exception e) {
            String msg = "文章删除失败";
            LOGGER.error(e.getMessage(), e);
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

}
