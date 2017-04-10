package cn.uway.ucloude.uts.access.test;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import cn.uway.ucloude.data.dataaccess.DataSourceProvider;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.utils.MD5Utils;
import cn.uway.ucloude.uts.web.access.db.DbUserManager;
import cn.uway.ucloude.uts.web.access.domain.MenuInfo;
import cn.uway.ucloude.uts.web.access.domain.RoleInfo;
import cn.uway.ucloude.uts.web.access.domain.UserInfo;
import cn.uway.ucloude.uts.web.access.face.UserManager;

public class DbLoginTest {
	@Test
	public void test() {
		DataSourceProvider.initialDataSource("");
		String userName = "admin";
		String password = "admin";
		UserManager userManager = new DbUserManager();
		UserInfo user = userManager.getUserInfo(userName);
		Assert.notNull(user, "无效的用户名!");
		Assert.isTrue(!user.getPassword().equals(entryPassword(password)),"密码错误");
		userManager.getOrgInfo(user.getOrgId());
		List<RoleInfo> roles = userManager.getRoles(user.getUserId());
		for (RoleInfo role : roles) {
			role.setFuncList(userManager.getFuncInfos(role.getRoleId()));
		}
		List<MenuInfo> menus = userManager.getMenuInfos(user.getUserId());

	}
	
	private String entryPassword(String password){
		return MD5Utils.getMD5String(password).toLowerCase().substring(8, 16);
	}
}
