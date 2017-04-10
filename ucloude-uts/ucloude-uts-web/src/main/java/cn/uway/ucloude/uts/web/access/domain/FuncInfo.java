package cn.uway.ucloude.uts.web.access.domain;

public class FuncInfo {
	private int funcId;
	private int parentId;
	private String funcName;
	private String funcDescription;
	private String funcUrl;
	private String funcOrder;
	private String funcCode;
	private int appId;
	private int funcType;
	private int funcKind;
	public int getFuncId() {
		return funcId;
	}
	public void setFuncId(int funcId) {
		this.funcId = funcId;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public String getFuncName() {
		return funcName;
	}
	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}
	public String getFuncDescription() {
		return funcDescription;
	}
	public void setFuncDescription(String funcDescription) {
		this.funcDescription = funcDescription;
	}
	public String getFuncUrl() {
		return funcUrl;
	}
	public void setFuncUrl(String funcUrl) {
		this.funcUrl = funcUrl;
	}
	public String getFuncOrder() {
		return funcOrder;
	}
	public void setFuncOrder(String funcOrder) {
		this.funcOrder = funcOrder;
	}
	public String getFuncCode() {
		return funcCode;
	}
	public void setFuncCode(String funcCode) {
		this.funcCode = funcCode;
	}
	public int getAppId() {
		return appId;
	}
	public void setAppId(int appId) {
		this.appId = appId;
	}
	public int getFuncType() {
		return funcType;
	}
	public void setFuncType(int funcType) {
		this.funcType = funcType;
	}
	public int getFuncKind() {
		return funcKind;
	}
	public void setFuncKind(int funcKind) {
		this.funcKind = funcKind;
	}
	
}
