package cn.uway.ucloude.uts.zookeeper.zkclient;

import cn.uway.ucloude.uts.zookeeper.ZkClient;
import cn.uway.ucloude.uts.zookeeper.ZookeeperTransporter;

public class ZkClientZookeeperTransporter implements ZookeeperTransporter {

	@Override
	public ZkClient connect(String registryAddress) {
		// TODO Auto-generated method stub
		return new ZkClientZkClient(registryAddress);
	}

}
