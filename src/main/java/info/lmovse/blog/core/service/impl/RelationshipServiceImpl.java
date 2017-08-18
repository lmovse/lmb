package info.lmovse.blog.core.service.impl;

import info.lmovse.blog.base.service.impl.AbstractServiceImpl;
import info.lmovse.blog.core.mapper.RelationshipMapper;
import info.lmovse.blog.core.pojo.po.Relationship;
import info.lmovse.blog.core.service.IRelationshipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by BlueT on 2017/3/18.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RelationshipServiceImpl extends AbstractServiceImpl<Relationship> implements IRelationshipService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RelationshipServiceImpl.class);

    @Resource
    private RelationshipMapper relationshipMapper;

    @Override
    public void deleteById(Integer cid, Integer mid) {
        Example relationshipVoExample = checkParams(cid, mid);
        relationshipMapper.deleteByExample(relationshipVoExample);
    }

    @Override
    public List<Relationship> getRelationshipById(Integer cid, Integer mid) {
        Example relationshipVoExample = checkParams(cid, mid);
        return findByExample(relationshipVoExample);
    }

    @Override
    public int countById(Integer cid, Integer mid) {
        Example relationshipVoExample = checkParams(cid, mid);
        return relationshipMapper.selectCountByExample(relationshipVoExample);
    }

    private Example checkParams(Integer cid, Integer mid) {
        Example relationshipVoExample = new Example(Relationship.class);
        Example.Criteria criteria = relationshipVoExample.createCriteria();
        if (cid != null) {
            criteria.andEqualTo("cid", cid);
        }
        if (mid != null) {
            criteria.andEqualTo("mid", mid);
        }
        return relationshipVoExample;
    }

}
