package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TRadAlarmBill;
import com.fc.v2.model.auto.TRadSite;
import com.fc.v2.service.ITRadAlarmBillService;
import com.fc.v2.service.ITRadSiteService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 剂量预警单 Controller。
 * 入口只有「登记」：一张预警单对应一次剂量预警，单号人工录，
 * 档位与处理状态都由系统定，页面只负责登记与台账查询。
 *
 * @author fuce
 * @date 2026-09-12
 */
@Api(value = "剂量预警单")
@Controller
@RequestMapping("/RadAlarmBillController")
public class RadAlarmBillController extends BaseController {

    private final String prefix = "admin/radAlarmBill";

    @Autowired
    private ITRadAlarmBillService radAlarmBillService;

    @Autowired
    private ITRadSiteService radSiteService;

    @ApiOperation(value = "台账跳转", notes = "台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("rad:radAlarmBill:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "剂量预警单台账查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "分页查询：可按场所、处理状态、单号筛选")
    @GetMapping("/list")
    @RequiresPermissions("rad:radAlarmBill:list")
    @ResponseBody
    public ResultTable list(@RequestParam(value = "siteId", required = false) Long siteId,
                            @RequestParam(value = "status", required = false) Integer status,
                            @RequestParam(value = "billNo", required = false) String billNo) {
        startPage();
        PageInfo<com.fc.v2.model.custom.RadAlarmBillVo> page =
                new PageInfo<com.fc.v2.model.custom.RadAlarmBillVo>(
                        radAlarmBillService.selectAlarmBillList(siteId, status, billNo));
        return pageTable(page.getList(), page.getTotal());
    }

    @ApiOperation(value = "登记跳转", notes = "登记跳转")
    @GetMapping("/add")
    @RequiresPermissions("rad:radAlarmBill:add")
    public String add(ModelMap modelMap) {
        // 新单只允许选在用场所；停用场所不在下拉里
        modelMap.put("sites", radSiteService.selectActiveSites());
        return prefix + "/add";
    }

    @Log(title = "剂量预警单登记", action = "add")
    @ApiOperation(value = "登记", notes = "登记：单号手工录，档位系统折，状态系统推")
    @PostMapping("/add")
    @RequiresPermissions("rad:radAlarmBill:add")
    @ResponseBody
    public AjaxResult add(TRadAlarmBill record) {
        return toAjax(radAlarmBillService.registerAlarmBill(record) != null ? 1 : 0);
    }

    @Log(title = "剂量预警单修改", action = "edit")
    @ApiOperation(value = "修改保存", notes = "修改保存")
    @PostMapping("/edit")
    @RequiresPermissions("rad:radAlarmBill:edit")
    @ResponseBody
    public AjaxResult editSave(TRadAlarmBill record) {
        return toAjax(radAlarmBillService.updateTRadAlarmBill(record));
    }

    @Log(title = "剂量预警单删除", action = "remove")
    @ApiOperation(value = "删除", notes = "删除")
    @DeleteMapping("/remove")
    @RequiresPermissions("rad:radAlarmBill:remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(radAlarmBillService.deleteTRadAlarmBillByIds(ids));
    }

    /** 台账筛选下拉：全部正常档案场所（含停用，否则历史单没法按场所筛） */
    @GetMapping("/siteOptions")
    @ResponseBody
    public AjaxResult siteOptions() {
        return AjaxResult.successData(200, radSiteService.selectTRadSiteList(
                new QueryWrapper<TRadSite>().eq("del_flag", 0).orderByAsc("site_no")));
    }
}
