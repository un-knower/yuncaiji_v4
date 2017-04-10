package cn.uway.ucloude.rpc.netty;

import cn.uway.ucloude.configuration.BasicConfiguration;
import cn.uway.ucloude.rpc.RpcClient;
import cn.uway.ucloude.rpc.RpcServer;
import cn.uway.ucloude.rpc.RpcTransporter;
import cn.uway.ucloude.rpc.configuration.ClientConfiguration;
import cn.uway.ucloude.rpc.configuration.ServerConfiguration;

public class NettyRpcTransporter implements RpcTransporter {

	@Override
	public RpcServer getRpcServer(BasicConfiguration configuration, ServerConfiguration serverConfiguration) {
		// TODO Auto-generated method stub
		return new NettyRpcServer(configuration, serverConfiguration);
	}

	@Override
	public RpcClient getRpcClient(BasicConfiguration configuration, ClientConfiguration clientConfiguration) {
		// TODO Auto-generated method stub
		return new NettyRpcClient(configuration, clientConfiguration);
	}

}
