package info.lmovse.blog.web.controller.admin;

import info.lmovse.blog.pojo.bo.RestResponse;
import info.lmovse.blog.pojo.dto.Types;
import info.lmovse.blog.pojo.po.Meta;
import info.lmovse.blog.service.IMetaService;
import info.lmovse.blog.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by 13 on 2017/2/21.
 */
@Controller
@RequestMapping("admin/links")
public class LinksController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinksController.class);

    @Resource
    private IMetaService metasService;

    @GetMapping("")
    public String index(HttpServletRequest request) {
        List<Meta> metas = metasService.getMetas(Types.LINK.getType());
        request.setAttribute("links", metas);
        return "admin/links";
    }

    @PostMapping(value = "save")
    @ResponseBody
    public RestResponse saveLink(String title, String url, String logo, Integer mid,
                                 @RequestParam(value = "sort", defaultValue = "0") int sort) {
        try {
            Meta metas = new Meta();
            metas.setName(title);
            metas.setSlug(url);
            metas.setDescription(logo);
            metas.setSort(sort);
            metas.setType(Types.LINK.getType());
            if (null != mid) {
                metas.setMid(mid);
                metasService.update(metas);
            } else {
                metasService.save(metas);
            }
        } catch (Exception e) {
            String msg = "友链保存失败";
            LOGGER.error(msg, e);
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

    @RequestMapping("delete")
    @ResponseBody
    public RestResponse delete(String mid) {
        try {
            metasService.deleteById(mid);
        } catch (Exception e) {
            String msg = "友链删除失败";
            LOGGER.error(msg, e);
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

}
