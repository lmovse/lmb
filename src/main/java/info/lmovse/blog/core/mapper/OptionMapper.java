package info.lmovse.blog.core.mapper;

import info.lmovse.blog.base.mapper.AppMapper;
import info.lmovse.blog.core.pojo.po.Option;

import java.util.List;

public interface OptionMapper extends AppMapper<Option> {

    /**
     * 批量保存
     * @param options list
     * @return 保存成功的个数
     */
    int insertOptions(List<Option> options);

}