package cn.uway.ucloude.uts.web.access.domain;

import java.util.Date;
import java.util.List;
import java.io.Serializable;

public class UserInfo implements Serializable {
	private int userId;
	private String userNo;
	private boolean invalid;
	private int userLevel;
	private int orgId;
	private int sexCode;
	private String password;
	private String mobile;
	private String email;
	private String phone;
	private String fax;
	private boolean isLocked;
	private Date lockDate;
	private String author;
	private Date createDate;
	private String mender;
	private Date updateDate;
	private String address;
	private String reMark;
	private String userName;
	private int postLevel;
	private int educational;
	private int positionalTitle;
	private int isInternalTrainer;
	private int post;
	private Date birthday;
	private String qq_number;
	private String other_contact_way;
	private int aptitude;
	private String yixin;
	private String wechat;
	private int cityId;
	private boolean isNetworkOptimizetor;
	private int userOptimizstionTypeCode;
	private String port;
	private boolean disable;
	
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public int getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}

	public int getOrgId() {
		return orgId;
	}

	public void setOrgId(int orgId) {
		this.orgId = orgId;
	}

	public int getSexCode() {
		return sexCode;
	}

	public void setSexCode(int sexCode) {
		this.sexCode = sexCode;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public Date getLockDate() {
		return lockDate;
	}

	public void setLockDate(Date lockDate) {
		this.lockDate = lockDate;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getMender() {
		return mender;
	}

	public void setMender(String mender) {
		this.mender = mender;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getReMark() {
		return reMark;
	}

	public void setReMark(String reMark) {
		this.reMark = reMark;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getPostLevel() {
		return postLevel;
	}

	public void setPostLevel(int postLevel) {
		this.postLevel = postLevel;
	}

	public int getEducational() {
		return educational;
	}

	public void setEducational(int educational) {
		this.educational = educational;
	}

	public int getPositionalTitle() {
		return positionalTitle;
	}

	public void setPositionalTitle(int positionalTitle) {
		this.positionalTitle = positionalTitle;
	}

	public int getIsInternalTrainer() {
		return isInternalTrainer;
	}

	public void setIsInternalTrainer(int isInternalTrainer) {
		this.isInternalTrainer = isInternalTrainer;
	}

	public int getPost() {
		return post;
	}

	public void setPost(int post) {
		this.post = post;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getQq_number() {
		return qq_number;
	}

	public void setQq_number(String qq_number) {
		this.qq_number = qq_number;
	}

	public String getOther_contact_way() {
		return other_contact_way;
	}

	public void setOther_contact_way(String other_contact_way) {
		this.other_contact_way = other_contact_way;
	}

	public int getAptitude() {
		return aptitude;
	}

	public void setAptitude(int aptitude) {
		this.aptitude = aptitude;
	}

	public String getYixin() {
		return yixin;
	}

	public void setYixin(String yixin) {
		this.yixin = yixin;
	}

	public String getWechat() {
		return wechat;
	}

	public void setWechat(String wechat) {
		this.wechat = wechat;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public boolean isNetworkOptimizetor() {
		return isNetworkOptimizetor;
	}

	public void setNetworkOptimizetor(boolean isNetworkOptimizetor) {
		this.isNetworkOptimizetor = isNetworkOptimizetor;
	}

	public int getUserOptimizstionTypeCode() {
		return userOptimizstionTypeCode;
	}

	public void setUserOptimizstionTypeCode(int userOptimizstionTypeCode) {
		this.userOptimizstionTypeCode = userOptimizstionTypeCode;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public boolean disable() {
		return disable;
	}

	public void setEnable(boolean disable) {
		this.disable = disable;
	}
}
