package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TRadSite;

import java.util.List;

/**
 * 辐射工作场所档案 Service接口
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface ITRadSiteService {

    /** 按主键查询 */
    TRadSite selectTRadSiteById(Long id);

    /** 按条件查询列表（分页由调用方统一处理） */
    List<TRadSite> selectTRadSiteList(Wrapper<TRadSite> queryWrapper);

    /** 新增（旧签名，保留） */
    int insertTRadSite(TRadSite record);

    /** 修改（旧签名，保留） */
    int updateTRadSite(TRadSite record);

    /** 批量删除（旧签名，保留） */
    int deleteTRadSiteByIds(String ids);

    /** 按主键删除（旧签名，保留） */
    int deleteTRadSiteById(Long id);

    /**
     * 登记建档：编号人工填写，按编号规则校验格式且不可与在用档案重复（重复不收）。
     * 重载新增，区别于旧的 insertTRadSite。
     */
    TRadSite registerSite(TRadSite record);

    /**
     * 停用场所：停用后不出现在新单的可选场所里，
     * 但老预警单挂着的场所关联不动、历史照查。
     */
    int disableSite(Long id);

    /** 在用（未删除、未停用）场所，供新单据下拉选择 */
    List<TRadSite> selectActiveSites();

    /** 按编号规则给出下一个建议编号（建档时人工填，可改） */
    String nextSiteNo();
}
