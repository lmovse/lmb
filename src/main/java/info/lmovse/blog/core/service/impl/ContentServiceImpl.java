package info.lmovse.blog.core.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vdurmont.emoji.EmojiParser;
import info.lmovse.blog.base.service.impl.AbstractServiceImpl;
import info.lmovse.blog.core.constant.AppConst;
import info.lmovse.blog.core.exception.ServiceException;
import info.lmovse.blog.core.mapper.CommentMapper;
import info.lmovse.blog.core.mapper.ContentMapper;
import info.lmovse.blog.core.mapper.MetaMapper;
import info.lmovse.blog.core.mapper.RelationshipMapper;
import info.lmovse.blog.core.pojo.dto.Types;
import info.lmovse.blog.core.pojo.po.Comment;
import info.lmovse.blog.core.pojo.po.Content;
import info.lmovse.blog.core.pojo.po.Meta;
import info.lmovse.blog.core.pojo.po.Relationship;
import info.lmovse.blog.core.service.IContentService;
import info.lmovse.blog.core.util.DateKit;
import info.lmovse.blog.core.util.TaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by lmovse on 2017/8/14
 * Tomorrow is a nice day.
 */
@Service
public class ContentServiceImpl extends AbstractServiceImpl<Content> implements IContentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentServiceImpl.class);

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private MetaMapper metaMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private RelationshipMapper relationshipMapper;

    @Override
    public void publish(Content content) {
        if (content == null) {
            throw new ServiceException("文章对象为空");
        }
        if (StringUtils.isBlank(content.getTitle())) {
            throw new ServiceException("文章标题不能为空");
        }
        if (StringUtils.isBlank(content.getContent())) {
            throw new ServiceException("文章内容不能为空");
        }
        if (content.getTitle().length() > AppConst.MAX_TITLE_COUNT) {
            throw new ServiceException("文章标题过长");
        }
        if (content.getContent().length() > AppConst.MAX_TEXT_COUNT) {
            throw new ServiceException("文章内容过长");
        }
        if (null == content.getAuthorId()) {
            throw new ServiceException("请登录后发布文章");
        }
        if (StringUtils.isNotBlank(content.getSlug())) {
            if (content.getSlug().length() < 5) {
                throw new ServiceException("路径不能少于 5 个字符");
            }
            if (!TaleUtils.isPath(content.getSlug())) {
                throw new ServiceException("您输入的路径不合法");
            }
            Example contentVoExample = new Example(Content.class);
            contentVoExample.selectProperties("type");
            contentVoExample.createCriteria()
                    .andEqualTo("type", content.getType())
                    .andEqualTo("slug", content.getSlug());
            int count = mapper.selectCountByExample(contentVoExample);
            if (count > 0) throw new ServiceException("该路径已经存在，请重新输入");
        } else {
            content.setSlug(null);
        }

        // 保存文章
        int time = DateKit.getCurrentUnixTime();
        content.setContent(EmojiParser.parseToAliases(content.getContent()));
        content.setCreated(time);
        content.setModified(time);
        content.setHits(0);
        content.setCommentsNum(0);
        super.save(content);

        // 保存标签与分类
        String tags = content.getTags();
        String categories = content.getCategories();
        saveMetas(content.getCid(), content.getTags(), Types.TAG.getType());
        saveMetas(content.getCid(), content.getCategories(), Types.CATEGORY.getType());
    }

    @Override
    public Content findById(String id) {
        Content content = null;

        // 如果有 slug 属性，通过 slug 查询，否则通过 ID 查询
        if (!NumberUtils.isDigits(id)) {
            content = new Content();
            content.setSlug(id);
            content = mapper.selectOne(content);
        } else {
            content = super.findById(id);
        }

        // 更新文章点击数量
        updateArticleHit(content);
        return content;
    }

    @Override
    public PageInfo<Content> getArticles(Integer mid, int page, int limit) {
        PageHelper.startPage(page, limit);
        List<Content> list = contentMapper.selectByCategory(mid);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Content> getArticles(String keyword, Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        Example contentVoExample = new Example(Content.class);
        contentVoExample.setOrderByClause("created desc");
        contentVoExample.createCriteria()
                .andEqualTo("type", "post")
                .andEqualTo("status", "publish")
                .andLike("title", "%" + keyword + "%");
        List<Content> contents = mapper.selectByExample(contentVoExample);
        return new PageInfo<>(contents);
    }

    @Override
    public void deleteById(String cid) {
        Content content = findById(cid);
        if (content == null) {
            throw new ServiceException("文章不存在！");
        }

        // 从主表中删除
        super.deleteById(cid);

        // 从中间表与评论表中删除
        Example example = new Example(Relationship.class);
        example.createCriteria().andEqualTo("cid", cid);
        List<Relationship> relationships = relationshipMapper.selectByExample(example);
        if (!relationships.isEmpty()) {
            relationshipMapper.deleteByExample(example);
        }
        List<Comment> comments = commentMapper.selectByExample(example);
        if (!comments.isEmpty()) {
            commentMapper.deleteByExample(example);
        }
    }

    @Override
    public void update(Content content) {
        if (null == content || null == content.getCid()) {
            throw new ServiceException("文章对象不能为空");
        }
        if (StringUtils.isBlank(content.getTitle())) {
            throw new ServiceException("文章标题不能为空");
        }
        if (StringUtils.isBlank(content.getContent())) {
            throw new ServiceException("文章内容不能为空");
        }
        if (content.getTitle().length() > 200) {
            throw new ServiceException("文章标题过长");
        }
        if (content.getContent().length() > 65000) {
            throw new ServiceException("文章内容过长");
        }
        if (null == content.getAuthorId()) {
            throw new ServiceException("请登录后发布文章");
        }
        if (StringUtils.isBlank(content.getSlug())) {
            content.setSlug(null);
        } else {
            if (content.getSlug().length() < 5) {
                throw new ServiceException("路径不能少于 5 个字符");
            }
            if (!TaleUtils.isPath(content.getSlug())) {
                throw new ServiceException("您输入的路径不合法");
            }
        }

        // 参数更新
        content.setModified(DateKit.getCurrentUnixTime());
        content.setContent(EmojiParser.parseToAliases(content.getContent()));
        super.update(content);

        // 从标签分类中间表中删除记录
        Integer cid = content.getCid();
        Example example = new Example(Relationship.class);
        example.createCriteria().andEqualTo("cid", cid);
        List<Relationship> relationships = relationshipMapper.selectByExample(example);
        if (!relationships.isEmpty()) {
            relationshipMapper.deleteByExample(example);
        }

        // 保存标签与分类
        saveMetas(cid, content.getTags(), Types.TAG.getType());
        saveMetas(cid, content.getCategories(), Types.CATEGORY.getType());
    }

    private void saveMetas(Integer cid, String name, String type) {
        String[] split = name.split(",");
        for (String s : split) {
            Meta meta = new Meta();
            meta.setType(type);
            meta.setName(s);
            Meta metaR = metaMapper.selectOne(meta);
            if (metaR == null) {
                metaMapper.insert(meta);
            } else {
                meta = metaR;
            }
            Relationship relationships = new Relationship();
            relationships.setCid(cid);
            relationships.setMid(meta.getMid());
            relationshipMapper.insert(relationships);
        }
    }

    private void updateArticleHit(Content content) {
        Content temp = new Content();
        temp.setCid(content.getCid());
        temp.setHits(content.getHits() + 1);
        mapper.updateByPrimaryKeySelective(temp);
    }

}
