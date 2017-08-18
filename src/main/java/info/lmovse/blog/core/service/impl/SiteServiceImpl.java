package info.lmovse.blog.core.service.impl;

import com.github.pagehelper.PageHelper;
import info.lmovse.blog.core.exception.ServiceException;
import info.lmovse.blog.core.mapper.AttachMapper;
import info.lmovse.blog.core.mapper.CommentMapper;
import info.lmovse.blog.core.mapper.ContentMapper;
import info.lmovse.blog.core.mapper.MetaMapper;
import info.lmovse.blog.core.pojo.bo.Archive;
import info.lmovse.blog.core.pojo.bo.BackResponse;
import info.lmovse.blog.core.pojo.bo.Statistics;
import info.lmovse.blog.core.pojo.dto.MetaDto;
import info.lmovse.blog.core.pojo.dto.Types;
import info.lmovse.blog.core.pojo.po.Attach;
import info.lmovse.blog.core.pojo.po.Comment;
import info.lmovse.blog.core.pojo.po.Content;
import info.lmovse.blog.core.pojo.po.Meta;
import info.lmovse.blog.core.service.ISiteService;
import info.lmovse.blog.core.util.DateKit;
import info.lmovse.blog.core.util.TaleUtils;
import info.lmovse.blog.core.util.ZipUtils;
import info.lmovse.blog.core.util.backup.Backup;
import info.lmovse.blog.core.web.controller.admin.AttachController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lmovse on 2017/3/7.
 * Tomorrow is a nice day.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SiteServiceImpl implements ISiteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteServiceImpl.class);

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private AttachMapper attachMapper;

    @Resource
    private MetaMapper metaMapper;

    @Override
    public List<Comment> recentComments(int limit) {
        if (limit < 0 || limit > 10) {
            limit = 10;
        }
        PageHelper.startPage(1, limit);
        Example example = new Example(Comment.class);
        example.setOrderByClause("created desc");
        return commentMapper.selectByExample(example);
    }

    @Override
    public List<Content> recentContents(int limit) {
        if (limit < 0 || limit > 10) {
            limit = 10;
        }
        PageHelper.startPage(1, limit);
        Example example = new Example(Content.class);
        example.setOrderByClause("created desc");
        example.createCriteria()
                .andEqualTo("status", Types.PUBLISH.getType())
                .andEqualTo("type", Types.ARTICLE.getType());
        return contentMapper.selectByExample(example);
    }

    @Override
    public BackResponse backup(String type, String path, String fmt) throws Exception {
        BackResponse backResponse = new BackResponse();
        if (type.equals("attach")) {
            if (StringUtils.isBlank(path)) {
                throw new ServiceException("请输入备份文件存储路径");
            }
            if (!(new File(path)).isDirectory()) {
                throw new ServiceException("请输入一个存在的目录");
            }
            String bkAttachDir = AttachController.CLASSPATH + "upload";
            String bkThemesDir = AttachController.CLASSPATH + "templates/themes";

            String fname = DateKit.dateFormat(new Date(), fmt) + "_" + TaleUtils.getRandomNumber(5) + ".zip";

            String attachPath = path + "/" + "attachs_" + fname;
            String themesPath = path + "/" + "themes_" + fname;

            ZipUtils.zipFolder(bkAttachDir, attachPath);
            ZipUtils.zipFolder(bkThemesDir, themesPath);

            backResponse.setAttachPath(attachPath);
            backResponse.setThemePath(themesPath);
        }
        if (type.equals("db")) {
            String bkAttachDir = AttachController.CLASSPATH + "upload/";
            if (!(new File(bkAttachDir)).isDirectory()) {
                File file = new File(bkAttachDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
            }
            String sqlFileName = "tale_" + DateKit.dateFormat(new Date(), fmt) + "_" + TaleUtils.getRandomNumber(5) + ".sql";
            String zipFile = sqlFileName.replace(".sql", ".zip");

            Backup backup = new Backup(TaleUtils.getNewDataSource().getConnection());
            String sqlContent = backup.execute();

            File sqlFile = new File(bkAttachDir + sqlFileName);
            write(sqlContent, sqlFile, Charset.forName("UTF-8"));

            String zip = bkAttachDir + zipFile;
            ZipUtils.zipFile(sqlFile.getPath(), zip);

            if (!sqlFile.exists()) {
                throw new ServiceException("数据库备份失败");
            }
            sqlFile.delete();

            backResponse.setSqlPath(zipFile);

            // 10 秒后删除备份文件
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    new File(zip).delete();
                }
            }, 10 * 1000);
        }
        return backResponse;
    }

    @Override
    public Comment getComment(Integer coid) {
        if (null != coid) {
            return commentMapper.selectByPrimaryKey(coid);
        }
        return null;
    }

    @Override
    public int getArticleIndex(Integer cid, String direct) {
        List<Integer> integers = contentMapper.selectIds();
        int index = integers.indexOf(cid);
        if ("prev".equals(direct)) {
            if (index != 0) {
                return integers.get(index - 1);
            }
        }
        if ("next".equals(direct)) {
            if (index != integers.size() - 1) {
                return integers.get(index + 1);
            }
        }
        return cid;
    }

    @Override
    public List<Archive> getArchives() {
        return contentMapper.findArchives();
    }

    @Override
    public Statistics getStatistics() {
        Statistics statistics = new Statistics();
        Example contentVoExample = new Example(Content.class);
        contentVoExample.createCriteria()
                .andEqualTo("type", Types.ARTICLE.getType())
                .andEqualTo("status", Types.PUBLISH.getType());

        // 文章总数
        int articles = contentMapper.selectCountByExample(contentVoExample);
        // 评论总数
        int comments = commentMapper.selectCountByExample(new Example(Comment.class));
        // 附件总数
        int attachs = attachMapper.selectCountByExample(new Example(Attach.class));

        Example metaVoExample = new Example(Meta.class);
        metaVoExample.createCriteria().andEqualTo("type", Types.LINK.getType());
        // 友链总数
        int links = metaMapper.selectCountByExample(metaVoExample);

        statistics.setArticles(articles);
        statistics.setComments(comments);
        statistics.setAttachs(attachs);
        statistics.setLinks(links);
        return statistics;
    }

    @Override
    public List<MetaDto> metas(String type, String orderBy, int limit) {
        List<MetaDto> retList = null;
        if (StringUtils.isNotBlank(type)) {
            retList = metaMapper.selectMetaDto(type);
        }
        return retList;
    }

    private void write(String data, File file, Charset charset) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(data.getBytes(charset));
        } catch (IOException var8) {
            throw new IllegalStateException(var8);
        } finally {
            if (null != os) {
                try {
                    os.close();
                } catch (IOException var2) {
                    var2.printStackTrace();
                }
            }
        }
    }

}
