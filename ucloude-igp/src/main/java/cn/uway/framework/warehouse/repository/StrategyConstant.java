package cn.uway.framework.warehouse.repository;

/**
 * @author yuy 策略常量值的标准定义
 */
public class StrategyConstant {

	/**
	 * one-multi：一对多，即一个文件流对应多个输出器
	 */
	public static String STRATEGY_ONETOMULTI = "ONE-MULTI";

	/**
	 * one-one:一对一，即一个文件流对应一个输出器(一个文件流只入一张表)
	 */
	public static String STRATEGY_ONETOONE = "ONE-ONE";

}
