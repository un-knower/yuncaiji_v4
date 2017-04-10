package cn.uway.ucloude.serialize.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cn.uway.ucloude.serialize.BinaryConvert;

public class BinaryTest {
	@Test
	public void testBinary(){
		List<String> numbers = new ArrayList<String>();
		for(int i = 0; i < 20; i++){
			numbers.add(i+"");
		}
		byte[] byteArray = BinaryConvert.serialize(numbers);
		System.out.println(byteArray);
		numbers = BinaryConvert.deSerialize(byteArray);
		System.out.println(numbers);
	}
}
