package cn.uway.ucloude.serialize.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cn.uway.ucloude.serialize.JsonConvert;

public class JsonTest {
	@Test
	public void testJoson(){
		List<String> numbers = new ArrayList<String>();
		for(int i = 0; i < 20; i++){
			numbers.add(i+"");
		}
		String json = JsonConvert.serialize(numbers);
		System.out.println(json);
	}
}
