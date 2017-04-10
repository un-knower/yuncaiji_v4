/*
 * 菜单初始化
 */

delete from ufa_menu_info where menu_id in(
SELECT menu_id
FROM ufa_menu_info
START WITH menu_id = 200001
CONNECT BY PRIOR menu_id=parent_id);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('200001', '江苏电信', null, 0, null, null, '0.png', 0, null, '调度平台', null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201000', '管理', '/ucloude-uts-web/index.htm', 1, '200001', 1, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201100', '节点操作', null, 2, '201000', 1, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201101', '节点', '/ucloude-uts-web/node-manager.htm', 1, '201100', 1, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201102', '节点组', '/ucloude-uts-web/node-group-manager.htm', 1, '201100', 2, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201200', '任务队列', null, 2, '201000', 2, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201101', '新增任务', '/ucloude-uts-web/job-add.htm', 1, '201200', 1, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201102', '周期性的任务', '/ucloude-uts-web/index.htm', 1, '201200', 2, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201103', '重复性的任务', '/ucloude-uts-web/repeat-job-queue.htm', 1, '201200', 3, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201104', '暂停中的任务', '/ucloude-uts-web/suspend-job-queue.htm', 1, '201200', 4, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201105', '执行中的任务', '/ucloude-uts-web/executing-job-queue.htm', 1, '201200', 5, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201106', '等待执行的任务', '/ucloude-uts-web/executable-job-queue.htm', 1, '201200', 7, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('201107', '加载任务', '/ucloude-uts-web/load-job.htm', 1, '201200', 8, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('202000', '监控', '/ucloude-uts-web/monitor/monitor.htm', 1, '200001', 2, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('203000', '任务日志', '/ucloude-uts-web/job-logger.htm', 1, '200001', 3, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('203001', '任务日志', '/ucloude-uts-web/job-logger.htm', 1, '203000', 1, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('203002', '节点上下线日志', '/ucloude-uts-web/node-onoffline-log.htm', 1, '203000', 2, '0.png', 0, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('203003', '采集任务日志', '/ucloude-uts-web/job-logger1.htm', 1, '203000', 3, '0.png', 1, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('203004', '上报任务日志', '/ucloude-uts-web/job-logger2.htm', 1, '203000', 4, '0.png', 1, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('203005', '汇总任务日志', '/ucloude-uts-web/job-logger3.htm', 1, '203000', 5, '0.png', 1, null, null, null, null, 1, 1, 1, null);

insert into ufa_menu_info (MENU_ID, MENU_NAME, MENU_URL, MENU_TYPE, PARENT_ID, MENU_ORDER, MENU_ICON, IS_EFFECT, MENU_CODE, TIP_TEXT, IS_SYSTEM, FUNC_KIND, PERMISS_USER, PERMISS_SUPER, PERMISS_UNIT, APP_ID)
values ('203006', '告警监控任务日志', '/ucloude-uts-web/job-logger4.htm', 1, '203000', 6, '0.png', 1, null, null, null, null, 1, 1, 1, null);


delete from ufa_role_menu_info where role_id =0 and menu_id in(
SELECT menu_id
FROM ufa_menu_info
START WITH menu_id = 200001
CONNECT BY PRIOR menu_id=parent_id);

insert into ufa_role_menu_info (role_menu_id, role_id, menu_id)
SELECT seq_ufa_role_menu_info.nextval,0,menu_id
FROM ufa_menu_info
START WITH menu_id = 200001
CONNECT BY PRIOR menu_id=parent_id;

commit;
