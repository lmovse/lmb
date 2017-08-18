package info.lmovse.blog.service.impl;

import info.lmovse.blog.exception.ServiceException;
import info.lmovse.blog.mapper.ContentMapper;
import info.lmovse.blog.pojo.po.Comment;
import info.lmovse.blog.pojo.po.Content;
import info.lmovse.blog.service.ICommentService;
import info.lmovse.blog.util.DateKit;
import info.lmovse.blog.util.TaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created by lmovse on 2017/8/15.
 * Tomorrow is a nice day.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CommentServiceImpl extends AbstractServiceImpl<Comment> implements ICommentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Resource
    private ContentMapper contentMapper;

    @Override
    public void save(Comment comment) {
        if (null == comment) {
            throw new ServiceException("评论对象为空");
        }
        if (StringUtils.isBlank(comment.getAuthor())) {
            comment.setAuthor("热心网友");
        }
        if (StringUtils.isNotBlank(comment.getMail()) && !TaleUtils.isEmail(comment.getMail())) {
            throw new ServiceException("请输入正确的邮箱格式");
        }
        if (StringUtils.isBlank(comment.getContent())) {
            throw new ServiceException("评论内容不能为空");
        }
        if (comment.getContent().length() < 5 || comment.getContent().length() > 2000) {
            throw new ServiceException("评论字数在5-2000个字符");
        }
        if (null == comment.getCid()) {
            throw new ServiceException("评论文章不能为空");
        }
        Content contents = contentMapper.selectByPrimaryKey(comment.getCid());
        if (null == contents) {
            throw new ServiceException("不存在的文章");
        }

        // 保存评论
        comment.setOwnerId(contents.getAuthorId());
        comment.setCreated(DateKit.getCurrentUnixTime());
        super.save(comment);

        // 更新文章评论数量
        updateComments(comment.getCid());
    }

    @Override
    public void update(Comment comments) {
        if (null != comments && null != comments.getCoid()) {
            super.update(comments);
        }
    }

    @Override
    public void deleteById(String coid) {
        if (null == coid) {
            throw new ServiceException("主键为空");
        }

        // 删除评论
        Comment comment = super.findById(coid);
        if (comment == null) {
            throw new ServiceException("没有此评论");
        }
        super.deleteById(coid);

        // 更新文章评论数量
        updateComments(comment.getCid());
    }

    private void updateComments(Integer cid) {
        Content content = contentMapper.selectByPrimaryKey(cid);
        Content uContent = new Content();
        uContent.setCid(content.getCid());
        uContent.setCommentsNum(content.getCommentsNum() + 1);
        contentMapper.updateByPrimaryKeySelective(uContent);
    }

}