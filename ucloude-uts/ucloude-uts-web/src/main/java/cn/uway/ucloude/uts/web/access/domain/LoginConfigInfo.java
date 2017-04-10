package cn.uway.ucloude.uts.web.access.domain;

import java.util.Date;

/**
 * 登录配置
 * 
 * @author Uway-M3
 */
public class LoginConfigInfo {
	private int LoginSettingID;
	private int IsEnabledIP;
	private int IsEnabledMac;
	private int PwdErrorCount;
	private LockType UserLockCode;
	private int UserLockTimeout;
	private String Mender;
	private Date UpdateDate;
	private String Remark;
	private int LoginIntervalCount;
	public int getLoginSettingID() {
		return LoginSettingID;
	}
	public void setLoginSettingID(int loginSettingID) {
		LoginSettingID = loginSettingID;
	}
	public int getIsEnabledIP() {
		return IsEnabledIP;
	}
	public void setIsEnabledIP(int isEnabledIP) {
		IsEnabledIP = isEnabledIP;
	}
	public int getIsEnabledMac() {
		return IsEnabledMac;
	}
	public void setIsEnabledMac(int isEnabledMac) {
		IsEnabledMac = isEnabledMac;
	}
	public int getPwdErrorCount() {
		return PwdErrorCount;
	}
	public void setPwdErrorCount(int pwdErrorCount) {
		PwdErrorCount = pwdErrorCount;
	}
	public LockType getUserLockCode() {
		return UserLockCode;
	}
	public void setUserLockCode(LockType userLockCode) {
		UserLockCode = userLockCode;
	}
	public int getUserLockTimeout() {
		return UserLockTimeout;
	}
	public void setUserLockTimeout(int userLockTimeout) {
		UserLockTimeout = userLockTimeout;
	}
	public String getMender() {
		return Mender;
	}
	public void setMender(String mender) {
		Mender = mender;
	}
	public Date getUpdateDate() {
		return UpdateDate;
	}
	public void setUpdateDate(Date updateDate) {
		UpdateDate = updateDate;
	}
	public String getRemark() {
		return Remark;
	}
	public void setRemark(String remark) {
		Remark = remark;
	}
	public int getLoginIntervalCount() {
		return LoginIntervalCount;
	}
	public void setLoginIntervalCount(int loginIntervalCount) {
		LoginIntervalCount = loginIntervalCount;
	}
	
}
