package info.lmovse.blog.core.web.controller.admin;

import com.github.pagehelper.PageInfo;
import com.vdurmont.emoji.EmojiParser;
import info.lmovse.blog.core.pojo.bo.RestResponse;
import info.lmovse.blog.core.pojo.po.Comment;
import info.lmovse.blog.core.pojo.po.User;
import info.lmovse.blog.core.service.ICommentService;
import info.lmovse.blog.core.util.TaleUtils;
import info.lmovse.blog.base.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by 13 on 2017/2/26.
 */
@Controller("adminController")
@RequestMapping("admin/comments")
public class CommentController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentController.class);

    @Resource
    private ICommentService commentsService;

    @GetMapping(value = "")
    public ModelAndView index(@RequestParam(value = "page", defaultValue = "1") int pageNum,
                              @RequestParam(value = "limit", defaultValue = "15") int pageSize,
                              HttpServletRequest request) {
        User user = this.getUser(request);

        Example commentVoExample = new Example(Comment.class);
        commentVoExample.setOrderByClause("coid desc");
        commentVoExample.createCriteria().andEqualTo("ownerId", user.getUid());
        PageInfo<Comment> commentsPaginator = commentsService.findPageByExample(commentVoExample, pageNum, pageSize);

        ModelAndView mv = new ModelAndView("admin/comment_list");
        mv.addObject("comments", commentsPaginator);
        return mv;
    }

    /**
     * 删除一条评论
     *
     * @param coid
     * @return
     */
    @PostMapping(value = "delete")
    @ResponseBody
    public RestResponse delete(String coid) {
        try {
            commentsService.deleteById(coid);
        } catch (Exception e) {
            String msg = "评论删除失败";
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

    @PostMapping(value = "status")
    @ResponseBody
    public RestResponse delete(Integer coid, String status) {
        try {
            Comment comments = new Comment();
            comments.setCoid(coid);
            comments.setStatus(status);
            commentsService.update(comments);
        } catch (Exception e) {
            String msg = "操作失败";
            LOGGER.error(msg, e);
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

    @PostMapping(value = "")
    @ResponseBody
    public RestResponse reply(String coid, String content, HttpServletRequest request) {
        if (null == coid || StringUtils.isBlank(content)) {
            return RestResponse.fail("请输入完整后评论");
        }

        if (content.length() > 2000) {
            return RestResponse.fail("请输入2000个字符以内的回复");
        }
        Comment c = commentsService.findById(coid);
        if (null == c) {
            return RestResponse.fail("不存在该评论");
        }
        User users = this.getUser(request);
        content = TaleUtils.cleanXSS(content);
        content = EmojiParser.parseToAliases(content);

        Comment comments = new Comment();
        comments.setAuthor(users.getUsername());
        comments.setAuthorId(users.getUid());
        comments.setCid(c.getCid());
        comments.setIp(request.getRemoteAddr());
        comments.setUrl(users.getHomeUrl());
        comments.setContent(content);
        comments.setMail(users.getEmail());
        comments.setParent(Integer.valueOf(coid));
        try {
            commentsService.save(comments);
            return RestResponse.ok();
        } catch (Exception e) {
            String msg = "回复失败";
            LOGGER.error(msg, e);
            return RestResponse.fail(msg);
        }
    }

}
