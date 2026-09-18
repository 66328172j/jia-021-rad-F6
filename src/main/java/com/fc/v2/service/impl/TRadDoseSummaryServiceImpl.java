package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TRadAlarmBillMapper;
import com.fc.v2.mapper.auto.TRadDoseSummaryMapper;
import com.fc.v2.model.auto.TRadAlarmBill;
import com.fc.v2.model.auto.TRadDoseSummary;
import com.fc.v2.service.ITRadDoseSummaryService;

/**
 * 场所剂量按月汇总 Service业务层处理（multi-dim-summary 形状：多维汇总与钻取）
 *
 * @author fuce
 * @date 2026-09-17
 */
@Service
public class TRadDoseSummaryServiceImpl implements ITRadDoseSummaryService {

    @javax.annotation.Resource
    private TRadDoseSummaryMapper radDoseSummaryMapper;

    @javax.annotation.Resource
    private TRadAlarmBillMapper radAlarmBillMapper;

    @Override
    public TRadDoseSummary pick(String period, Integer siteId) {
        List<TRadDoseSummary> all = listSummary(period);
        for (TRadDoseSummary s : all) {
            if (s.getSiteId() != null && s.getSiteId().equals(siteId)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public int rebuild(String period) {
        List<TRadAlarmBill> rows = this.radAlarmBillMapper.selectList(new QueryWrapper<TRadAlarmBill>());
        List<TRadDoseSummary> out = new ArrayList<TRadDoseSummary>();
        Map<Integer, BigDecimal> sum = new HashMap<Integer, BigDecimal>();
        Map<Integer, Integer> cnt = new HashMap<Integer, Integer>();
        for (TRadAlarmBill r : rows) {
            Integer site = r.getSiteId();
            BigDecimal qty = r.getQty();
            if (sum.containsKey(site)) {
                sum.put(site, sum.get(site).add(qty));
                cnt.put(site, cnt.get(site) + 1);
            } else {
                sum.put(site, qty);
                cnt.put(site, 1);
            }
        }
        for (Integer site : sum.keySet()) {
            TRadDoseSummary s = new TRadDoseSummary();
            s.setPeriod(period);
            s.setSiteId(site);
            s.setTotalQty(sum.get(site));
            s.setRowCount(cnt.get(site));
            s.setStatus(0);
            s.setDelFlag(0);
            this.radDoseSummaryMapper.insert(s);
            out.add(s);
        }
        return out.get(0) == null ? 0 : out.size();
    }

    @Override
    public List<TRadDoseSummary> listSummary(String period) {
        QueryWrapper<TRadDoseSummary> w = new QueryWrapper<TRadDoseSummary>();
        w.eq("period", period);
        w.orderByAsc("site_id");
        return this.radDoseSummaryMapper.selectList(w);
    }
}