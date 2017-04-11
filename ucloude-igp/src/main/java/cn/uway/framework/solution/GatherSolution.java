package cn.uway.framework.solution;

import cn.uway.framework.accessor.Accessor;
import cn.uway.framework.parser.Parser;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.exporter.ExportDefinition;

/**
 * 采集解决方案基类
 * 
 * @author MikeYang
 * @Date 2012-10-27
 * @version 1.0
 * @since 3.0
 * @see SolutionLoader
 */
public class GatherSolution {

	/**
	 *  基本属性部分:解决方案编号
	 */
	private long id; 

	/**
	 *  数据接入器
	 */
	private Accessor accessor; 

	/**
	 *  数据解码器
	 */
	private Parser parser;
	
	/**
	 * <pre>
	 * 是否使用自适应压缩流
	 * 		自适应压缩流是指在原始文件如果是压缩文件如：	zip、tar或gzip时(根据文件名后缀判断)，
	 *  	将自动变成在parser中可直接读取的压缩流，而不用去关心原始文件的压缩格式和压缩包中的文件个数。
	 *  
	 *  	(注意：使用自适应流时，如果原始文件是压缩包，且有多个文件时，
	 *   		每个子文件，会依次轮流调用parser的parse()、haseNext()、nextRecord()方法，
	 *   		直接文件解码遍历完毕，同一个压缩包下面的子文件都会共用同一个parser对象，
	 *   		需要注意parser的初始化工作
	 *   	）
	 * </pre>
	 */
	private boolean adaptiveStreamJobAvaliable;

	 /**
	  * 输出定义xml文件配置信息 填写输出xml文件路径
	  */
	private ExportDefinition exportDefinition;

	/**
	 *  数据接入前执行的命令
	 */
	private String beforeAccessShell; 

	/**
	 *  数据解码前执行的命令
	 */
	private String beforePaserShell; 

	/**
	 * 命令执行超时时间，单位分钟
	 */
	private int shellTimeoutMinutes; 

	/**
	 *  以下为方便在配置文件中很容易看出此方案应用的场景而定义
	 */
	private String vendor; // 厂家信息
	/**
	 * 标题
	 */
	private String caption; 
	/**
	 *  省份
	 */
	private String province; 
	 /**
	  *  城市
	  */
	private String city;

	public GatherSolution() {
		super();
	}
	
	/**
	 * 构造方法
	 */
	public GatherSolution(Task task) {
		super();
		this.id = task.getSolutionId();
	}

	/**
	 * 获取采集解决方案编号
	 */
	public long getId() {
		return id;
	}

	/**
	 * 设置采集解决方案编号
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * 获取数据接入器
	 */
	public Accessor getAccessor() {
		return accessor;
	}

	public void setAccessor(Accessor accessor) {
		this.accessor = accessor;
	}

	/**
	 * 获取数据解码器
	 */
	public Parser getParser() {
		return parser;
	}

	public void setParser(Parser parser) {
		this.parser = parser;
	}
	
	public boolean isAdaptiveStreamJobAvaliable() {
		return adaptiveStreamJobAvaliable;
	}
	
	public void setAdaptiveStreamJobAvaliable(boolean adaptiveStreamJobAvaliable) {
		this.adaptiveStreamJobAvaliable = adaptiveStreamJobAvaliable;
	}

	/**
	 * 获取数据接入前执行的命令
	 */
	public String getBeforeAccessShell() {
		return beforeAccessShell;
	}

	void setBeforeAccessShell(String beforeAccessShell) {
		this.beforeAccessShell = beforeAccessShell;
	}

	/**
	 * 获取数据解码前执行的命令
	 */
	public String getBeforePaserShell() {
		return beforePaserShell;
	}

	void setBeforePaserShell(String beforePaserShell) {
		this.beforePaserShell = beforePaserShell;
	}

	/**
	 * 获取命令超时时间
	 */
	public int getShellTimeoutMinutes() {
		return shellTimeoutMinutes;
	}

	void setShellTimeoutMinutes(int shellTimeoutMinutes) {
		this.shellTimeoutMinutes = shellTimeoutMinutes;
	}

	/**
	 * 获取厂家信息
	 */
	public String getVendor() {
		return vendor;
	}

	/**
	 * 设置厂家信息
	 */
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	/**
	 * 获取标题
	 */
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * 获取省份信息
	 */
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public ExportDefinition getExportDefinition() {
		return exportDefinition;
	}

	public void setExportDefinition(ExportDefinition exportDefinition) {
		this.exportDefinition = exportDefinition;
	}

	/**
	 * 获取城市信息
	 */
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String toString() {
		return "采集解决方案编号:" + this.id + " 采集之前的指令:" + this.beforeAccessShell + " 采集之后的指令:" + this.beforePaserShell + " 指令超时时间，单位为分钟:"
				+ this.shellTimeoutMinutes;
	}
}
