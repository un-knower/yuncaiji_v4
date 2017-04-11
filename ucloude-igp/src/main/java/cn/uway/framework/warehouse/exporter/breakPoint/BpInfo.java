package cn.uway.framework.warehouse.exporter.breakPoint;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.status.Status;
import cn.uway.framework.status.dao.StatusDAO;

/**
 * 入库断点相关信息，标识入库到第几条
 * @author sunt
 *
 */
public class BpInfo {
	protected String uniqueId;
	// 输出表表名
	protected String table;
	// 状态表id(唯一)
	protected long statusId;
	// 输出模版ID
	protected int exportId;
	// 断点值
	protected long breakPoint = 0L;
	// 当前处理条数，如果大于断点值，说明需要更新至状态表
	protected long current = 0L;
	// 解析完毕
	protected Boolean parseEnd = false;
	// 解析总行数
	protected long parseNum = -1L;
	
	/**
	 * 使用哪种文件Creater生成parq的文件名称
	 * 没有配置时，使用默认创建器
	 * 1用于生成配置表同步到IMPALA的文件名称
	 * 如果没有适合创建器请定义
	 * 通过在导出模板中配置ctType字段指定文件生成 
	 */
	protected int ctType;
	
	// <statusId,Status>，一个文件只保留一个状态实体
	protected static Map<Long,Status> s2s = new ConcurrentHashMap<Long, Status>();
	// <statusId,Set<exportIds>>，一个文件可以有多个分发器
	protected static Map<Long,Set<Integer>> s2e = new ConcurrentHashMap<Long, Set<Integer>>();
	
	/**
	 * 状态表操作DAO
	 */
	protected StatusDAO statusDAO = AppContext.getBean("statusDAO", StatusDAO.class);
	
	public BpInfo(String table,Status status,int exportId,long breakPoint,int ctType){
		this.table = table;
		this.statusId = status.getId();
		this.exportId = exportId;
		this.uniqueId = statusId+"-"+exportId;
		this.breakPoint = breakPoint;
		this.current = breakPoint;
		this.ctType = ctType;
		if(!s2s.containsKey(statusId)){
			s2s.put(statusId, status);
		}
		Set<Integer> eIds = s2e.get(statusId);
		if(null == eIds){
			eIds = new HashSet<Integer>();
			s2e.put(statusId, eIds);
		}
		eIds.add(exportId);
	}
	
	/**
	 * 获取bp概况，不需要线程安全
	 * @param sb
	 */
	public void getStatistic(StringBuilder sb){
		sb.append(uniqueId).append("---breakPoint:").append(breakPoint).append(";current:").append(current)
		.append(";parseEnd:").append(parseEnd).append(";parseNum:").append(parseNum);
	}
	
	/**
	 * 在exporter.close()时调用
	 * @param parseNum
	 */
	public synchronized void setParseNum(long parseNum){
		this.parseNum = parseNum;
		this.parseEnd = true;
		// 如果总条数==断点数，说明本分发器已经写完了
		if(parseNum == breakPoint){
			updateExportStatus();
		}
	}
	
	public String getTable() {
		return table;
	}
	public String getUniqueId() {
		return uniqueId;
	}
	
	public synchronized void addOne(){
		current++;
	}
	
	public int getCtType() {
		return ctType;
	}

	/**
	 * 尝试更新输出状态为FINISH_SUCCESS
	 */
	private Boolean updateExportStatus(){
		Set<Integer> eIds = s2e.get(statusId);
		if(null == eIds){
			return false;
		}
		eIds.remove(exportId);
		if(eIds.isEmpty()){
			statusDAO.updateExportStatusUnsynchronized(statusId, Status.FINISH_SUCCESS);
			s2s.remove(statusId);
			s2e.remove(statusId);
			return false;
		}
		return true;
	}
	
	/**
	 * 更新断点信息
	 * @return 更新正常返回true；更新失败（文件已关闭等）返回false
	 */
	public synchronized Boolean updateBreakPoint(){
		if(current != breakPoint){
			breakPoint = current;
			Status status = s2s.get(statusId);
			if(null == status){
				return false;
			}
			status.breakPointProcess(exportId, breakPoint);
			statusDAO.updateBreakPointUnsynchronized(statusId, status.getWarehousePoint());
		}
		// 如果已经解析结束，而且断点数==总条数，说明本分发器已经写完了
		if(parseEnd&&(breakPoint == parseNum)){
			return updateExportStatus();
		}
		return true;
	}
	
}
