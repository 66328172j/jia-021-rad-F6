package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TRadSite;

import java.util.List;

/**
 * 辐射工作场所档案 Service接口
 *
 * @author fuce
 * @date 2026-09-18
 */
public interface ITRadSiteService {

    /** 按主键查询（含停用档案：老单子上挂着的关联不能断，仅滤删除标记） */
    TRadSite selectTRadSiteById(Long id);

    /** 按条件查询列表（分页由调用方统一处理） */
    List<TRadSite> selectTRadSiteList(Wrapper<TRadSite> queryWrapper);

    /** 按档案条件查询列表（编号/名称/分区/类型/状态筛选，分页由调用方统一处理） */
    List<TRadSite> selectTRadSiteList(TRadSite record);

    /** 建档：编号人工填，填重了不收 */
    int insertTRadSite(TRadSite record);

    /** 改档/停用：编号与他人重复不收 */
    int updateTRadSite(TRadSite record);

    /** 批量删除 */
    int deleteTRadSiteByIds(String ids);

    /** 按主键删除 */
    int deleteTRadSiteById(Long id);
}
