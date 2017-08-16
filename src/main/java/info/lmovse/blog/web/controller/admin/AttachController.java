package info.lmovse.blog.web.controller.admin;

import com.github.pagehelper.PageInfo;
import info.lmovse.blog.constant.AppConst;
import info.lmovse.blog.pojo.bo.RestResponse;
import info.lmovse.blog.pojo.dto.LogActions;
import info.lmovse.blog.pojo.dto.Types;
import info.lmovse.blog.pojo.po.Attach;
import info.lmovse.blog.pojo.po.User;
import info.lmovse.blog.service.IAttachService;
import info.lmovse.blog.service.ILogService;
import info.lmovse.blog.util.Commons;
import info.lmovse.blog.util.DateKit;
import info.lmovse.blog.util.TaleUtils;
import info.lmovse.blog.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 附件管理
 * <p>
 * Created by 13 on 2017/2/21.
 */
@Controller
@RequestMapping("admin/attach")
public class AttachController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachController.class);

    public static final String CLASSPATH = TaleUtils.getUplodFilePath();

    @Resource
    private IAttachService attachService;

    @Resource
    private ILogService logService;

    /**
     * 附件页面
     *
     * @param model
     * @param page
     * @param limit
     * @return
     */
    @GetMapping(value = "")
    public String index(Model model,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = AppConst.PAGE_SIZE_STRING) int limit) {
        Example example = new Example(Attach.class);
        example.setOrderByClause("id desc");
        PageInfo<Attach> attachPaginator = attachService.findPageByExample(example, page, limit);
        model.addAttribute("attachs", attachPaginator);
        model.addAttribute(Types.ATTACH_URL.getType(), Commons.site_option(Types.ATTACH_URL.getType(), Commons.site_url()));
        model.addAttribute("max_file_size", AppConst.MAX_FILE_SIZE / 1024);
        return "admin/attach";
    }

    /**
     * 上传文件接口
     *
     * @param request
     * @return
     */
    @PostMapping(value = "upload")
    @ResponseBody
    public RestResponse upload(HttpServletRequest request, MultipartFile[] file) throws IOException {
        User users = this.getUser(request);
        Integer uid = users.getUid();
        List<String> errorFiles = new ArrayList<>();
        try {
            for (MultipartFile multipartFile : file) {
                Attach attach = new Attach();
                String fname = multipartFile.getOriginalFilename();
                if (multipartFile.getSize() > AppConst.MAX_FILE_SIZE) {
                    errorFiles.add(fname);
                }
                String fkey = TaleUtils.getFileKey(fname);
                String ftype = TaleUtils.isImage(multipartFile.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType();
                File fileItem = new File(CLASSPATH + fkey);
                try {
                    FileCopyUtils.copy(multipartFile.getInputStream(), new FileOutputStream(fileItem));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                attach.setFname(fname);
                attach.setFkey(fkey);
                attach.setFtype(ftype);
                attach.setAuthorId(uid);
                attach.setCreated(DateKit.getCurrentUnixTime());
                attachService.save(attach);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return RestResponse.fail();
        }
        return RestResponse.ok(errorFiles);
    }

    @RequestMapping(value = "delete")
    @ResponseBody
    public RestResponse delete(String id, HttpServletRequest request) {
        try {
            Attach attach = attachService.findById(id);
            if (null == attach) {
                return RestResponse.fail("不存在该附件");
            }
            attachService.deleteById(id);
            File file = new File(CLASSPATH + attach.getFkey());
            if (file.exists()) {
                file.delete();
            }
            logService.insertLog(LogActions.DEL_ARTICLE.getAction(), attach.getFkey(), request.getRemoteAddr(), this.getUid(request));
        } catch (Exception e) {
            String msg = "附件删除失败";
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

}
