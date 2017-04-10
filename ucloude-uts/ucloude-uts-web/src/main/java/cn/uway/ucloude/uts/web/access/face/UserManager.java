package cn.uway.ucloude.uts.web.access.face;

import java.util.List;
import cn.uway.ucloude.uts.web.access.domain.FuncInfo;
import cn.uway.ucloude.uts.web.access.domain.MenuInfo;
import cn.uway.ucloude.uts.web.access.domain.OrgInfo;
import cn.uway.ucloude.uts.web.access.domain.RoleInfo;
import cn.uway.ucloude.uts.web.access.domain.UserInfo;

public interface UserManager {
	UserInfo getUserInfo(String userNo);

	OrgInfo getOrgInfo(int orgId);

	List<RoleInfo> getRoles(int userId);

	List<FuncInfo> getFuncInfos(int roleId);

	List<MenuInfo> getMenuInfos(int userId);

	List<MenuInfo> getAllMenu();

	boolean lockUser(int userId);
}
