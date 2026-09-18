package com.fc.v2.model.custom;

import com.fc.v2.model.auto.TRadAlarmBill;

/**
 * 剂量预警单台账行：单据字段 + 冗余展示用的场所信息。
 * 场所编号/名称来自 t_rad_site 关联，场所停用后历史行仍要能显示。
 *
 * @author fuce
 */
public class RadAlarmBillVo extends TRadAlarmBill {

    private static final long serialVersionUID = 1L;

    /** 场所编号（关联展示） */
    private String siteNo;

    /** 场所名称（关联展示） */
    private String siteName;

    /** 所属分区（关联展示） */
    private String areaName;

    public String getSiteNo() {
        return siteNo;
    }

    public void setSiteNo(String siteNo) {
        this.siteNo = siteNo;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
