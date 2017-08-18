package info.lmovse.blog.core.service.impl;

import info.lmovse.blog.base.service.impl.AbstractServiceImpl;
import info.lmovse.blog.core.exception.ServiceException;
import info.lmovse.blog.core.mapper.ContentMapper;
import info.lmovse.blog.core.mapper.MetaMapper;
import info.lmovse.blog.core.mapper.RelationshipMapper;
import info.lmovse.blog.core.pojo.dto.MetaDto;
import info.lmovse.blog.core.pojo.dto.Types;
import info.lmovse.blog.core.pojo.po.Content;
import info.lmovse.blog.core.pojo.po.Meta;
import info.lmovse.blog.core.pojo.po.Relationship;
import info.lmovse.blog.core.service.IMetaService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * Created by BlueT on 2017/3/17.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MetaServiceImpl extends AbstractServiceImpl<Meta> implements IMetaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaServiceImpl.class);

    @Resource
    private MetaMapper metaMapper;

    @Resource
    private RelationshipMapper relationshipMapper;

    @Resource
    private ContentMapper contentMapper;

    @Override
    public Integer countMeta(Integer mid) {
        return mapper.selectCountByExample(mid);
    }

    @Override
    public List<Meta> getMetas(String types) {
        if (StringUtils.isBlank(types)) {
            return null;
        }
        Example metaVoExample = new Example(Meta.class);
        metaVoExample.setOrderByClause("sort desc, mid desc");
        metaVoExample.createCriteria().andEqualTo("type", types);
        return findByExample(metaVoExample);
    }

    @Override
    public List<MetaDto> getMetaDto(String type) {
        if (type == null) {
            throw new ServiceException("必要参数不能为空！");
        }
        return metaMapper.selectMetaDto(type);
    }

    @Override
    public void deleteById(String mid) {
        // 从主表中删除
        Meta metas = findById(mid);
        if (metas == null) {
            throw new ServiceException("不存在的标签或者分类！");
        }
        super.deleteById(mid);

        // 从中间表与文章中删除
        String type = metas.getType();
        String name = metas.getName();
        Example example = new Example(Relationship.class);
        example.createCriteria().andEqualTo("mid", mid);
        List<Relationship> rlist = relationshipMapper.selectByExample(example);
        if (rlist == null) {
            return;
        }
        for (Relationship r : rlist) {
            Content contents = contentMapper.selectByPrimaryKey(r.getCid());
            if (null != contents) {
                // 更新文章中的标签？分类信息
                Content temp = new Content();
                temp.setCid(r.getCid());
                if (type.equals(Types.CATEGORY.getType())) {
                    temp.setCategories(contents.getCategories().replace(name + ",", ""));
                }
                if (type.equals(Types.TAG.getType())) {
                    temp.setTags(contents.getTags().replace(name + ",", ""));
                }
                contentMapper.updateByPrimaryKeySelective(temp);
            }
        }
        relationshipMapper.deleteByPrimaryKey(mid);
    }

    @Override
    public void save(Meta meta) {
        String name = meta.getName();
        String type = meta.getType();
        if (StringUtils.isBlank(name) || StringUtils.isBlank(type)) {
            throw new ServiceException("必要参数不能为空！");
        }
        Example metaVoExample = new Example(Meta.class);
        metaVoExample.createCriteria()
                .andEqualTo("type", type)
                .andEqualTo("name", name);
        List<Meta> metaVos = mapper.selectByExample(metaVoExample);
        if (metaVos.size() != 0) {
            throw new ServiceException("名称已存在！请更换");
        }

        // 有 ID ，更新 meta，无 ID，新增 meta
        Integer mid = meta.getMid();
        if (null != mid) {
            Meta original = findById(String.valueOf(mid));

            // 如果是标签或者分类，则更新原有文章的标签或分类
            if (Objects.equals(original.getType(), Types.CATEGORY.getType())
                    || Objects.equals(original.getType(), Types.TAG.getType())) {
                Example example = new Example(Content.class);
                Content content = new Content();
                if (Types.CATEGORY.getType().equals(meta.getType())) {
                    example.createCriteria().andLike("categories", "%" + original.getName() + "%");
                    example.selectProperties("categories");
                    List<Content> contents = contentMapper.selectByExample(example);
                    for (Content contentU : contents) {
                        contentU.setCategories(contentU.getCategories().replace(original.getName(), meta.getName()));
                        contentMapper.updateByExampleSelective(contentU, example);
                    }
                } else {
                    example.createCriteria().andLike("tags", "%" + original.getName() + "%");
                    example.selectProperties("tags");
                    List<Content> contents = contentMapper.selectByExample(example);
                    for (Content contentU : contents) {
                        contentU.setTags(contentU.getTags().replace(original.getName(), meta.getName()));
                        contentMapper.updateByExampleSelective(contentU, example);
                    }
                }
            }
            super.update(meta);
        } else {
            super.save(meta);
        }
    }

}
