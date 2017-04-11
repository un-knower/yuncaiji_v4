package cn.uway.framework.warehouse.exporter.template;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.log.ImportantLogger;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.destination.dao.ExportTargetDAO;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.StringUtil;

/**
 * 输出目的地定义上下文 在程序启动时解析export_config.xml ExportTagetTempletContext
 * 
 * @author chenrongqiang 2012-11-12
 */
public class ExportTargetTempletContext {

	static ILogger logger = LoggerManager.getLogger(ExportTargetTempletContext.class);

	// Context缓存所有IGP输出目的地配置 在程序启动的时候加载初始化
	private Map<Integer, ExporterBean> fileExportTagetBeans = new HashMap<Integer, ExporterBean>();

	/**
	 * 本地输出路径的List集合
	 */
	private List<FileExporterBean> localExportTargetList = new LinkedList<FileExporterBean>();

	private Map<String, ExporterBean> dbExportTagetBeans = new HashMap<String, ExporterBean>();
	
	private Map<String, ExporterBean> remoteFileExportTagetBeans = new HashMap<String, ExporterBean>();

	private static ExportTargetTempletContext contextInstance = new ExportTargetTempletContext();

	private BasicDataSource connPool = (BasicDataSource) AppContext.getBean("jdbcConnection", DataSource.class);

	private ExportTargetTempletContext() {
		super();
	}

	/**
	 * 单例工厂方法
	 * 
	 * @return 获取ExportTargetTempletContext单例对象
	 */
	public static ExportTargetTempletContext getInstance() {
		return contextInstance;
	}

	/**
	 * 提供给外部获取输出目的地模版
	 * 
	 * @param targetId
	 *            输出模版中配置的目的地ID
	 * @param bIgnoreException
	 *            是否忽略错误
	 * @return 目的地配置模版
	 */
	public synchronized ExporterBean getFileExportTargetBean(Integer targetId, boolean bIgnoreException) {
		if (fileExportTagetBeans.isEmpty())
			loadFileExportTargetTemplet();
		ExporterBean exportBean = fileExportTagetBeans.get(targetId);
		if (exportBean == null && !bIgnoreException) {
			logger.warn("targetId={}未找到正确的输出目的地配置!", targetId);
			throw new NullPointerException("未找到正确的输出目的地配置!");
		}
		return exportBean;
	}

	/**
	 * 提供给外部获取输出目的地模版
	 * 
	 * @param targetId
	 *            输出模版中配置的目的地ID
	 * @return 目的地配置模版
	 */
	public ExporterBean getFileExportTargetBean(Integer targetId) {
		return getFileExportTargetBean(targetId, false);
	}

	/**
	 * 获取本地文件输出路径<br>
	 * 
	 * @return 在export_config.xml中配置的所有输出路径
	 */
	public synchronized List<FileExporterBean> getFileExportBeans() {
		if (localExportTargetList.isEmpty())
			loadFileExportTargetTemplet();
		return localExportTargetList;
	}

	/**
	 * 通过任务分组、输出数据类型、输出模版中ID查找数据库输出配置
	 * 
	 * @param task
	 * @param dbTempletBean
	 * @return 数据库输出配置
	 */
	public synchronized ExporterBean getDbExportTargetBean(Task task, DbExportTemplateBean dbTempletBean) {
		// 输出到主库
		if (StringUtil.isNotEmpty(dbTempletBean.getIsToMainDB()) && isToMainDB(dbTempletBean.getIsToMainDB())) {
			return getMainDBExportTargetBean();
		}

		if (dbExportTagetBeans.isEmpty())
			loadDbExportTargetTemplet();

		StringBuffer keyName = new StringBuffer();
		keyName.append(task.getGroupId()).append("-").append(dbTempletBean.getDataType()).append("-").append(dbTempletBean.getId());
		return dbExportTagetBeans.get(keyName.toString());
	}
	
	/**
	 * 通过任务分组、输出数据类型、输出模版中ID查找数据库输出配置
	 * 
	 * @param task
	 * @param parqTempletBean
	 * @return 数据库输出配置
	 */
	public synchronized ExporterBean getParqExportTargetBean(Task task, ParqExportTemplateBean parqTempletBean) {
		// 输出到主库
		if (StringUtil.isNotEmpty(parqTempletBean.getIsToMainDB()) && isToMainDB(parqTempletBean.getIsToMainDB())) {
			return getMainDBExportTargetBean();
		}

		if (dbExportTagetBeans.isEmpty())
			loadDbExportTargetTemplet();

		StringBuffer keyName = new StringBuffer();
		keyName.append(task.getGroupId()).append("-").append(parqTempletBean.getDataType()).append("-").append(parqTempletBean.getId());
		return dbExportTagetBeans.get(keyName.toString());
	}
	
	/**
	 * 通过任务分组、输出数据类型、输出模版中ID查找数据库输出配置
	 * 
	 * @param task
	 * @param remoteFileTempletBean
	 * @return 数据库输出配置
	 */
	public synchronized ExporterBean getRemoteFileExportTargetBean(Task task, RemoteFileExportTemplateBean remoteFileTempletBean) {
		// 输出到主库
		if (StringUtil.isNotEmpty(remoteFileTempletBean.getIsToMainRemote()) && isToMainDB(remoteFileTempletBean.getIsToMainRemote())) {
			// TODO: 输出默认配置，待实现
			//return getMainDBExportTargetBean();
		}

		if (remoteFileExportTagetBeans.isEmpty())
			loadRemoteFileExportTargetTemplet();

		StringBuffer keyName = new StringBuffer();
		keyName.append(task.getGroupId()).append("-").append(remoteFileTempletBean.getDataType()).append("-").append(remoteFileTempletBean.getId());
		return remoteFileExportTagetBeans.get(keyName.toString());
	}

	/**
	 * 通过任务分组、输出数据类型、输出模版中ID查找数据库输出配置
	 * 
	 * @param task
	 * @param dbTempletBean
	 * @return 数据库输出配置
	 */
	public synchronized ExporterBean getSqlldrExportTargetBean(Task task, SqlldrExportTemplateBean dbTempletBean) {
		// 输出到主库
		if (StringUtil.isNotEmpty(dbTempletBean.getIsToMainDB()) && isToMainDB(dbTempletBean.getIsToMainDB())) {
			return getMainDBExportTargetBean();
		}

		if (dbExportTagetBeans.isEmpty())
			loadDbExportTargetTemplet();

		StringBuffer keyName = new StringBuffer();
		keyName.append(task.getGroupId()).append("-").append(dbTempletBean.getDataType()).append("-").append(dbTempletBean.getId());
		return dbExportTagetBeans.get(keyName.toString());
	}

	/**
	 * 从数据库加载数据库输出配置
	 */
	public void loadDbExportTargetTemplet() {
		ExportTargetDAO exportTargetDAO = AppContext.getBean("exportTargetDAO", ExportTargetDAO.class);
		dbExportTagetBeans.putAll(exportTargetDAO.loadDbExportTargetTemplet());
	}

	/**
	 * 从数据库加载文件输出配置
	 */
	public void loadRemoteFileExportTargetTemplet() {
		ExportTargetDAO exportTargetDAO = AppContext.getBean("exportTargetDAO", ExportTargetDAO.class);
		remoteFileExportTagetBeans.putAll(exportTargetDAO.loadRemoteFileExportTargetTemplet());
	}
		

	/**
	 * 从conf目录加载export_config.xml的配置<br>
	 * 
	 */
	public void loadFileExportTargetTemplet() {
		try {
			FileInputStream configIns = new FileInputStream(new File("conf/export_config.xml"));
			
			//TODO add by tyler for hadoop begin
			/*InputStream configIns = HDFSFileHelper.getInstance().getInputStream("conf/export_config.xml");*/
			//add by tyler for hadoop end
			
			Element configRootEle = new SAXReader().read(configIns).getRootElement();
			@SuppressWarnings("unchecked")
			// dom4j版本过低 不支持泛型 此处增加unchecked标注
			List<Element> exportConfigs = configRootEle.elements("export");
			for (Element exportTarget : exportConfigs) {
				String openFlag = exportTarget.attributeValue("on");
				if (!"true".equalsIgnoreCase(openFlag)) {
					continue;
				}
				// 目前根据配置中是否包含provider来判断是数据库还是文件 减少配置文件中增加配置
				Element providerElement = exportTarget.element("provider");
				Integer targetId = Integer.parseInt(exportTarget.attributeValue("id"));
				// 如果配置文件中ID定义重复 第二次出现将直接被忽略
				if (fileExportTagetBeans.containsKey(targetId)) {
					logger.warn("输出目的地export_config.xml Id定义重复!当前配置将被忽略! id=" + targetId);
					continue;
				}
				if (providerElement == null) {
					handleLocalExport(exportTarget, targetId);
					continue;
				}
				// handleDatabaseExport(exportTarget, targetId);
			}
		} catch (Exception e) {
			ImportantLogger.getLogger().warn("export_config.xml解析失败", e);
			ImportantLogger.getLogger().warn("程序即将停止!");
			System.exit(0);
		}
	}

	/**
	 * 解析数据库输出模版配置<br>
	 * 
	 * @param exportTarget
	 * @param targetId
	 * @return 解析数据库输出模版配置
	 */
	/*
	 * private void handleDatabaseExport(Element exportTarget, Integer targetId) { DBExportTargetBean exportTargetBean = new DBExportTargetBean();
	 * DatabaseConnectionInfo connectionInfo = new DatabaseConnectionInfo(); connectionInfo.setId(id) exportTargetBean.setId(targetId);
	 * exportTargetBean.setOpenFlag(true); exportTargetBean.setProvider(exportTarget .element("provider").attributeValue("value"));
	 * exportTargetBean.setUrl(exportTarget .element("url").attributeValue("value")); exportTargetBean.setUser(exportTarget
	 * .element("user").attributeValue("value")); exportTargetBean.setPwd(exportTarget .element("pwd").attributeValue("value")); if
	 * (exportTarget.element("insertBatchNum") != null) { try { exportTargetBean. setBatchNum(Integer.parseInt(exportTarget.element("insertBatchNum"
	 * ).attributeValue("value"))); } catch (Exception e) { } } fileExportTagetBeans.put(targetId, exportTargetBean); }
	 */

	/**
	 * @param exportTarget
	 * @param targetId
	 */
	private void handleLocalExport(Element exportTarget, Integer targetId) {
		FileExporterBean exportTargetBean = new FileExporterBean();
		exportTargetBean.setId(targetId);
		exportTargetBean.setOpenFlag(true);
		exportTargetBean.setPath(exportTarget.element("path").attributeValue("value"));
		exportTargetBean.setFileName(exportTarget.element("filename").attributeValue("value"));
		exportTargetBean.setZipFlag(Boolean.parseBoolean(exportTarget.element("zip").attributeValue("value")));
		exportTargetBean.setCompressFormat(exportTarget.element("zip").attributeValue("compressFormat"));
		if (exportTarget.element("header") != null && exportTarget.element("header").attributeValue("value") != null) {
			if (exportTarget.element("header").attributeValue("value").trim().equalsIgnoreCase("true"))
				exportTargetBean.setExportHeader(true);
		}

		Element ele = exportTarget.element("split");

		if (ele != null) {
			String split = ele.attributeValue("value");
			if (StringUtil.isNotEmpty(split))
				exportTargetBean.setSplit(split);
		}

		fileExportTagetBeans.put(targetId, exportTargetBean);
		localExportTargetList.add(exportTargetBean);
	}

	/**
	 * 判断是否输出到主库
	 * 
	 * @param isToMainDB
	 * @return
	 */
	public boolean isToMainDB(String isToMainDB) {
		return "true".equalsIgnoreCase(isToMainDB) || "yes".equalsIgnoreCase(isToMainDB) || "on".equalsIgnoreCase(isToMainDB);
	}

	/**
	 * 包装mainDBExportTargetBean,主库信息
	 * 
	 * @return ExporterBean
	 */
	public ExporterBean getMainDBExportTargetBean() {
		DatabaseExporterBean dbTempletBean_ = new DatabaseExporterBean();
		DatabaseConnectionInfo connectionInfo = new DatabaseConnectionInfo();
		connectionInfo.setId(-1000);
		connectionInfo.setDriver(connPool.getDriverClassName());
		connectionInfo.setUrl(connPool.getUrl());
		connectionInfo.setUserName(connPool.getUsername());
		connectionInfo.setPassword(connPool.getPassword());
		connectionInfo.setMaxActive(connPool.getMaxTotal());
		connectionInfo.setMaxWait((int) connPool.getMaxWaitMillis()/1000);
		dbTempletBean_.setConnectionInfo(connectionInfo);
		dbTempletBean_.setOpenFlag(true);
		dbTempletBean_.setBatchNum(1000);
		return dbTempletBean_;
	}
}
