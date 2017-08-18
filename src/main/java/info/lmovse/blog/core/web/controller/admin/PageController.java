package info.lmovse.blog.core.web.controller.admin;

import com.github.pagehelper.PageInfo;
import info.lmovse.blog.core.constant.AppConst;
import info.lmovse.blog.core.exception.ServiceException;
import info.lmovse.blog.core.pojo.bo.RestResponse;
import info.lmovse.blog.core.pojo.dto.LogActions;
import info.lmovse.blog.core.pojo.dto.Types;
import info.lmovse.blog.core.pojo.po.Content;
import info.lmovse.blog.core.pojo.po.User;
import info.lmovse.blog.core.service.IContentService;
import info.lmovse.blog.core.service.ILogService;
import info.lmovse.blog.base.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by 13 on 2017/2/21.
 */
@Controller()
@RequestMapping("admin/page")
public class PageController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageController.class);

    @Resource
    private IContentService contentsService;

    @Resource
    private ILogService logService;

    @GetMapping(value = "")
    public String index(Model model) {
        Example contentVoExample = new Example(Content.class);
        contentVoExample.setOrderByClause("created desc");
        contentVoExample.createCriteria().andEqualTo("type", Types.PAGE.getType());
        PageInfo<Content> contentsPaginator = contentsService.findPageByExample(contentVoExample, 1, AppConst.MAX_POSTS);
        model.addAttribute("articles", contentsPaginator);
        return "admin/page_list";
    }

    @GetMapping(value = "new")
    public String newPage(HttpServletRequest request) {
        return "admin/page_edit";
    }

    @GetMapping(value = "/{cid}")
    public String editPage(@PathVariable String cid, Model model) {
        Content contents = contentsService.findById(cid);
        model.addAttribute("contents", contents);
        return "admin/page_edit";
    }

    @PostMapping(value = "publish")
    @ResponseBody
    public RestResponse publishPage(@RequestParam String title,
                                    @RequestParam String content,
                                    @RequestParam String status,
                                    @RequestParam String slug,
                                    @RequestParam(required = false) Integer allowComment,
                                    @RequestParam(required = false) Integer allowPing,
                                    HttpServletRequest request) {

        User users = this.getUser(request);
        Content contents = new Content();
        contents.setTitle(title);
        contents.setContent(content);
        contents.setStatus(status);
        contents.setSlug(slug);
        contents.setType(Types.PAGE.getType());
        if (null != allowComment) {
            contents.setAllowComment(allowComment == 1);
        }
        if (null != allowPing) {
            contents.setAllowPing(allowPing == 1);
        }
        contents.setAuthorId(users.getUid());

        try {
            contentsService.publish(contents);
        } catch (Exception e) {
            String msg = "页面发布失败";
            if (e instanceof ServiceException) {
                msg = e.getMessage();
            } else {
                LOGGER.error(msg, e);
            }
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

    @PostMapping(value = "modify")
    @ResponseBody
    public RestResponse modifyArticle(@RequestParam Integer cid,
                                      @RequestParam String title,
                                      @RequestParam String content,
                                      @RequestParam String status,
                                      @RequestParam String slug,
                                      @RequestParam(required = false) Integer allowComment,
                                      @RequestParam(required = false) Integer allowPing,
                                      HttpServletRequest request) {

        User users = this.getUser(request);
        Content contents = new Content();
        contents.setCid(cid);
        contents.setTitle(title);
        contents.setContent(content);
        contents.setStatus(status);
        contents.setSlug(slug);
        contents.setType(Types.PAGE.getType());
        if (null != allowComment) {
            contents.setAllowComment(allowComment == 1);
        }
        if (null != allowPing) {
            contents.setAllowPing(allowPing == 1);
        }
        contents.setAuthorId(users.getUid());
        try {
            contentsService.update(contents);
        } catch (Exception e) {
            String msg = "页面编辑失败";
            LOGGER.error(msg, e);
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

    @RequestMapping(value = "delete")
    @ResponseBody
    public RestResponse delete(String cid, HttpServletRequest request) {
        try {
            contentsService.deleteById(cid);
            logService.insertLog(LogActions.DEL_PAGE.getAction(), cid + "", request.getRemoteAddr(), this.getUid(request));
        } catch (Exception e) {
            String msg = "页面删除失败";
            LOGGER.error(msg, e);
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }
}
