package info.lmovse.blog.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vdurmont.emoji.EmojiParser;
import info.lmovse.blog.constant.AppConst;
import info.lmovse.blog.exception.ServiceException;
import info.lmovse.blog.mapper.CommentMapper;
import info.lmovse.blog.mapper.ContentMapper;
import info.lmovse.blog.mapper.MetaMapper;
import info.lmovse.blog.mapper.RelationshipMapper;
import info.lmovse.blog.pojo.dto.Types;
import info.lmovse.blog.pojo.po.Comment;
import info.lmovse.blog.pojo.po.Content;
import info.lmovse.blog.pojo.po.Meta;
import info.lmovse.blog.pojo.po.Relationship;
import info.lmovse.blog.service.IContentService;
import info.lmovse.blog.util.DateKit;
import info.lmovse.blog.util.TaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, Object> valueOps;

    @Override
    public void publish(Content content) {
        // 参数检测
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
        // 判断是否缓存，缓存了从缓存中读取
        Content contentR = findFromRedis(id);
        if (contentR != null) return contentR;

        Content content = null;

        // 如果有 slug 属性，通过 slug 查询，否则通过 ID 查询
        if (!NumberUtils.isDigits(id)) {
            content = new Content();
            content.setSlug(id);
            content = mapper.selectOne(content);
        } else {
            content = super.findById(id);
        }

        // 放入缓存中
        putToRedis(id, content);
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
        // 从主表中删除
        Content content = findById(cid);
        if (content == null) {
            throw new ServiceException("文章不存在！");
        }
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

        // 从缓存中删除
        deleteRedis(content);
    }

    @Override
    public void update(Content content) {
        // 参数检测
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

        // 从中间表中删除记录
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

        // 从缓存中删除
        deleteRedis(content);
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

    private void putToRedis(String id, Content content) {
        try {
            if (!NumberUtils.isDigits(id)) {
                // 当通过 slug 访问时，需要同时存储两条缓存，用于操作评论时通过 cid 查找缓存
                valueOps.set(prefixKey(content.getSlug()), content);
                valueOps.set(prefixKey(content.getCid()), content);
            } else {
                valueOps.set(prefixKey(content.getCid()), content);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("添加缓存失败，失败详情：{}", e.getMessage(), e);
        }
    }

    private Content findFromRedis(String id) {
        try {
            if (redisTemplate.hasKey(prefixKey(id))) {
                Content content = (Content) valueOps.get(prefixKey(id));
                content.setHits(content.getHits() + 1);
                if (content.getHits() % 100 == 0) {
                    updateArticleHit(content);
                }
                if (NumberUtils.isDigits(id)) {
                    valueOps.set(prefixKey(content.getCid()), content);
                } else {
                    valueOps.set(prefixKey(content.getSlug()), content);
                }
                return content;
            }
        } catch (Exception e) {
            LOGGER.error("查询缓存失败，失败详情：{}", e.getMessage(), e);
        }
        return null;
    }

    private void updateArticleHit(Content content) {
        Content temp = new Content();
        temp.setHits(content.getHits());
        mapper.updateByPrimaryKeySelective(temp);
    }

    private void deleteRedis(Content content) {
        Integer cid = content.getCid();
        try {
            if (StringUtils.isNotBlank(content.getSlug())) {
                redisTemplate.delete(prefixKey(content.getSlug()));
                redisTemplate.delete(prefixKey(cid));
            } else {
                redisTemplate.delete(prefixKey(cid));
            }
        } catch (Exception e) {
            LOGGER.error("删除缓存失败，失败详情：{}", e.getMessage(), e);
        }
    }

    private String prefixKey(Object key) {
        return "content:" + key.toString();
    }

}
