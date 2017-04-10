package cn.uway.ucloude.uts.core.cluster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.EcTopic;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.listener.MasterChangeListener;

public class MasterElector {
	private static final ILogger logger = LoggerManager.getLogger(MasterElector.class);

	/**
	 * 上下文
	 */
	private UtsContext context;

	/**
	 * 主节点变化监听器
	 */
	private List<MasterChangeListener> listeners;

	private Node master;

	/**
	 * 节点变化锁
	 */
	private ReentrantLock lock = new ReentrantLock();

	public MasterElector(UtsContext context) {
		this.context = context;
	}

	public void addMasterChangeListener(List<MasterChangeListener> masterChangeListeners) {
		if (listeners == null)
			listeners = new CopyOnWriteArrayList<MasterChangeListener>();
		if (CollectionUtil.isNotEmpty(masterChangeListeners))
			listeners.addAll(masterChangeListeners);
	}

	/**
	 * 添加节点组
	 * @param nodes
	 */
	public void addNodes(List<Node> nodes) {
		lock.lock();
		try {
			Node newMaster = null;
			for (Node node : nodes) {
				if (newMaster == null) {
					newMaster = node;
				} else {
					if (newMaster.getCreateTime()> node.getCreateTime())
						newMaster = node;
				}
			}
			addNode(newMaster);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 当前节点是否是master
	 * 
	 * @return
	 */
	public boolean isCurrentMaster() {
		return master != null && master.getIdentity().equals(context.getConfiguration().getIdentity());
	}

	/**
	 * 添加节点
	 * @param newNode
	 */
	public void addNode(Node newNode) {
		lock.lock();
		try {
			if (master == null) {
				master = newNode;
				notifyListener();
			} else {
				if (master.getCreateTime() > newNode.getCreateTime()) {
					master = newNode;
					notifyListener();
				}
			}
		} finally {
			lock.unlock();
		}
	}

	private void notifyListener() {

		boolean isMaster = false;
		if (context.getConfiguration().getIdentity().equals(master.getIdentity())) {
			logger.info("Current node become the master node:{}", master);
			isMaster = true;
		} else {
			logger.info("Master node is :{}", master);
			isMaster = false;
		}

		if (listeners != null) {
			for (MasterChangeListener masterChangeListener : listeners) {
				try {
					masterChangeListener.change(master, isMaster);
				} catch (Throwable t) {
					logger.error("MasterChangeListener notify error!", t);
				}
			}
		}
		EventInfo eventInfo = new EventInfo(EcTopic.MASTER_CHANGED);
		eventInfo.setParam("master", master);
		context.getEventCenter().publishSync(eventInfo);

	}

	/**
	 * 移除节点
	 * @param removedNodes
	 */
	public void removeNode(List<Node> removedNodes) {
		lock.lock();
		try {
			if (master != null) {
				boolean masterRemoved = false;
				for (Node removedNode : removedNodes) {
					if (master.getIdentity().equals(removedNode.getIdentity())) {
						masterRemoved = true;
					}
				}
				if (masterRemoved) {
					// 如果挂掉的是master, 需要重新选举
					List<Node> nodes = context.getSubscribedNodeManager().getNodeList(
							context.getConfiguration().getNodeType(), context.getConfiguration().getNodeGroup());
					if (CollectionUtil.isNotEmpty(nodes)) {
						Node newMaster = null;
						for (Node node : nodes) {
							if (newMaster == null) {
								newMaster = node;
							} else {
								if (newMaster.getCreateTime() > node.getCreateTime()) {
									newMaster = node;
								}
							}
						}
						master = newMaster;
						notifyListener();
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

}
