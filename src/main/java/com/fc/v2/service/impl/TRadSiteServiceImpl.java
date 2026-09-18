package com.fc.v2.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.mapper.auto.TRadSiteMapper;
import com.fc.v2.model.auto.TRadSite;
import com.fc.v2.service.ITRadSiteService;
import com.fc.v2.util.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 辐射工作场所档案Service业务层处理
 *
 * @author fuce
 * @date 2026-09-18
 */
@Service
public class TRadSiteServiceImpl extends ServiceImpl<TRadSiteMapper, TRadSite> implements ITRadSiteService {

    /** 档案状态：在用 */
    private static final int STATUS_IN_USE = 0;

    @Override
    public TRadSite selectTRadSiteById(Long id) {
        // 停用只是状态位，档案仍在：老单子上挂着的关联不能断，故不按状态过滤
        return this.baseMapper.selectOne(new QueryWrapper<TRadSite>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TRadSite> selectTRadSiteList(Wrapper<TRadSite> queryWrapper) {
        QueryWrapper<TRadSite> wrapper = (QueryWrapper<TRadSite>) queryWrapper;
        wrapper.eq("del_flag", 0);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public List<TRadSite> selectTRadSiteList(TRadSite record) {
        QueryWrapper<TRadSite> wrapper = new QueryWrapper<TRadSite>();
        if (record != null) {
            if (StringUtils.isNotEmpty(record.getSiteNo())) {
                wrapper.eq("site_no", record.getSiteNo());
            }
            if (StringUtils.isNotEmpty(record.getSiteName())) {
                wrapper.like("site_name", record.getSiteName());
            }
            if (StringUtils.isNotEmpty(record.getSiteType())) {
                wrapper.eq("site_type", record.getSiteType());
            }
            if (StringUtils.isNotEmpty(record.getAreaName())) {
                wrapper.eq("area_name", record.getAreaName());
            }
            if (record.getStatus() != null) {
                // 做新单子选场所时传 status=0，停用场所选不到
                wrapper.eq("status", record.getStatus());
            }
        }
        wrapper.eq("del_flag", 0);
        wrapper.orderByDesc("id");
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public int insertTRadSite(TRadSite record) {
        if (record == null) {
            return 0;
        }
        // 场所编号按规则编、建档时人工填：空编号不收
        if (StringUtils.isEmpty(record.getSiteNo())) {
            return 0;
        }
        // 填重了不收
        Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TRadSite>()
                .eq("site_no", record.getSiteNo()).eq("del_flag", 0));
        if (dupCnt != null && dupCnt > 0) {
            return 0;
        }
        if (record.getStatus() == null) {
            record.setStatus(STATUS_IN_USE);
        }
        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    public int updateTRadSite(TRadSite record) {
        if (record == null || record.getId() == null) {
            return 0;
        }
        if (StringUtils.isNotEmpty(record.getSiteNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TRadSite>()
                    .eq("site_no", record.getSiteNo()).ne("id", record.getId()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                return 0;
            }
        }
        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new UpdateWrapper<TRadSite>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public int deleteTRadSiteByIds(String ids) {
        Long[] idArr = ConvertUtil.toLongArray(ids);
        return this.baseMapper.deleteBatchIds(Arrays.asList(idArr));
    }

    @Override
    public int deleteTRadSiteById(Long id) {
        return this.baseMapper.deleteById(id);
    }
}
