package info.lmovse.blog.service.impl;

import info.lmovse.blog.pojo.po.Attach;
import info.lmovse.blog.service.IAttachService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by lmovse on 2017/3/20.
 * Tomorrow is a nice day.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AttachServiceImpl extends AbstractServiceImpl<Attach> implements IAttachService {}
