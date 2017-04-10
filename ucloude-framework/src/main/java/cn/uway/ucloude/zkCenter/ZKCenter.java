package cn.uway.ucloude.zkCenter;

import java.io.IOException;

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.NetUtils;

public class ZKCenter implements Watcher, StatCallback, Runnable {

	public static class ZKCenterInstance {

		public static ZKCenter instance = new ZKCenter();
	}

	private static final ILogger LOGGER = LoggerManager
			.getLogger(ZKCenter.class);

	private static final String ZNODE_ROOT_NAME = "/ucloude";

	private static final String ZNODE_MONITOR_ROOT_NAME = ZNODE_ROOT_NAME
			+ "/nodes";

	private volatile ZooKeeper zk;
	
	private volatile boolean dead;

	private String hostPort;
	
	public static ZKCenter instance() {
		//return ZKCenterInstance.instance;
		return new ZKCenter();
	}

	public ZKCenter() {
		// TODO 获取系统的配置
		hostPort = "cdh1:2181,cdh2:2181";
		init(hostPort);
	}

	public void close() {
		if (this.zk != null) {
			try {
				this.zk.close();
			} catch (InterruptedException e) {
				LOGGER.warn(
						"ZKCenter:destory() close ZooKepper has error ocurred.",
						e);
			}
			this.zk = null;
		}
		
        synchronized (this) {
            notifyAll();
        }
	}
	
	public void startMonitor() {
		if (zk == null) {
			LOGGER.debug("ZKCenter start faild.");
			return;
		}
		
		Thread monitorThread = new Thread(this);
		monitorThread.start();
		
		zk.exists(ZNODE_MONITOR_ROOT_NAME, true, this, this);
	}

	public synchronized void init(String hostPort) {
		this.hostPort = hostPort;
		try {
			zk = new ZooKeeper(hostPort, 3000, this);
			if (!createZNode(ZNODE_MONITOR_ROOT_NAME, CreateMode.PERSISTENT, ZNODE_MONITOR_ROOT_NAME.getBytes())) {
				LOGGER.error("ZooKeeper初始化根结点. znode=" + ZNODE_MONITOR_ROOT_NAME);
				return;
			}
			
		} catch (IOException e) {
			LOGGER.error("ZooKeeper初始化失败. hostPort=" + hostPort, e);
			this.zk = null;
		}
	}

	public synchronized boolean registerAppHost() {
		String hostName = NetUtils.getLocalHostName();
		String ip = NetUtils.getLocalAddress().getHostAddress().toString();
		
		String zNode = ZNODE_MONITOR_ROOT_NAME + "//" + hostName;
		if (!createZNode(zNode, CreateMode.EPHEMERAL, ip.getBytes()))
			return false;
		
		return true;
	}

	public boolean createZNode(String zNode, CreateMode mode, byte[] byteData) {
		if (zk == null)
			return false;

		try {
			Stat stat = zk.exists(zNode, false);
			if (stat == null) {
				zk.create(zNode, byteData, Ids.OPEN_ACL_UNSAFE, mode);
			}
			
			return true;
		} catch (KeeperException e) {
			LOGGER.warn("createZNode has error ocureed. zNode=" + zNode, e);
		} catch (InterruptedException e) {
			LOGGER.warn("createZNode has error ocureed. zNode=" + zNode, e);
		}
		
		return false;
	}
	
	public boolean setZNodeValue(String zNode, byte[] data) {
		if (zk == null) {
			LOGGER.warn("setZNodeValue has error ocureed. zk==null");
			return false;
		}
		
		try {
			Stat stat = zk.setData(zNode, data, 0);
			if (stat != null) 
				return true;
			
		} catch (KeeperException e) {
			LOGGER.warn("setZNodeValue has error ocureed. zNode=" + zNode, e);
		} catch (InterruptedException e) {
			LOGGER.warn("setZNodeValue has error ocureed. zNode=" + zNode, e);
		}
		
		return false;
	}
	
	public byte[] getZNodeValue(String zNode) {
		if (zk == null) {
			LOGGER.warn("getZNodeValue has error ocureed. zk==null");
			return null;
		}
		
		try {
			byte[] data = zk.getData(zNode, false, null);
			return data;
		} catch (KeeperException e) {
			LOGGER.warn("setZNodeValue has error ocureed. zNode=" + zNode, e);
		} catch (InterruptedException e) {
			LOGGER.warn("setZNodeValue has error ocureed. zNode=" + zNode, e);
		}
		
		return null;
	}

	@Override
	public void process(WatchedEvent event) {
		String path = event.getPath();
		LOGGER.warn("ZKCenter process() path:" + path + " eventType:" + event.getType() + " state:" + event.getState().toString());
		
		if (event.getType() == Event.EventType.None) {
			// We are are being told that the state of the
			// connection has changed
			switch (event.getState()) {
				case SyncConnected :
					// In this particular example we don't need to do anything
					// here - watches are automatically re-registered with
					// server and any watches triggered while the client was
					// disconnected will be delivered (in order of course)
					break;
				case Expired :
					// It's all over
					dead = true;
					close();
				default :
					break;
			}
		} else {
			if (path != null && path.equals(ZNODE_MONITOR_ROOT_NAME)) {
				// Something has changed on the node, let's find out
				zk.exists(ZNODE_MONITOR_ROOT_NAME, true, this, null);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void processResult(int rc, String path, Object ctx, Stat stat) {
		boolean exists = false;
		switch (rc) {
			case Code.Ok :
				exists = true;
				break;
			case Code.NoNode :
				exists = false;
				break;
			case Code.SessionExpired :
			case Code.NoAuth :
				dead = true;
				break;
			default :
				// Retry errors
				LOGGER.error("zk.exists");
				zk.exists(ZNODE_MONITOR_ROOT_NAME, true, this, this);
				return;
		}
		
		if (dead) {
			close();
		}
		
		LOGGER.error("ZKCenter processResult() path:{} exists:{} rc:{} stat:{}", new Object[]{path, exists, rc, stat.toString()});
	}

	@Override
	public void run() {
		try {
            synchronized (this) {
                while (!dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
        }
        
        LOGGER.error("ZKCenter monitor thread has terminate");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ZKCenter instance = ZKCenter.instance();
		instance.startMonitor();
	}
}
