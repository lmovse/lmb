package info.lmovse.blog.service.impl;

import info.lmovse.blog.pojo.po.Log;
import info.lmovse.blog.service.ILogService;
import info.lmovse.blog.util.DateKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by BlueT on 2017/3/4.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LogServiceImpl extends AbstractServiceImpl<Log> implements ILogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogServiceImpl.class);

    @Override
    public void insertLog(String action, String data, String ip, Integer authorId) {
        Log logs = new Log();
        logs.setAction(action);
        logs.setData(data);
        logs.setIp(ip);
        logs.setAuthorId(authorId);
        logs.setCreated(DateKit.getCurrentUnixTime());
        mapper.insert(logs);
    }

}
