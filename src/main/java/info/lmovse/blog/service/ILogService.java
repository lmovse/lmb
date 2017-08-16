package info.lmovse.blog.service;

import info.lmovse.blog.pojo.po.Log;

/**
 * Created by BlueT on 2017/3/4.
 */
public interface ILogService extends IService<Log> {

    /**
     *  保存
     * @param action
     * @param data
     * @param ip
     * @param authorId
     */
    void insertLog(String action, String data, String ip, Integer authorId);

}
