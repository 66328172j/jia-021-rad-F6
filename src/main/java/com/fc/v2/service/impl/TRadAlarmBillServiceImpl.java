package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.exception.ServiceException;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.mapper.auto.TRadAlarmBillMapper;
import com.fc.v2.mapper.auto.TRadDoseRuleMapper;
import com.fc.v2.mapper.auto.TRadSiteMapper;
import com.fc.v2.mapper.custom.RadAlarmBillDao;
import com.fc.v2.model.auto.TRadAlarmBill;
import com.fc.v2.model.auto.TRadDoseRule;
import com.fc.v2.model.auto.TRadSite;
import com.fc.v2.model.custom.RadAlarmBillVo;
import com.fc.v2.service.ITRadAlarmBillService;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 剂量预警单 Service业务层处理。
 *
 * <p>一事一张：单号人工录入；所属场所只允许选在用场所；剂量率由服务层校验
 * （负数、大得离谱的直接不收）；预警档位不用人算，系统按当前生效的
 * 「工作场所剂量率判定规则」（t_rad_dose_rule）折算；处理状态由系统推为
 * 「待处理」，前端不认。
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TRadAlarmBillServiceImpl extends ServiceImpl<TRadAlarmBillMapper, TRadAlarmBill> implements ITRadAlarmBillService {

    /** 剂量率合理上限(μSv/h)：1 Sv/h，超过视为离谱值，不收 */
    private static final BigDecimal MAX_QTY = new BigDecimal("1000000");

    /** 单据状态 0待处理 1已处理 2已办结 */
    private static final int STATUS_PENDING = 0;

    @Autowired
    private TRadDoseRuleMapper radDoseRuleMapper;

    @Autowired
    private TRadSiteMapper radSiteMapper;

    @Autowired
    private RadAlarmBillDao radAlarmBillDao;

    @Override
    public TRadAlarmBill selectTRadAlarmBillById(Long id) {
        return this.baseMapper.selectOne(new QueryWrapper<TRadAlarmBill>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TRadAlarmBill> selectTRadAlarmBillList(Wrapper<TRadAlarmBill> queryWrapper) {
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public int insertTRadAlarmBill(TRadAlarmBill record) {
        if (record == null) {
            return 0;
        }
        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    public int updateTRadAlarmBill(TRadAlarmBill record) {
        if (record == null || record.getId() == null) {
            return 0;
        }
        if (StringUtils.isNotEmpty(record.getBillNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TRadAlarmBill>()
                    .eq("bill_no", record.getBillNo())
                    .ne("id", record.getId())
                    .eq("del_flag", 0));
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
        // 逻辑删除：场所与单据之间的历史关联不能断
        String[] idArr = ConvertUtil.toStrArray(ids);
        int rows = 0;
        for (String id : idArr) {
            rows += this.baseMapper.update(null, new UpdateWrapper<TRadAlarmBill>()
                    .set("del_flag", 1)
                    .eq("id", Long.valueOf(id))
                    .eq("del_flag", 0));
        }
        return rows;
    }

    @Override
    public int deleteTRadAlarmBillById(Long id) {
        return deleteTRadAlarmBillByIds(String.valueOf(id));
    }

    @Override
    public TRadAlarmBill registerAlarmBill(TRadAlarmBill record) {
        if (record == null) {
            throw new ServiceException("预警单信息不能为空");
        }

        // 单号手工录入，必填且不重
        String billNo = record.getBillNo() == null ? null : record.getBillNo().trim();
        if (StringUtils.isEmpty(billNo)) {
            throw new ServiceException("预警单号不能为空，需手工录入");
        }
        record.setBillNo(billNo);
        Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TRadAlarmBill>()
                .eq("bill_no", billNo)
                .eq("del_flag", 0));
        if (dupCnt != null && dupCnt > 0) {
            throw new ServiceException("预警单号 " + billNo + " 已存在，不能重复登记");
        }

        // 所属场所：必须存在，且只允许在用场所（停用场所新单选不到）
        if (record.getSiteId() == null) {
            throw new ServiceException("请选择所属场所");
        }
        TRadSite site = radSiteMapper.selectOne(new QueryWrapper<TRadSite>()
                .eq("id", record.getSiteId())
                .eq("del_flag", 0));
        if (site == null) {
            throw new ServiceException("所属场所不存在");
        }
        if (Integer.valueOf(1).equals(site.getStatus())) {
            throw new ServiceException("场所「" + site.getSiteName() + "」已停用，不能新登记预警单");
        }

        // 剂量率校验归服务层：空、负数、离谱值一律不收
        BigDecimal qty = record.getQty();
        if (qty == null) {
            throw new ServiceException("剂量率不能为空");
        }
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("剂量率不能为负数");
        }
        if (qty.compareTo(MAX_QTY) > 0) {
            throw new ServiceException("剂量率 " + qty.toPlainString() + " 超出合理上限(1000000μSv/h)，不予登记");
        }

        // 预警档位系统折算：取当前时刻生效、启用且未删除、优先级最高的一条规则
        TRadDoseRule rule = currentDoseRule();
        record.setAlarmLevel(BigDecimal.valueOf(resolveLevel(qty, rule)));

        // 处理状态由系统推，忽略前端传值
        record.setId(null);
        record.setStatus(STATUS_PENDING);
        record.setDelFlag(0);
        this.baseMapper.insert(record);
        return record;
    }

    @Override
    public List<RadAlarmBillVo> selectAlarmBillList(Long siteId, Integer status, String billNo) {
        return radAlarmBillDao.selectAlarmBillList(siteId, status,
                StringUtils.isEmpty(billNo) ? null : billNo.trim());
    }

    /**
     * 当前生效的剂量率判定规则：启用、未删除、生效区间覆盖当前时刻，优先级最高。
     */
    private TRadDoseRule currentDoseRule() {
        Date now = new Date();
        TRadDoseRule rule = radDoseRuleMapper.selectOne(new QueryWrapper<TRadDoseRule>()
                .eq("status", 0)
                .eq("del_flag", 0)
                .and(w -> w.isNull("eff_start").or().le("eff_start", now))
                .and(w -> w.isNull("eff_end").or().gt("eff_end", now))
                .orderByDesc("priority")
                .last("limit 1"));
        if (rule == null) {
            throw new ServiceException("当前没有生效的剂量率判定规则，无法折算预警档位，请先维护规则");
        }
        if (rule.getTh1Max() == null || rule.getTh2Max() == null || rule.getTh3Max() == null) {
            throw new ServiceException("剂量率判定规则「" + rule.getRuleName() + "」阈值不完整，无法折算预警档位");
        }
        return rule;
    }

    /**
     * 按规则阈值折档：≤正常档上限 1档(正常)，≤关注档上限 2档(关注)，
     * ≤管控档上限 3档(管控)，超出 4档(报警)。
     */
    private int resolveLevel(BigDecimal qty, TRadDoseRule rule) {
        if (qty.compareTo(rule.getTh1Max()) <= 0) {
            return 1;
        }
        if (qty.compareTo(rule.getTh2Max()) <= 0) {
            return 2;
        }
        if (qty.compareTo(rule.getTh3Max()) <= 0) {
            return 3;
        }
        return 4;
    }
}
