package info.lmovse.blog.core.service.impl;

import info.lmovse.blog.base.service.impl.AbstractServiceImpl;
import info.lmovse.blog.core.pojo.po.Attach;
import info.lmovse.blog.core.service.IAttachService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by lmovse on 2017/3/20.
 * Tomorrow is a nice day.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AttachServiceImpl extends AbstractServiceImpl<Attach> implements IAttachService {}
