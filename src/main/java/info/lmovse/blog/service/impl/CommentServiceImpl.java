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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, Object> valueOps;

    @Override
    public void save(Comment comment) {
        // 参数检测
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

        // 新增評論的時候更新文章緩存
        Integer cid = comment.getCid();
        try {
            Content content = (Content) valueOps.get(prefixKey(cid));
            if (StringUtils.isNotBlank(content.getSlug())) {
                content = (Content) valueOps.get(prefixKey(content.getSlug()));
                content.setCommentsNum(content.getCommentsNum() + 1);
                valueOps.set(prefixKey(content.getSlug()), content);
            } else {
                content.setCommentsNum(content.getCommentsNum() + 1);
                valueOps.set(prefixKey(content.getCid()), content);
            }
            // 缓存中评论数量满 10 条时同步到数据库中
            if (content.getCommentsNum() % 10 == 0) {
                Content uContent = new Content();
                uContent.setCommentsNum(content.getCommentsNum());
                contentMapper.updateByPrimaryKeySelective(uContent);
            }
        } catch (Exception e) {
            LOGGER.error("更新緩存失敗！失敗詳情：{}", e.getMessage(), e);
        }
    }

    @Override
    public void update(Comment comments) {
        if (null != comments && null != comments.getCoid()) {
            super.update(comments);
        }
    }

    @Override
    public void deleteById(String coid) {
        // 删除评论
        if (null == coid) {
            throw new ServiceException("主键为空");
        }
        Comment comment = super.findById(coid);
        if (comment == null) {
            throw new ServiceException("没有此评论");
        }
        super.deleteById(coid);

        // 刪除評論的時候更新緩存
        Integer cid = comment.getCid();
        try {
            Content content = (Content) valueOps.get(prefixKey(cid));
            if (StringUtils.isNotBlank(content.getSlug())) {
                content = (Content) valueOps.get(prefixKey(content.getSlug()));
                content.setCommentsNum(content.getCommentsNum() - 1);
                valueOps.set(prefixKey(content.getSlug()), content);
            } else {
                content.setCommentsNum(content.getCommentsNum() - 1);
                valueOps.set(prefixKey(content.getCid()), content);
            }
            if (content.getCommentsNum() % 10 == 0) {
                Content uContent = new Content();
                uContent.setCommentsNum(content.getCommentsNum());
                contentMapper.updateByPrimaryKeySelective(uContent);
            }
        } catch (Exception e) {
            LOGGER.error("更新緩存失敗！失敗詳情：{}", e.getMessage(), e);
        }
    }

    private String prefixKey(Object key) {
        return "content:" + key.toString();
    }

}