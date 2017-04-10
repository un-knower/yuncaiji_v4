package cn.uway.ucloude.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import cn.uway.ucloude.utils.Assert;

/**
 * Http通信上下文
 * @author uway
 *
 */
public class HttpCmdContext {
	private ReentrantLock lock = new ReentrantLock();
    private final Map<String/*节点标识*/, Map<String/*cmd*/, HttpCmdProcessor>>
            NODE_PROCESSOR_MAP = new HashMap<String, Map<String, HttpCmdProcessor>>();

    public void addCmdProcessor(HttpCmdProcessor proc) {
        if (proc == null) {
            throw new IllegalArgumentException("proc can not be null");
        }

        String identity = proc.nodeIdentity();
        Assert.hasText(identity, "nodeIdentity can't be empty");

        String command = proc.getCommand();
        Assert.hasText(command, "command can't be empty");

        Map<String, HttpCmdProcessor> cmdProcessorMap = NODE_PROCESSOR_MAP.get(identity);
        if (cmdProcessorMap == null) {
            lock.lock();
            cmdProcessorMap = NODE_PROCESSOR_MAP.get(identity);
            if (cmdProcessorMap == null) {
                cmdProcessorMap = new ConcurrentHashMap<String, HttpCmdProcessor>();
                NODE_PROCESSOR_MAP.put(identity, cmdProcessorMap);
            }
            lock.unlock();
        }
        cmdProcessorMap.put(command, proc);
    }

    public HttpCmdProcessor getCmdProcessor(String nodeIdentity, String command) {
        Assert.hasText(nodeIdentity, "nodeIdentity can't be empty");

        Map<String, HttpCmdProcessor> cmdProcessorMap = NODE_PROCESSOR_MAP.get(nodeIdentity);
        if (cmdProcessorMap == null) {
            return null;
        }
        return cmdProcessorMap.get(command);
    }

}
