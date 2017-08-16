package info.lmovse.blog.web.controller.user;

import com.github.pagehelper.PageInfo;
import info.lmovse.blog.constant.AppConst;
import info.lmovse.blog.pojo.dto.Types;
import info.lmovse.blog.pojo.po.Comment;
import info.lmovse.blog.pojo.po.Content;
import info.lmovse.blog.service.ICommentService;
import info.lmovse.blog.service.IContentService;
import info.lmovse.blog.web.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;

import static info.lmovse.blog.constant.AppConst.COMMENT_PAGE_SIZE;
import static info.lmovse.blog.constant.AppConst.PAGE_SIZE;

/**
 * Created by lmovse on 2017/8/11.
 * Tomorrow is a nice day.
 */
@Controller
public class ContentController extends BaseController {

    @Resource
    private IContentService contentService;

    @Resource
    private ICommentService commentService;

    /**
     * 首页分页
     *
     * @param pageNum 第几页
     * @return 主页文章列表
     */
    @GetMapping(value = "page/{pageNum}")
    public String indexWithPage(Model model, @PathVariable int pageNum) {
        pageNum = pageNum < 0 || pageNum > AppConst.MAX_PAGE ? 1 : pageNum;
        Example example = new Example(Content.class);
        example.setOrderByClause("created desc");
        example.createCriteria()
                .andEqualTo("type", Types.ARTICLE.getType())
                .andEqualTo("status", Types.PUBLISH.getType());
        PageInfo<Content> articles = contentService.findPageByExample(example, pageNum, PAGE_SIZE);
        model.addAttribute("articles", articles);
        model.addAttribute("title", pageNum);
        return themePrefix("index");
    }

    /**
     * 文章详情页
     *
     * @param cp    评论页码
     * @param model 模型对象
     * @param cid   文章主键或别名
     * @return
     */
    @GetMapping(value = {"article/{cid}", "article/{cid}.html"})
    public String getArticle(Integer cp, Model model, @PathVariable String cid) {
        Content content = contentService.findById(cid);
        if (null == content || "draft".equals(content.getStatus())) {
            return this.renderNotFound();
        }
        model.addAttribute("article", content);
        model.addAttribute("is_post", true);
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
        return themePrefix("post");
    }

}
