package cn.uway.ucloude.uts.zookeeper;

import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.container.SPI;

@SPI(key = ExtConfigKeys.ZK_CLIENT_KEY, dftValue = "zkclient")
public interface ZookeeperTransporter {
	ZkClient connect(String registryAddress);
}
