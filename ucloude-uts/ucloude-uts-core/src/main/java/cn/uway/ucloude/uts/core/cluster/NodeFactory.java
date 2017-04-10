package cn.uway.ucloude.uts.core.cluster;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.utils.NetUtils;
import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.core.exception.UtsRuntimeException;

public class NodeFactory {
	public static <T extends Node> T create(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new UtsRuntimeException("Create Node error: clazz=" + clazz, e);
        }
    }

    public static void build(Node node, UtsConfiguration config) {
        node.setCreateTime(SystemClock.now());
        node.setIp(config.getIp());
        node.setHostName(NetUtils.getLocalHostName());
        node.setGroup(config.getNodeGroup());
        node.setThreads(config.getWorkThreads());
        node.setPort(config.getListenPort());
        node.setIdentity(config.getIdentity());
        node.setClusterName(config.getClusterName());
    }
}
