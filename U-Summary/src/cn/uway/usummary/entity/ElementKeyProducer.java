package cn.uway.usummary.entity;

/**
 * Element key生成器 会根据dataType维持一个自增长的数字
 * 
 */
public class ElementKeyProducer {

	/**
	 * Element KEY计数器
	 */
	private int counter = 0;

	/**
	 * 获取当前数据类型的下一个element key
	 * 
	 * @param dataType
	 * @return NextElementKey
	 */
	public String getNextElementKey() {
		counter++;
		return new StringBuilder().append("ElementKey-").append(counter).toString();
	}
}