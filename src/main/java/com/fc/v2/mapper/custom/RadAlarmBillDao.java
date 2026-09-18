package com.fc.v2.mapper.custom;

import com.fc.v2.model.custom.RadAlarmBillVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 剂量预警单自定义查询：关联场所编号/名称/分区。
 *
 * @author fuce
 */
public interface RadAlarmBillDao {

    /**
     * 预警单台账（关联场所展示字段），可按场所、处理状态、单号筛选。
     * 分页由调用方 PageHelper 统一处理。
     */
    List<RadAlarmBillVo> selectAlarmBillList(@Param("siteId") Long siteId,
                                             @Param("status") Integer status,
                                             @Param("billNo") String billNo);
}
