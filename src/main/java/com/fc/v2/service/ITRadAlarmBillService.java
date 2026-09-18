package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TRadAlarmBill;

import java.util.List;

/**
 * 剂量预警单 Service接口
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface ITRadAlarmBillService {

    /** 按主键查询 */
    TRadAlarmBill selectTRadAlarmBillById(Long id);

    /** 按条件查询列表（分页由调用方统一处理） */
    List<TRadAlarmBill> selectTRadAlarmBillList(Wrapper<TRadAlarmBill> queryWrapper);

    /** 按单据条件查询列表（场所/状态筛选，分页由调用方统一处理） */
    List<TRadAlarmBill> selectTRadAlarmBillList(TRadAlarmBill record);

    /** 新增 */
    int insertTRadAlarmBill(TRadAlarmBill record);

    /**
     * 登记（预警单只从登记进）：单号手工录、校验归服务层，
     * 档位由系统按剂量率折算、状态由系统置为待处理，createBy 为登记人
     */
    int insertTRadAlarmBill(TRadAlarmBill record, String createBy);

    /** 修改 */
    int updateTRadAlarmBill(TRadAlarmBill record);

    /** 批量删除 */
    int deleteTRadAlarmBillByIds(String ids);

    /** 按主键删除 */
    int deleteTRadAlarmBillById(Long id);
}
