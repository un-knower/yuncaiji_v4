package cn.uway.ucloude.uts.web.access.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.web.access.domain.FuncInfo;
import cn.uway.ucloude.uts.web.access.domain.MenuInfo;
import cn.uway.ucloude.uts.web.access.domain.OrgInfo;
import cn.uway.ucloude.uts.web.access.domain.RoleInfo;
import cn.uway.ucloude.uts.web.access.domain.UserInfo;
import cn.uway.ucloude.uts.web.access.face.UserManager;

/**
 * @author Uway-M3 用户管理数据库访问
 */
public class DbUserManager extends JdbcAbstractAccess implements UserManager {

	public DbUserManager() {
		super(ExtConfigKeys.CONNECTION_KEY);
	}

	public UserInfo getUserInfo(String userNo) {
		UserInfo user = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all().from()
				.table(getUserTabName()).where("INVALID = ?", 0).and("user_No = ?", userNo)
				.single(new ResultSetHandler<UserInfo>() {
					@Override
					public UserInfo handle(ResultSet rs) throws SQLException {
						UserInfo user = null;
						if (rs.next()) {
							user = readUserInfo(rs);
						}
						return user;
					}
				});
		return user;
	}

	public OrgInfo getOrgInfo(int orgId) {
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all().from()
				.table(getOrgTabName()).where("org_id = ?", orgId).and("INVALID = ?", 0)
				.single(new ResultSetHandler<OrgInfo>() {
					@Override
					public OrgInfo handle(ResultSet rs) throws SQLException {
						if (rs.next()) {
							return readOrgInfo(rs);
						} else {
							return null;
						}
					}
				});
	}

	public List<RoleInfo> getRoles(int userId) {
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select()
				.columns("r.role_id", "r.role_name", "r.role_type", "r.is_system").from().table("ufa_user_role_info ur")
				.innerJoin("ufa_role_info r on ur.role_id = r.role_id").where("r.INVALID = ?", 0)
				.and("ur.user_id = ?", userId).list(new ResultSetHandler<List<RoleInfo>>() {
					@Override
					public List<RoleInfo> handle(ResultSet rs) throws SQLException {
						List<RoleInfo> roleList = new ArrayList<RoleInfo>();
						while (rs.next()) {
							roleList.add(readRoleInfo(rs));
						}
						return roleList;
					}
				});
	}

	public List<FuncInfo> getFuncInfos(int roleId) {
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select()
				.columns("f.app_id", "f.func_id", "f.func_code", "f.func_url", "f.func_name").from()
				.table("ufa_role_func_info rf").innerJoin("ufa_function_info f on rf.func_id = f.func_id")
				.where("f.VALID != ?", 0).and("rf.role_id = ?", roleId).list(new ResultSetHandler<List<FuncInfo>>() {
					@Override
					public List<FuncInfo> handle(ResultSet rs) throws SQLException {
						List<FuncInfo> funcs = new ArrayList<FuncInfo>();
						while (rs.next()) {
							funcs.add(readFuncInfo(rs));
						}
						return funcs;
					}
				});
	}

	public List<MenuInfo> getMenuInfos(int userId) {
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().columns("distinct m.*").from()
				.table("ufa_menu_info m").innerJoin("ufa_role_menu_info rm on m.menu_id = rm.menu_id")
				.innerJoin("ufa_user_role_info ur on rm.role_id = ur.role_id").where("m.is_effect = ?", 0)
				.and("ur.user_id = ?", userId).orderBy("parent_id, menu_order", OrderByType.ASC)
				.list(new ResultSetHandler<List<MenuInfo>>() {
					@Override
					public List<MenuInfo> handle(ResultSet rs) throws SQLException {
						List<MenuInfo> menus = new ArrayList<MenuInfo>();
						while (rs.next()) {
							menus.add(readMenuInfo(rs));
						}
						return menus;
					}
				});
	}

	@Override
	public List<MenuInfo> getAllMenu() {
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all().from()
				.table("ufa_menu_info").where("is_effect = ?", 0).orderBy("parent_id, menu_order", OrderByType.ASC)
				.list(new ResultSetHandler<List<MenuInfo>>() {
					@Override
					public List<MenuInfo> handle(ResultSet rs) throws SQLException {
						List<MenuInfo> menus = new ArrayList<MenuInfo>();
						while (rs.next()) {
							menus.add(readMenuInfo(rs));
						}
						return menus;
					}
				});
	}

	public boolean lockUser(int userId) {
		try {
			SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getUpdateSql().update().table("UFA_USER_INFO")
					.set("LOCK_DATE", new java.sql.Timestamp(new Date().getTime())).set("IS_LOCKED", 1)
					.where("user_id = ?", userId).doUpdate();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private UserInfo readUserInfo(ResultSet rs) throws SQLException {
		UserInfo user = new UserInfo();
		user.setAddress(rs.getString("address"));
		user.setAptitude(rs.getInt("aptitude"));
		user.setAuthor(rs.getString("author"));
		user.setBirthday(rs.getDate("birthday"));
		user.setCityId(rs.getInt("city_id"));
		user.setLocked(rs.getBoolean("is_locked"));
		user.setMobile(rs.getString("mobile"));
		user.setUserNo(rs.getString("user_no"));
		user.setUserName(rs.getString("user_name"));
		user.setUserLevel(rs.getInt("user_level"));
		user.setUserId(rs.getInt("user_id"));
		user.setPhone(rs.getString("phone"));
		user.setPassword(rs.getString("password"));
		user.setOrgId(rs.getInt("org_id"));
		user.setEmail(rs.getString("email"));
		user.setEnable(rs.getBoolean("IS_ENABLE"));
		return user;
	}

	private OrgInfo readOrgInfo(ResultSet rs) throws SQLException {
		OrgInfo org = new OrgInfo();
		org.setOrgId(rs.getInt("ORG_ID"));
		org.setOrgName("ORG_NAME");
		org.setAreaType(rs.getInt("area_type"));
		org.setAuthor(rs.getString("author"));
		org.setCreateTime(rs.getTimestamp("create_time"));
		org.setInvalid(rs.getBoolean("invalid"));
		org.setLastUpdatedTime(rs.getTimestamp("last_updated_time"));
		org.setMender(rs.getString("mender"));
		org.setOrgDescription(rs.getString("org_description"));
		org.setParentId(rs.getInt("parent_id"));
		org.setSeqNo(rs.getInt("seq_no"));
		return org;
	}

	private RoleInfo readRoleInfo(ResultSet rs) throws SQLException {
		RoleInfo role = new RoleInfo();
		role.setRoleId(rs.getInt("role_id"));
		role.setRoleName(rs.getString("role_name"));
		role.setRoleType(rs.getInt("role_type"));
		role.setSystem(rs.getBoolean("is_system"));
		return role;
	}

	private FuncInfo readFuncInfo(ResultSet rs) throws SQLException {
		FuncInfo func = new FuncInfo();
		func.setAppId(rs.getInt("app_id"));
		func.setFuncCode(rs.getString("func_code"));
		func.setFuncId(rs.getInt("func_id"));
		func.setFuncUrl(rs.getString("func_url"));
		func.setFuncName(rs.getString("func_name"));
		return func;
	}

	private MenuInfo readMenuInfo(ResultSet rs) throws SQLException {
		MenuInfo menu = new MenuInfo();
		menu.setMenuId(rs.getInt("menu_id"));
		menu.setMenuName(rs.getString("menu_name"));
		menu.setMenuUrl(rs.getString("menu_url"));
		menu.setMenuCode(rs.getString("menu_code"));
		menu.setMenuIcon(rs.getString("menu_icon"));
		menu.setMenuOrder(rs.getInt("menu_order"));
		menu.setMenuType(rs.getInt("menu_type"));
		menu.setTipText(rs.getString("tip_text"));
		menu.setParentId(rs.getInt("parent_id"));
		return menu;
	}

	private String getUserTabName() {
		return "UFA_USER_INFO";
	}

	private String getOrgTabName() {
		return "UFA_ORG_INFO";
	}
}
