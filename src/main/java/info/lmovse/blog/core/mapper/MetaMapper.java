package info.lmovse.blog.core.mapper;

import info.lmovse.blog.base.mapper.AppMapper;
import info.lmovse.blog.core.pojo.dto.MetaDto;
import info.lmovse.blog.core.pojo.po.Meta;

import java.util.List;

public interface MetaMapper extends AppMapper<Meta> {

    /**
     * 查询标签或种类的数量以及 list 集合
     *
     * @param type
     * @return 0 个或多个包含 metaDto 的集合
     **/
    List<MetaDto> selectMetaDto(String type);

}