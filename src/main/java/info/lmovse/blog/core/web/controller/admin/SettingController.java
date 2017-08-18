package info.lmovse.blog.core.web.controller.admin;

import com.alibaba.fastjson.JSON;
import info.lmovse.blog.core.constant.AppConst;
import info.lmovse.blog.core.pojo.bo.BackResponse;
import info.lmovse.blog.core.pojo.bo.RestResponse;
import info.lmovse.blog.core.pojo.dto.LogActions;
import info.lmovse.blog.core.pojo.po.Option;
import info.lmovse.blog.core.service.ILogService;
import info.lmovse.blog.core.service.IOptionService;
import info.lmovse.blog.core.service.ISiteService;
import info.lmovse.blog.base.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangq on 2017/3/20.
 */
@Controller
@RequestMapping("/admin/setting")
public class SettingController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingController.class);

    @Resource
    private IOptionService optionService;

    @Resource
    private ILogService logService;

    @Resource
    private ISiteService siteService;

    /**
     * 系统设置
     */
    @GetMapping(value = "")
    public String setting(Model model) {
        List<Option> voList = optionService.findAll();
        Map<String, String> options = new HashMap<>();
        voList.forEach((option) -> options.put(option.getName(), option.getValue()));
        options.putIfAbsent("site_record", "");
        model.addAttribute("options", options);
        return "admin/setting";
    }

    /**
     * 保存系统设置
     */
    @PostMapping("")
    @ResponseBody
    public RestResponse saveSetting(@RequestParam(required = false) String site_theme,
                                    HttpServletRequest request) {
        try {
            // 保存设置
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, String> configs = new HashMap<>();
            parameterMap.forEach((s, strings) -> {
                Option option = new Option();
                option.setName(s);
                option.setValue(StringUtils.join(strings, ","));
                optionService.save(option);
                configs.put(s, StringUtils.join(strings, ","));
            });

            // 将设置保存到缓存中
            AppConst.initConfig = configs;
            if (StringUtils.isNotBlank(site_theme)) {
                BaseController.THEME_PREFIX = "themes/" + site_theme + "/";
            }
            logService.insertLog(LogActions.SYS_SETTING.getAction(), JSON.toJSONString(configs), request.getRemoteAddr(), this.getUid(request));
            return RestResponse.ok();
        } catch (Exception e) {
            String msg = "保存设置失败";
            LOGGER.error(e.getMessage());
            return RestResponse.fail(msg);
        }
    }

    /**
     * 系统备份
     *
     * @return
     */
    @PostMapping(value = "backup")
    @ResponseBody
    public RestResponse backup(@RequestParam String bk_type, @RequestParam String bk_path,
                               HttpServletRequest request) {
        if (StringUtils.isBlank(bk_type)) {
            return RestResponse.fail("请确认信息输入完整");
        }
        try {
            BackResponse backResponse = siteService.backup(bk_type, bk_path, "yyyyMMddHHmm");
            logService.insertLog(LogActions.SYS_BACKUP.getAction(), null, request.getRemoteAddr(), this.getUid(request));
            return RestResponse.ok(backResponse);
        } catch (Exception e) {
            String msg = "备份失败";
            LOGGER.error(msg, e);
            return RestResponse.fail(msg);
        }
    }

}
