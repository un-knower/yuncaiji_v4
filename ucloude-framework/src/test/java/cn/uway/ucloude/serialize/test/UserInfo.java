package cn.uway.ucloude.serialize.test;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserInfo {
	private String userName;
	private int age;
	private List<UserInfo> children;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public List<UserInfo> getChildren() {
		return children;
	}

	public void setChildren(List<UserInfo> children) {
		this.children = children;
	}
}
