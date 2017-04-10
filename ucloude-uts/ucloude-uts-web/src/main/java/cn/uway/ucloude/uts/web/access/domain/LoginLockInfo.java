package cn.uway.ucloude.uts.web.access.domain;

import java.util.Date;

public class LoginLockInfo {
	private int loginLockId;
	private int userId;
	private String userLockCode;
	private int pwdErrorCounter;
	private int userLockTimeout;
	private String loginIp;
	private Date lockDate;
	private Date createDate;
	private Date updateDate;
	private String remark;
	
	public int getLoginLockId() {
		return loginLockId;
	}
	public void setLoginLockId(int loginLockId) {
		this.loginLockId = loginLockId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUserLockCode() {
		return userLockCode;
	}
	public void setUserLockCode(String userLockCode) {
		this.userLockCode = userLockCode;
	}
	public int getPwdErrorCounter() {
		return pwdErrorCounter;
	}
	public void setPwdErrorCounter(int pwdErrorCounter) {
		this.pwdErrorCounter = pwdErrorCounter;
	}
	public int getUserLockTimeout() {
		return userLockTimeout;
	}
	public void setUserLockTimeout(int userLockTimeout) {
		this.userLockTimeout = userLockTimeout;
	}
	public String getLoginIp() {
		return loginIp;
	}
	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}
	public Date getLockDate() {
		return lockDate;
	}
	public void setLockDate(Date lockDate) {
		this.lockDate = lockDate;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

}
