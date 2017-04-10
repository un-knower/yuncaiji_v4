package cn.uway.ucloude.uts.web.access.face;

import java.util.Date;

import cn.uway.ucloude.uts.web.access.domain.LoginConfigInfo;
import cn.uway.ucloude.uts.web.access.domain.LoginLockInfo;

public interface LoginConfigAccess {
	LoginConfigInfo getLoginCinfigInfo();

	LoginLockInfo getLoginLockInfo(int userId, String ip, Date createTime);

	boolean AddLoginLockInfo(LoginLockInfo loginInfo);

	boolean UpdateLoginLockInfo(LoginLockInfo loginInfo);
}
