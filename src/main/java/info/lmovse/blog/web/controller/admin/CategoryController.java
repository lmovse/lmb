package info.lmovse.blog.web.controller.admin;

import info.lmovse.blog.pojo.bo.RestResponse;
import info.lmovse.blog.pojo.dto.MetaDto;
import info.lmovse.blog.pojo.dto.Types;
import info.lmovse.blog.pojo.po.Meta;
import info.lmovse.blog.service.IMetaService;
import info.lmovse.blog.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by 13 on 2017/2/21.
 */
@Controller
@RequestMapping("admin/category")
public class CategoryController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);

    @Resource
    private IMetaService metasService;

    @GetMapping(value = "")
    public String index(Model model) {
        List<MetaDto> categories = metasService.getMetaDto(Types.CATEGORY.getType());
        List<MetaDto> tags = metasService.getMetaDto(Types.TAG.getType());
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        return "admin/category";
    }

    @PostMapping("save")
    @ResponseBody
    public RestResponse saveCategory(Meta meta) {
        try {
            meta.setType(Types.CATEGORY.getType());
            metasService.save(meta);
        } catch (Exception e) {
            String msg = "分类保存失败";
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
            String msg = "删除失败";
            return RestResponse.fail(msg);
        }
        return RestResponse.ok();
    }

}
