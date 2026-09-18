package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.mapper.auto.TRadAlarmBillMapper;
import com.fc.v2.mapper.auto.TRadDoseRuleMapper;
import com.fc.v2.mapper.auto.TRadSiteMapper;
import com.fc.v2.model.auto.TRadAlarmBill;
import com.fc.v2.model.auto.TRadDoseRule;
import com.fc.v2.model.auto.TRadSite;
import com.fc.v2.service.ITRadAlarmBillService;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 剂量预警单Service业务层处理
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TRadAlarmBillServiceImpl extends ServiceImpl<TRadAlarmBillMapper, TRadAlarmBill> implements ITRadAlarmBillService {

    /** 单据状态：待处理（登记时由系统置位，状态由系统推） */
    private static final int STATUS_PENDING = 0;

    /** 场所档案状态：停用 */
    private static final int SITE_STATUS_DISABLED = 1;

    /**
     * 剂量率合理上限(μSv/h)：与 ITRadDoseRuleService#evaluateTop 的既有口径一致，
     * 超出 [0, MAX_REASONABLE_QTY] 视为写得不合理（负数、大得离谱），直接不收
     */
    private static final BigDecimal MAX_REASONABLE_QTY = new BigDecimal("100");

    @Autowired
    private TRadDoseRuleMapper radDoseRuleMapper;

    @Autowired
    private TRadSiteMapper radSiteMapper;

    @Override
    public TRadAlarmBill selectTRadAlarmBillById(Long id) {
        return this.baseMapper.selectOne(new QueryWrapper<TRadAlarmBill>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TRadAlarmBill> selectTRadAlarmBillList(Wrapper<TRadAlarmBill> queryWrapper) {
        QueryWrapper<TRadAlarmBill> wrapper = (QueryWrapper<TRadAlarmBill>) queryWrapper;
        wrapper.eq("del_flag", 0);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public List<TRadAlarmBill> selectTRadAlarmBillList(TRadAlarmBill record) {
        QueryWrapper<TRadAlarmBill> wrapper = new QueryWrapper<TRadAlarmBill>();
        if (record != null) {
            // 按场所筛选
            if (record.getSiteId() != null) {
                wrapper.eq("site_id", record.getSiteId());
            }
            // 按状态筛选
            if (record.getStatus() != null) {
                wrapper.eq("status", record.getStatus());
            }
            if (StringUtils.isNotEmpty(record.getBillNo())) {
                wrapper.eq("bill_no", record.getBillNo());
            }
        }
        wrapper.eq("del_flag", 0);
        wrapper.orderByDesc("id");
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public int insertTRadAlarmBill(TRadAlarmBill record) {
        // 老签名保留：预警单只从登记进，统一走登记逻辑
        return insertTRadAlarmBill(record, null);
    }

    @Override
    public int insertTRadAlarmBill(TRadAlarmBill record, String createBy) {
        if (record == null) {
            return 0;
        }
        // 单号手工录：空单号不收
        if (StringUtils.isEmpty(record.getBillNo())) {
            return 0;
        }
        // 剂量率写得不合理（负数、大得离谱）的直接不收
        BigDecimal qty = record.getQty();
        if (qty == null || qty.compareTo(BigDecimal.ZERO) < 0 || qty.compareTo(MAX_REASONABLE_QTY) > 0) {
            return 0;
        }
        // 一事一张：单号重了不收
        Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TRadAlarmBill>()
                .eq("bill_no", record.getBillNo()).eq("del_flag", 0));
        if (dupCnt != null && dupCnt > 0) {
            return 0;
        }
        // 场所须存在且在用：停用以后做新单子选不到（老单子上挂着的关联不动）
        TRadSite site = radSiteMapper.selectOne(new QueryWrapper<TRadSite>()
                .eq("id", record.getSiteId()).eq("del_flag", 0));
        if (site == null || (site.getStatus() != null && site.getStatus() == SITE_STATUS_DISABLED)) {
            return 0;
        }
        // 判档阈值来自「剂量率判定规则」（t_rad_dose_rule 的 th1_max/th2_max/th3_max），
        // 取启用且未删除、优先级最高的一条；无可用规则无法定档，不收。
        TRadDoseRule rule = radDoseRuleMapper.selectOne(new QueryWrapper<TRadDoseRule>()
                .eq("status", 0).eq("del_flag", 0).orderByDesc("priority").last("limit 1"));
        if (rule == null) {
            return 0;
        }
        // 档位不用人算：系统按剂量率折出来，以前端传值为准一律覆盖
        record.setAlarmLevel(BigDecimal.valueOf(bandOf(rule, qty)));
        // 状态由系统推：登记即待处理
        record.setStatus(STATUS_PENDING);
        if (StringUtils.isNotEmpty(createBy)) {
            record.setCreateBy(createBy);
        }
        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    /** 分档：不超过哪一档上限就落哪一档，超出管控档上限为最高档 */
    private int bandOf(TRadDoseRule rule, BigDecimal qty) {
        if (rule.getTh1Max() != null && qty.compareTo(rule.getTh1Max()) <= 0) {
            return 1;
        }
        if (rule.getTh2Max() != null && qty.compareTo(rule.getTh2Max()) <= 0) {
            return 2;
        }
        if (rule.getTh3Max() != null && qty.compareTo(rule.getTh3Max()) <= 0) {
            return 3;
        }
        return 4;
    }

    @Override
    public int updateTRadAlarmBill(TRadAlarmBill record) {
        if (record == null || record.getId() == null) {
            return 0;
        }

        if (record.getId() != null && StringUtils.isNotEmpty(record.getBillNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TRadAlarmBill>()
                    .eq("bill_no", record.getBillNo()).ne("id", record.getId()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                return 0;
            }
        }

        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new UpdateWrapper<TRadAlarmBill>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public int deleteTRadAlarmBillByIds(String ids) {
        Long[] idArr = ConvertUtil.toLongArray(ids);
        return this.baseMapper.deleteBatchIds(Arrays.asList(idArr));
    }

    @Override
    public int deleteTRadAlarmBillById(Long id) {
        return this.baseMapper.deleteById(id);
    }
}
