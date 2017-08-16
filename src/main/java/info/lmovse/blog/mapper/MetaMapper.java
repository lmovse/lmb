package info.lmovse.blog.mapper;

import info.lmovse.blog.configurer.AppMapper;
import info.lmovse.blog.pojo.dto.MetaDto;
import info.lmovse.blog.pojo.po.Meta;

import org.springframework.stereotype.Component;

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