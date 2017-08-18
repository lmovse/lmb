package info.lmovse.blog.core.service;

import info.lmovse.blog.base.service.IService;
import info.lmovse.blog.core.pojo.dto.MetaDto;
import info.lmovse.blog.core.pojo.po.Meta;

import java.util.List;

/**
 * 分类信息service接口
 * Created by BlueT on 2017/3/17.
 */
public interface IMetaService extends IService<Meta> {
    /**
     * 根据文章 id 获取项目个数
     * @param mid
     * @return
     */
    Integer countMeta(Integer mid);

    /**
     * 根据类型查询项目列表
     * @param types
     * @return
     */
    List<Meta> getMetas(String types);

    /**
     * 获取显示页面显示对象
     * @param type
     * @return
     */
    List<MetaDto> getMetaDto(String type);

}
