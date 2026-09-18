-- rad 辐射工作场所安全监测 -- 菜单/权限种子 (jia-021)
-- 依赖：t_sys_permission、t_sys_permission_role；管理员角色 488243256161730560
-- ID 取 7100 段，避开雪花号段。重复执行前先按 perms 清理。

-- 顶级目录
INSERT INTO `t_sys_permission`
(`id`, `name`, `descripion`, `url`, `is_blank`, `pid`, `perms`, `type`, `icon`, `order_num`, `visible`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(7100000000000000000, '辐射场所管理', NULL, '', 0, 0, '', 0, 'layui-icon layui-icon-radio', 20, 0, 'admin', sysdate(), NULL, NULL, '辐射工作场所剂量监测模块');

-- 场所档案
INSERT INTO `t_sys_permission`
(`id`, `name`, `descripion`, `url`, `is_blank`, `pid`, `perms`, `type`, `icon`, `order_num`, `visible`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(7100000000000000001, '场所登记', '场所档案台账', '/RadSiteController/view', 0, 7100000000000000000, 'rad:radSite:view', 1, 'layui-icon layui-icon-read', 1, 0, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000002, '场所台账集合', '场所台账集合', '/RadSiteController/list', 0, 7100000000000000001, 'rad:radSite:list', 2, '', NULL, 0, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000003, '场所登记建档', '场所登记建档', '/RadSiteController/register', 0, 7100000000000000001, 'rad:radSite:add', 2, 'layui-icon layui-icon-add-1', NULL, 0, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000004, '场所档案修改', '场所档案修改', '/RadSiteController/edit', 0, 7100000000000000001, 'rad:radSite:edit', 2, 'layui-icon layui-icon-edit', NULL, 0, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000005, '场所停用', '场所停用（新单不可选，老单关联保留）', '/RadSiteController/disable', 0, 7100000000000000001, 'rad:radSite:disable', 2, 'layui-icon layui-icon-pause', NULL, 0, 'admin', sysdate(), NULL, NULL, NULL);

-- 剂量预警单
INSERT INTO `t_sys_permission`
(`id`, `name`, `descripion`, `url`, `is_blank`, `pid`, `perms`, `type`, `icon`, `order_num`, `visible`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(7100000000000000011, '剂量预警单', '剂量预警单台账', '/RadAlarmBillController/view', 0, 7100000000000000000, 'rad:radAlarmBill:view', 1, 'layui-icon layui-icon-notice', 2, 0, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000012, '预警单集合', '预警单台账集合', '/RadAlarmBillController/list', 0, 7100000000000000011, 'rad:radAlarmBill:list', 2, '', NULL, 0, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000013, '预警单登记', '预警单登记：单号手工录，档位系统折', '/RadAlarmBillController/add', 0, 7100000000000000011, 'rad:radAlarmBill:add', 2, 'layui-icon layui-icon-add-1', NULL, 0, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000014, '预警单修改', '预警单修改', '/RadAlarmBillController/edit', 0, 7100000000000000011, 'rad:radAlarmBill:edit', 2, 'layui-icon layui-icon-edit', NULL, 0, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000015, '预警单删除', '预警单逻辑删除', '/RadAlarmBillController/remove', 0, 7100000000000000011, 'rad:radAlarmBill:remove', 2, 'layui-icon layui-icon-delete', NULL, 0, 'admin', sysdate(), NULL, NULL, NULL);

-- 授予「管理员」角色
INSERT INTO `t_sys_permission_role`
(`id`, `role_id`, `permission_id`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(7100000000000000101, 488243256161730560, 7100000000000000000, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000102, 488243256161730560, 7100000000000000001, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000103, 488243256161730560, 7100000000000000002, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000104, 488243256161730560, 7100000000000000003, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000105, 488243256161730560, 7100000000000000004, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000106, 488243256161730560, 7100000000000000005, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000111, 488243256161730560, 7100000000000000011, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000112, 488243256161730560, 7100000000000000012, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000113, 488243256161730560, 7100000000000000013, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000114, 488243256161730560, 7100000000000000014, 'admin', sysdate(), NULL, NULL, NULL),
(7100000000000000115, 488243256161730560, 7100000000000000015, 'admin', sysdate(), NULL, NULL, NULL);
