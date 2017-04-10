package cn.uway.ucloude.serialize.test;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import cn.uway.ucloude.serialize.XmlConvert;

public class XmlTest {
	@Test
	public void testXml() {
		UserInfo u1=new UserInfo();
		u1.setAge(10);
		u1.setUserName("nihao");
		UserInfo u2=new UserInfo();
		u1.setAge(10);
		u1.setUserName("nihao");
		UserInfo u3=new UserInfo();
		u1.setAge(10);
		u1.setUserName("nihao");
		List<UserInfo> list=new ArrayList<>();
		list.add(u2);
		list.add(u3);
		u1.setChildren(list);
		String xml = XmlConvert.serialize(u1);
		System.out.println(xml);
		UserInfo ru = XmlConvert.deSerialize(xml,UserInfo.class);
		System.out.println(ru);
	}
}
