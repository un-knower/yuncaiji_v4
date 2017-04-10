package cn.uway.ucloude.uts.web.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import cn.uway.ucloude.uts.web.access.domain.FuncInfo;
import cn.uway.ucloude.uts.web.access.domain.MenuInfo;
import cn.uway.ucloude.uts.web.access.domain.RoleInfo;
import cn.uway.ucloude.uts.web.access.domain.UserInfo;

public class UtsGrantedAuthority implements GrantedAuthority {

	@Override
	public String getAuthority() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasRole(int roleId) {
		return utsRoleMap != null && utsRoleMap.containsKey(roleId);
	}

	public boolean hasFunc(int funcId) {
		return utsFuncMap != null && utsFuncMap.containsKey(funcId);
	}

	private MenuInfo rootMenu;
	private UserInfo utsUserInfo;
	private HashMap<Integer, FuncInfo> utsFuncMap;
	private HashMap<Integer, RoleInfo> utsRoleMap;

	public MenuInfo getRootMenu() {
		return rootMenu;
	}

	public void setRootMenu(MenuInfo rootMenu) {
		this.rootMenu = rootMenu;
	}

	public UserInfo getUtsUserInfo() {
		return utsUserInfo;
	}

	public void setUtsUserInfo(UserInfo utsUserInfo) {
		this.utsUserInfo = utsUserInfo;
	}

	public HashMap<Integer, FuncInfo> getUtsFuncMap() {
		return utsFuncMap;
	}

	public void setUtsFuncMap(HashMap<Integer, FuncInfo> utsFuncMap) {
		this.utsFuncMap = utsFuncMap;
	}

	public HashMap<Integer, RoleInfo> getUtsRoleMap() {
		return utsRoleMap;
	}

	public void setUtsRoleMap(HashMap<Integer, RoleInfo> utsRoleMap) {
		this.utsRoleMap = utsRoleMap;
	}

}
