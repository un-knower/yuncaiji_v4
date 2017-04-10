package cn.uway.ucloude.zkCenter;

/**
 * A simple class that monitors the data and existence of a ZooKeeper
 * node. It uses asynchronous ZooKeeper APIs.
 */
import java.util.List;

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class DataMonitor implements Watcher, StatCallback {

	/**
	 * Other classes use the DataMonitor by implementing this method
	 */
	public interface DataMonitorListener {

		/**
		 * The existence status of the node has changed.
		 */
		void exists(byte data[]);

		/**
		 * The ZooKeeper session is no longer valid.
		 * 
		 * @param rc
		 *            the ZooKeeper reason code
		 */
		void closing(int rc);
	}

	ZooKeeper zk;

	String znode;

	Watcher chainedWatcher;

	boolean dead;

	DataMonitorListener listener;

	byte prevData[];

	public DataMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher,
			DataMonitorListener listener) {
		this.zk = zk;
		this.znode = znode;
		this.chainedWatcher = chainedWatcher;
		this.listener = listener;
		// Get things started by checking if the node exists. We are going
		// to be completely event driven
		try {
			//List<String> nodeList = zk.getChildren("/hbase/", false);
			Stat stat = zk.exists(znode, false);
			if (stat == null) {
				String result = zk.create(znode, "hello word".getBytes(),
						Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				System.out.println("create znode:" + result);
			}
			List<String> nodes = zk.getChildren(znode, false);
			if (nodes != null) {
				System.out.println("path:" + znode + " items:" + nodes.toString());
			}
			byte[] content = zk.getData(znode, false, stat);
			System.out.println("znode:" + znode + " content:" + new String(content));
			// zk.setData(znode, "hello word2".getBytes(), 1);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		zk.exists(znode, true, this, null);
	}

	public void process(WatchedEvent event) {
		String path = event.getPath();
		
		System.out.println("DataMonitor process() path:" + path + " eventType:" + event.getType() + " state:" + event.getState().toString());
		
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
					listener.closing(KeeperException.Code.SessionExpired);
					break;
			}
		} else {
			if (path != null && path.equals(znode)) {
				// Something has changed on the node, let's find out
				zk.exists(znode, true, this, null);
			}
		}
		if (chainedWatcher != null) {
			chainedWatcher.process(event);
		}
	}

	@SuppressWarnings("deprecation")
	public void processResult(int rc, String path, Object ctx, Stat stat) {
		System.out.println("DataMonitor processResult() path:" + path + " rc:" + rc + " stat:" + stat);
		
		boolean exists;
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
				listener.closing(rc);
				return;
			default :
				// Retry errors
				zk.exists(znode, true, this, null);
				return;
		}
//
//		byte b[] = null;
//		if (exists) {
//			try {
//				b = zk.getData(znode, false, null);
//			} catch (KeeperException e) {
//				// We don't need to worry about recovering now. The watch
//				// callbacks will kick off any exception handling
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				return;
//			}
//		}
//		if ((b == null && b != prevData)
//				|| (b != null && !Arrays.equals(prevData, b))) {
//			listener.exists(b);
//			prevData = b;
//		}
	}
}