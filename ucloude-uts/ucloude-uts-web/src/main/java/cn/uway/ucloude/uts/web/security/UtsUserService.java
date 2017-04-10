package cn.uway.ucloude.uts.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import cn.uway.ucloude.uts.web.access.domain.FuncInfo;
import cn.uway.ucloude.uts.web.access.domain.MenuInfo;
import cn.uway.ucloude.uts.web.access.domain.RoleInfo;
import cn.uway.ucloude.uts.web.access.domain.UserInfo;
import cn.uway.ucloude.uts.web.access.face.UserManager;

@Service("utsUserService")
public class UtsUserService implements UserDetailsService {

	@Autowired
	private UserManager userManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.userdetails.UserDetailsService#
	 * loadUserByUsername(java.lang.String)
	 */
	@Override
	public UserDetails loadUserByUsername(String userNo) throws UsernameNotFoundException {
		Collection<GrantedAuthority> grantedAuths = new HashSet<GrantedAuthority>();
		UtsGrantedAuthority utsAuth = new UtsGrantedAuthority();
		UserInfo user = userManager.getUserInfo(userNo);
		if (user == null) {
			return null;
		}
		utsAuth.setUtsUserInfo(user);
		if (!user.disable() && !user.isLocked()) {// 锁定或禁用的用户
			List<RoleInfo> roles = userManager.getRoles(user.getUserId());
			HashMap<Integer, FuncInfo> funcMap = new HashMap<Integer, FuncInfo>();
			HashMap<Integer, RoleInfo> roleMap = new HashMap<Integer, RoleInfo>();
			for (RoleInfo role : roles) {
				roleMap.put(role.getRoleId(), role);
				List<FuncInfo> roleFuncs = userManager.getFuncInfos(role.getRoleId());
				role.setFuncList(roleFuncs);
				for (FuncInfo func : roleFuncs) {
					if (!funcMap.containsKey(func.getFuncId())) {
						funcMap.put(func.getFuncId(), func);
					}
				}
			}
			List<MenuInfo> menus = userManager.getMenuInfos(user.getUserId());
			utsAuth.setRootMenu(buildMenuTree(menus));
			utsAuth.setUtsFuncMap(funcMap);
			grantedAuths.add(utsAuth);
		}
		User userDetaile = new User(userNo, user.getPassword(), !user.disable(), true, true, !user.isLocked(),
				grantedAuths);
		utsAuth.getUtsUserInfo().setPassword(null);
		return userDetaile;
	}

	private MenuInfo buildMenuTree(List<MenuInfo> menus) {
		MenuInfo root = null;
		for (MenuInfo menu : menus) {
			if (menu.getMenuId() == 200001) {
				root = menu;
			}
		}
		if (root != null) {
			root.setChildrens(getChildrenMenus(root, menus));
		}
		return root;
	}

	private List<MenuInfo> getChildrenMenus(MenuInfo curMenu, List<MenuInfo> menus) {
		List<MenuInfo> childrens = new ArrayList<MenuInfo>();
		for (MenuInfo menu : menus) {
			if (menu.getParentId() == curMenu.getMenuId()) {
				menu.setChildrens(getChildrenMenus(menu, menus));
				childrens.add(menu);
			}
		}
		return childrens;
	}

}
