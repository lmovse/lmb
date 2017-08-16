package info.lmovse.blog.service.impl;

import info.lmovse.blog.pojo.po.Option;
import info.lmovse.blog.service.IOptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * options表的service
 * Created by BlueT on 2017/3/7.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OptionServiceImpl extends AbstractServiceImpl<Option> implements IOptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptionServiceImpl.class);

    @Override
    public void save(Option option) {
        if (findById(option.getName()) != null) {
            super.update(option);
            LOGGER.debug("更新 {} 成功", option.getName());
        } else {
            super.save(option);
            LOGGER.debug("保存 {} 成功", option.getName());
        }
    }

}
