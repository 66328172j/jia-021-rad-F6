package com.fc.v2.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.exception.ServiceException;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.mapper.auto.TRadSiteMapper;
import com.fc.v2.model.auto.TRadSite;
import com.fc.v2.service.ITRadSiteService;
import com.fc.v2.util.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 辐射工作场所档案 Service业务层处理
 *
 * <p>场所编号按一套规则编：前缀 RAD + 3位顺序号（如 RAD001）。
 * 建档时编号人工填写，服务层校验格式并查重，填重了不收。
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TRadSiteServiceImpl extends ServiceImpl<TRadSiteMapper, TRadSite> implements ITRadSiteService {

    /** 编号规则：RAD + 3位顺序号 */
    public static final String SITE_NO_PATTERN = "RAD[0-9]{3}";
    private static final String SITE_NO_PREFIX = "RAD";

    /** 档案状态 0在用 1停用 */
    private static final int STATUS_ACTIVE = 0;
    private static final int STATUS_DISABLED = 1;

    @Override
    public TRadSite selectTRadSiteById(Long id) {
        return this.baseMapper.selectOne(new QueryWrapper<TRadSite>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TRadSite> selectTRadSiteList(Wrapper<TRadSite> queryWrapper) {
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public int insertTRadSite(TRadSite record) {
        if (record == null) {
            return 0;
        }
        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    public int updateTRadSite(TRadSite record) {
        if (record == null || record.getId() == null) {
            return 0;
        }
        // 状态只能走停用入口，普通修改不得改档案状态
        record.setStatus(null);
        return this.baseMapper.update(record, new UpdateWrapper<TRadSite>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public int deleteTRadSiteByIds(String ids) {
        // 逻辑删除：老单据挂着的场所关联不能断
        String[] idArr = ConvertUtil.toStrArray(ids);
        int rows = 0;
        for (String id : idArr) {
            rows += this.baseMapper.update(null, new UpdateWrapper<TRadSite>()
                    .set("del_flag", 1)
                    .eq("id", Long.valueOf(id))
                    .eq("del_flag", 0));
        }
        return rows;
    }

    @Override
    public int deleteTRadSiteById(Long id) {
        return deleteTRadSiteByIds(String.valueOf(id));
    }

    @Override
    public TRadSite registerSite(TRadSite record) {
        if (record == null) {
            throw new ServiceException("登记信息不能为空");
        }
        String siteNo = record.getSiteNo() == null ? null : record.getSiteNo().trim();
        if (StringUtils.isEmpty(siteNo)) {
            throw new ServiceException("场所编号不能为空");
        }
        if (!siteNo.matches(SITE_NO_PATTERN)) {
            throw new ServiceException("场所编号不符合编码规则（RAD + 3位顺序号，如 RAD001）");
        }
        record.setSiteNo(siteNo);
        if (StringUtils.isEmpty(record.getSiteName())) {
            throw new ServiceException("场所名称不能为空");
        }
        if (StringUtils.isEmpty(record.getSiteType())) {
            throw new ServiceException("场所类型不能为空");
        }
        if (StringUtils.isEmpty(record.getAreaName())) {
            throw new ServiceException("所属分区不能为空");
        }

        // 填重了不收：同一编号在正常档案中只允许一条
        Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TRadSite>()
                .eq("site_no", siteNo)
                .eq("del_flag", 0));
        if (dupCnt != null && dupCnt > 0) {
            throw new ServiceException("场所编号 " + siteNo + " 已存在，填重了不收");
        }

        record.setId(null);
        record.setStatus(STATUS_ACTIVE);
        record.setDelFlag(0);
        this.baseMapper.insert(record);
        return record;
    }

    @Override
    public int disableSite(Long id) {
        TRadSite site = this.baseMapper.selectOne(new QueryWrapper<TRadSite>()
                .eq("id", id)
                .eq("del_flag", 0));
        if (site == null) {
            throw new ServiceException("场所档案不存在或已删除");
        }
        if (Integer.valueOf(STATUS_DISABLED).equals(site.getStatus())) {
            throw new ServiceException("该场所已停用，无需重复停用");
        }
        // 只改状态，不动任何老单据上的关联
        return this.baseMapper.update(null, new UpdateWrapper<TRadSite>()
                .set("status", STATUS_DISABLED)
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TRadSite> selectActiveSites() {
        return this.baseMapper.selectList(new QueryWrapper<TRadSite>()
                .eq("del_flag", 0)
                .eq("status", STATUS_ACTIVE)
                .orderByAsc("site_no"));
    }

    @Override
    public String nextSiteNo() {
        // 按库内同前缀（含历史停用档案，避免号段复用）最大顺序号 + 1
        List<TRadSite> list = this.baseMapper.selectList(new QueryWrapper<TRadSite>()
                .likeRight("site_no", SITE_NO_PREFIX)
                .orderByDesc("site_no")
                .last("limit 1"));
        int seq = 0;
        if (!list.isEmpty() && list.get(0).getSiteNo() != null
                && list.get(0).getSiteNo().matches(SITE_NO_PATTERN)) {
            seq = Integer.parseInt(list.get(0).getSiteNo().substring(SITE_NO_PREFIX.length()));
        }
        return String.format("%s%03d", SITE_NO_PREFIX, seq + 1);
    }
}
