package info.lmovse.blog.web.controller.user;

import com.vdurmont.emoji.EmojiParser;
import info.lmovse.blog.pojo.bo.RestResponse;
import info.lmovse.blog.pojo.dto.ErrorCode;
import info.lmovse.blog.pojo.dto.Types;
import info.lmovse.blog.pojo.po.Comment;
import info.lmovse.blog.service.ICommentService;
import info.lmovse.blog.util.IPKit;
import info.lmovse.blog.util.PatternKit;
import info.lmovse.blog.util.TaleUtils;
import info.lmovse.blog.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * Created by lmovse on 2017/8/11.
 * Tomorrow is a nice day.
 */
@Controller
public class CommentController extends BaseController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CommentController.class);

    @Resource
    private ICommentService commentService;

    /**
     * @param request     request
     * @param response    response
     * @param cid         文章 ID
     * @param coid        父级评论 ID
     * @param author      作者
     * @param mail        邮箱
     * @param url         网址
     * @param text        评论内容
     * @param _csrf_token 当前浏览用户的 token
     * @return 响应结果集
     */
    @PostMapping("/comments")
    @ResponseBody
    public RestResponse comment(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam Integer cid,
                                @RequestParam Integer coid,
                                @RequestParam String author,
                                @RequestParam String mail,
                                @RequestParam String url,
                                @RequestParam String text,
                                @RequestParam String _csrf_token) {
        String ref = request.getHeader("Referer");
        if (StringUtils.isBlank(ref) || StringUtils.isBlank(_csrf_token)) {
            return RestResponse.fail(ErrorCode.BAD_REQUEST);
        }

        String token = this.cache.hget(Types.CSRF_TOKEN.getType(), _csrf_token);
        if (StringUtils.isBlank(token)) {
            return RestResponse.fail(ErrorCode.BAD_REQUEST);
        }

        if (null == cid || StringUtils.isBlank(text)) {
            return RestResponse.fail("请输入完整后评论");
        }

        if (StringUtils.isNotBlank(author) && author.length() > 50) {
            return RestResponse.fail("姓名过长");
        }

        if (StringUtils.isNotBlank(mail) && !TaleUtils.isEmail(mail)) {
            return RestResponse.fail("请输入正确的邮箱格式");
        }

        if (StringUtils.isNotBlank(url) && !PatternKit.isURL(url)) {
            return RestResponse.fail("请输入正确的URL格式");
        }

        if (text.length() > 200) {
            return RestResponse.fail("请输入200个字符以内的评论");
        }

        String val = IPKit.getIpAddress(request) + ":" + cid;
        Integer count = cache.hget(Types.COMMENTS_FREQUENCY.getType(), val);
        if (null != count && count > 0) {
            return RestResponse.fail("您发表评论太快了，请过会再试");
        }

        author = TaleUtils.cleanXSS(author);
        text = TaleUtils.cleanXSS(text);

        author = EmojiParser.parseToAliases(author);
        text = EmojiParser.parseToAliases(text);

        Comment comments = new Comment();
        comments.setAuthor(author);
        comments.setCid(cid);
        comments.setIp(request.getRemoteAddr());
        comments.setUrl(url);
        comments.setContent(text);
        comments.setMail(mail);
        comments.setParent(coid);
        try {
            commentService.save(comments);
            cookie("tale_remember_author", URLEncoder.encode(author, "UTF-8"), 7 * 24 * 60 * 60, response);
            cookie("tale_remember_mail", URLEncoder.encode(mail, "UTF-8"), 7 * 24 * 60 * 60, response);
            if (StringUtils.isNotBlank(url)) {
                cookie("tale_remember_url", URLEncoder.encode(url, "UTF-8"), 7 * 24 * 60 * 60, response);
            }
            // 每个文章 1 分钟可以评论一次
            cache.hset(Types.COMMENTS_FREQUENCY.getType(), val, 1, 60);
            return RestResponse.ok();
        } catch (Exception e) {
            String msg = "评论发布失败";
            LOGGER.error(msg, e);
            return RestResponse.fail(msg);
        }
    }

}
