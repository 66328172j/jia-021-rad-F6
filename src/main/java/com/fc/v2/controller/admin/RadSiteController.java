package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TRadSite;
import com.fc.v2.service.ITRadSiteService;
import com.fc.v2.util.StringUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 辐射工作场所档案 Controller。
 * 入口只有「登记」：建档、停用、台账查询都在这一个口子。
 *
 * @author fuce
 * @date 2026-09-12
 */
@Api(value = "辐射工作场所档案")
@Controller
@RequestMapping("/RadSiteController")
public class RadSiteController extends BaseController {

    private final String prefix = "admin/radSite";

    @Autowired
    private ITRadSiteService radSiteService;

    @ApiOperation(value = "台账跳转", notes = "台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("rad:radSite:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "场所档案台账查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @PostMapping("/list")
    @RequiresPermissions("rad:radSite:list")
    @ResponseBody
    public ResultTable list(TRadSite record) {
        QueryWrapper<TRadSite> queryWrapper = new QueryWrapper<TRadSite>();
        queryWrapper.eq("del_flag", 0);
        queryWrapper.like(StringUtils.isNotEmpty(record.getSiteNo()), "site_no", record.getSiteNo());
        queryWrapper.like(StringUtils.isNotEmpty(record.getSiteName()), "site_name", record.getSiteName());
        queryWrapper.eq(StringUtils.isNotEmpty(record.getAreaName()), "area_name", record.getAreaName());
        queryWrapper.eq(StringUtils.isNotNull(record.getStatus()), "status", record.getStatus());
        queryWrapper.orderByAsc("site_no");

        startPage();
        PageInfo<TRadSite> page = new PageInfo<TRadSite>(radSiteService.selectTRadSiteList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @ApiOperation(value = "登记跳转", notes = "登记跳转")
    @GetMapping("/register")
    @RequiresPermissions("rad:radSite:add")
    public String register(ModelMap modelMap) {
        modelMap.put("suggestSiteNo", radSiteService.nextSiteNo());
        return prefix + "/register";
    }

    @Log(title = "场所登记", action = "register")
    @ApiOperation(value = "登记建档", notes = "登记建档：编号人工填写，重复不收")
    @PostMapping("/register")
    @RequiresPermissions("rad:radSite:add")
    @ResponseBody
    public AjaxResult doRegister(TRadSite record) {
        return toAjax(radSiteService.registerSite(record) != null ? 1 : 0);
    }

    @Log(title = "场所停用", action = "disable")
    @ApiOperation(value = "停用", notes = "停用后新单不可选，老单关联保留")
    @PutMapping("/disable")
    @RequiresPermissions("rad:radSite:disable")
    @ResponseBody
    public AjaxResult disable(Long id) {
        return toAjax(radSiteService.disableSite(id));
    }

    @ApiOperation(value = "修改跳转", notes = "修改跳转")
    @GetMapping("/edit/{id}")
    @RequiresPermissions("rad:radSite:edit")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        mmap.put("RadSite", radSiteService.selectTRadSiteById(id));
        return prefix + "/edit";
    }

    @Log(title = "场所档案修改", action = "edit")
    @ApiOperation(value = "修改保存", notes = "修改保存")
    @PostMapping("/edit")
    @RequiresPermissions("rad:radSite:edit")
    @ResponseBody
    public AjaxResult editSave(TRadSite record) {
        return toAjax(radSiteService.updateTRadSite(record));
    }
}
