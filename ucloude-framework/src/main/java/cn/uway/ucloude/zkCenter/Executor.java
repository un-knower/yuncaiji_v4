package cn.uway.ucloude.zkCenter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class Executor
    implements Watcher, Runnable, DataMonitor.DataMonitorListener
{
    String znode;

    volatile DataMonitor dm;

    ZooKeeper zk;

    String filename;

    String exec[];

    Process child;

    public Executor(String hostPort, String znode, String filename,
            String exec[]) throws KeeperException, IOException {
        this.filename = filename;
        this.exec = exec;
        zk = new ZooKeeper(hostPort, 3000, this);
        dm = new DataMonitor(zk, znode, null, this);
    }

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
    	String str = "8613530055371";
    	str = str.substring(2,13);
    	System.out.println(str);
    	
    	
//    	Runtime.getRuntime().exec("ls /home/shig");
//    	
//    	if (args.length < 4) {
//            //"192.168.15.196"
//            args = new String[] {"cdh1:2181,cdh2:2181", "/ucloude", "/home/shig/zk.log", "hostname"}; 
//        }
//        String hostPort = args[0];
//        String znode = args[1];
//        String filename = args[2];
//        String exec[] = new String[args.length - 3];
//        System.arraycopy(args, 3, exec, 0, exec.length);
//        try {
//            new Executor(hostPort, znode, filename, exec).run();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    	
    	ZKCenter instance = ZKCenter.instance();
    	//System.out.println(instance.z ("/ucloude/nodes/1", CreateMode.EPHEMERAL, "1".getBytes()));
    	System.out.println(instance.createZNode("/ucloude/nodes/1", CreateMode.EPHEMERAL, "1".getBytes()));
    	System.out.println(instance.createZNode("/ucloude/nodes/2", CreateMode.EPHEMERAL, "2".getBytes()));
    	System.out.println(instance.createZNode("/ucloude/nodes/3", CreateMode.EPHEMERAL, "3".getBytes()));
    	
    	System.out.println("finished.");
    }

    /***************************************************************************
     * We do process any events ourselves, we just need to forward them on.
     *
     * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.proto.WatcherEvent)
     */
    public void process(WatchedEvent event) {
    	System.out.println("Executor process() path:" + event.getPath() + " eventType:" + event.getType() + " state:" + event.getState().toString());
    	if (dm != null) {
        	dm.process(event);
        }
    }

    public void run() {
        try {
            synchronized (this) {
                while (!dm.dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
        }
    }

    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }

    static class StreamWriter extends Thread {
        OutputStream os;

        InputStream is;

        StreamWriter(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            start();
        }

        public void run() {
            byte b[] = new byte[80];
            int rc;
            try {
                while ((rc = is.read(b)) > 0) {
                    os.write(b, 0, rc);
                }
            } catch (IOException e) {
            }

        }
    }

    public void exists(byte[] data) {
        if (data == null) {
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                }
            }
            child = null;
        } else {
            if (child != null) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}