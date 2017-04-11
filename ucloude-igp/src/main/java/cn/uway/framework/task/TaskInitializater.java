package cn.uway.framework.task;

import cn.uway.framework.log.ImportantLogger;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.NetUtil;

/**
 * 任务初始化类<br>
 * 在程序启动时将上次任务解码完成但是入库未完成的记录 status设置为0 标志重采
 * 
 * @author chenrongqiang
 * @version 1.0
 */
public class TaskInitializater{

	/**
	 * 状态表操作DAO
	 */
	private StatusDAO statusDAO;

	/**
	 * 程序PID
	 */
	private String pid;

	private static ILogger logger = LoggerManager.getLogger(TaskInitializater.class); // 日志

	public void setStatusDAO(StatusDAO statusDAO){
		this.statusDAO = statusDAO;
	}

	public void setPid(String pid){
		this.pid = pid;
	}

	public void revertGatherStatusOfLastShutdown(){
		String pcName = NetUtil.getHostName();
		if((pid != null && !pid.trim().isEmpty()) && !"0".equals(pid))
			pcName += "@" + this.pid;
		int revertNum = statusDAO.gatherObjStatusRevert(pcName);
		if(revertNum == -1){
			ImportantLogger.getLogger().error("上次程序关闭未全部输出完成采集对象状态还原失败,程序即将关闭。");
			System.exit(0);
		}
		logger.debug("程序上次关闭未全部输出完成采集记录状态还原成功,共{}记录被还原【status从7还原为0】", revertNum);
	}

}
