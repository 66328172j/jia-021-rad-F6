package com.fc.v2.controller.admin;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TRadSite;
import com.fc.v2.service.ITRadSiteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 辐射工作场所档案 Controller
 *
 * @author fuce
 * @date 2026-09-18
 */
@Api(value = "辐射工作场所档案")
@Controller
@RequestMapping("/RadSiteController")
public class RadSiteController extends BaseController {

    private final String prefix = "admin/radSite";

    @Autowired
    private ITRadSiteService radSiteService;

    @ApiOperation(value = "分页跳转", notes = "分页跳转")
    @GetMapping("/view")
    @RequiresPermissions("rad:radSite:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "辐射工作场所档案集合查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("rad:radSite:list")
    @ResponseBody
    public ResultTable list(TRadSite record) {
        startPage();
        com.github.pagehelper.PageInfo<TRadSite> page =
                new com.github.pagehelper.PageInfo<TRadSite>(radSiteService.selectTRadSiteList(record));
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "辐射工作场所建档", action = "add")
    @ApiOperation(value = "建档", notes = "建档")
    @PostMapping("/add")
    @RequiresPermissions("rad:radSite:add")
    @ResponseBody
    public AjaxResult add(TRadSite record) {
        return toAjax(radSiteService.insertTRadSite(record));
    }

    @Log(title = "辐射工作场所改档/停用", action = "edit")
    @ApiOperation(value = "修改保存", notes = "修改保存")
    @PostMapping("/edit")
    @RequiresPermissions("rad:radSite:edit")
    @ResponseBody
    public AjaxResult editSave(TRadSite record) {
        return toAjax(radSiteService.updateTRadSite(record));
    }

    @Log(title = "辐射工作场所档案删除", action = "remove")
    @ApiOperation(value = "删除", notes = "删除")
    @DeleteMapping("/remove")
    @RequiresPermissions("rad:radSite:remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(radSiteService.deleteTRadSiteByIds(ids));
    }
}
