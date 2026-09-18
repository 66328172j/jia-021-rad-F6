package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TRadAlarmBill;
import com.fc.v2.model.custom.RadAlarmBillVo;

import java.util.List;

/**
 * 剂量预警单 Service接口
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface ITRadAlarmBillService {

    /** 按主键查询（旧签名，保留） */
    TRadAlarmBill selectTRadAlarmBillById(Long id);

    /** 按条件查询列表（旧签名，保留；分页由调用方统一处理） */
    List<TRadAlarmBill> selectTRadAlarmBillList(Wrapper<TRadAlarmBill> queryWrapper);

    /** 新增（旧签名，保留） */
    int insertTRadAlarmBill(TRadAlarmBill record);

    /** 修改（旧签名，保留） */
    int updateTRadAlarmBill(TRadAlarmBill record);

    /** 批量删除（旧签名，保留） */
    int deleteTRadAlarmBillByIds(String ids);

    /** 按主键删除（旧签名，保留） */
    int deleteTRadAlarmBillById(Long id);

    /**
     * 登记一张预警单（一事一张，新增用重载）。
     * 单号人工录入；剂量率校验在服务层；预警档位由系统按当前生效的
     * 剂量率判定规则折算，不让人算；处理状态由系统推为「待处理」。
     *
     * @return 登记成功后的单据（认服务返回）
     */
    TRadAlarmBill registerAlarmBill(TRadAlarmBill record);

    /**
     * 台账查询（重载）：关联场所编号/名称/分区，可按场所、处理状态筛选。
     */
    List<RadAlarmBillVo> selectAlarmBillList(Long siteId, Integer status, String billNo);
}
