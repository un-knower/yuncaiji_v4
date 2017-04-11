package cn.uway.framework.warehouse.repository;

/**
 * RepositoryIDGenerator
 * 
 * @author chenrongqiang 2012-11-1
 */
public class RepositoryIDGenerator {

	//
	public static long number = 0L;

	/**
	 * 产生仓库ID 目前暂以时间毫秒数 修改ID产生规则 在时间毫秒数基础上加上一个自增长的数字 ,并且对ID产生方法加锁 避免两个线程同时进入是获取到的仓库ID一致，引起并发问题 chenrongqiang 2012-11-14
	 * 
	 * @return
	 */
	public synchronized static long generatId() {
		addNumber();
		return System.currentTimeMillis() + number;
	}

	private static void addNumber() {
		number++;
	}

	public static void main(String[] args) {
		for (int i = 0; i < 1000; i++) {
			System.out.println(generatId());
		}
	}
}
