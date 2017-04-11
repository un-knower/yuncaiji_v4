package cn.uway.framework.parser;

import java.util.Date;
import java.util.List;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.job.AbstractJob;
import cn.uway.framework.job.JobParam;

/**
 * 解码器接口
 * 
 * @author MikeYang
 * @Date 2012-10-27
 * @version 1.0
 * @since 3.0
 */
public interface Parser {

	/**
	 * 解码之前的操作
	 */
	void before();

	/**
	 * 设置解析模板
	 * 
	 * @param templates
	 */
	void setTemplates(String templates);
	
	
	/**
	 * 获取解析模板
	 * @return
	 */
	String getTemplates();
	
	/**
	 * 是否需要接受指定文件的解码
	 * @param fileName
	 * @return
	 */
	boolean canAccess(String fileName);

	/**
	 * 解码接入输出对象
	 * 
	 * @param accessOutObject
	 *            接入输出对象{@link AccessOutObject}
	 */
	void parse(AccessOutObject accessOutObject) throws Exception;

	/**
	 * 是否有下一条记录
	 */
	boolean hasNextRecord() throws Exception;

	/**
	 * 获取下一条记录
	 * <p>
	 * 调用此方法建议先判断一下是否还有记录，即调用{@link #hasNextRecord()}判断.
	 * </p>
	 * 
	 * @return {@link ParseOutRecord}
	 */
	ParseOutRecord nextRecord() throws Exception;

	/**
	 * 获取所有记录
	 * 
	 * @return 整个接入输出对象被解码后的所有记录{@link ParseOutRecord}
	 */
	List<ParseOutRecord> getAllRecords();

	/**
	 * 关闭解码器，主要用来释放资源
	 */
	void close();

	/**
	 * 获取解析数据处理报告
	 */
	ParserReport getReport();

	/**
	 * 获取数据时间 周期性任务直接取task中dataTime
	 * 
	 * @return 数据时间
	 */
	Date getDataTime(ParseOutRecord outRecord);

	/**
	 * 获取数据时间
	 * 
	 * @return 解析对象数据时间
	 */
	Date getCurrentDataTime();

	/**
	 * 获取数据时间
	 * 
	 * @return 解析对象数据时间
	 */
	void setDataTime(Date cuttentDataTime);

	/**
	 * 解码完成之后的操作
	 */
	void after() throws Exception;

	/**
	 * 销毁方法
	 */
	void destory();

	/**
	 * 设置当前解码任务的job
	 * 
	 * @param currJob
	 */
	void setCurrentJob(AbstractJob currJob);

	/**
	 * 获取当前的解码任务
	 * 
	 * @param currJob
	 */
	AbstractJob getCurrentJob();

	/**
	 * 初始化方法
	 * 
	 * @param jobParam
	 */
	void init(JobParam jobParam);

	/**
	 * 关闭之后调用方法
	 */
	void afterClose();
}
