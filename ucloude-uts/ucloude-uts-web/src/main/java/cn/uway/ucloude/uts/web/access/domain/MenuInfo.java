package cn.uway.ucloude.uts.web.access.domain;

import java.util.List;

public class MenuInfo {
	private int menuId;
	private String menuName;
	private String menuUrl;
	private int menuType;
	private int parentId;
	private int menuOrder;
	private String menuIcon;
	private String  menuCode;
	private String  tipText;
	private boolean isSystem;
	private int funcKind;
	private boolean permissUser;
	private boolean permissSuper;
	private boolean permissUnit;
	private int appId;
	
	private List<MenuInfo> childrens;
	
	public int getMenuId() {
		return menuId;
	}
	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	public String getMenuUrl() {
		return menuUrl;
	}
	public void setMenuUrl(String menuUrl) {
		this.menuUrl = menuUrl;
	}
	public int getMenuType() {
		return menuType;
	}
	public void setMenuType(int menuType) {
		this.menuType = menuType;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public int getMenuOrder() {
		return menuOrder;
	}
	public void setMenuOrder(int menuOrder) {
		this.menuOrder = menuOrder;
	}
	public String getMenuIcon() {
		return menuIcon;
	}
	public void setMenuIcon(String menuIcon) {
		this.menuIcon = menuIcon;
	}
	public String getMenuCode() {
		return menuCode;
	}
	public void setMenuCode(String menuCode) {
		this.menuCode = menuCode;
	}
	public String getTipText() {
		return tipText;
	}
	public void setTipText(String tipText) {
		this.tipText = tipText;
	}
	public boolean isSystem() {
		return isSystem;
	}
	public void setSystem(boolean isSystem) {
		this.isSystem = isSystem;
	}
	public int getFuncKind() {
		return funcKind;
	}
	public void setFuncKind(int funcKind) {
		this.funcKind = funcKind;
	}
	public boolean isPermissUser() {
		return permissUser;
	}
	public void setPermissUser(boolean permissUser) {
		this.permissUser = permissUser;
	}
	public boolean isPermissSuper() {
		return permissSuper;
	}
	public void setPermissSuper(boolean permissSuper) {
		this.permissSuper = permissSuper;
	}
	public boolean isPermissUnit() {
		return permissUnit;
	}
	public void setPermissUnit(boolean permissUnit) {
		this.permissUnit = permissUnit;
	}
	public int getAppId() {
		return appId;
	}
	public void setAppId(int appId) {
		this.appId = appId;
	}
	public List<MenuInfo> getChildrens() {
		return childrens;
	}
	public void setChildrens(List<MenuInfo> childrens) {
		this.childrens = childrens;
	}
}
