package cn.uway.ucloude.rpc;

import cn.uway.ucloude.configuration.BasicConfiguration;
import cn.uway.ucloude.container.SPI;
import cn.uway.ucloude.rpc.configuration.ClientConfiguration;
import cn.uway.ucloude.rpc.configuration.ServerConfiguration;

@SPI(key = "ucloude.rpc", dftValue = "netty")
public interface RpcTransporter {
	RpcServer getRpcServer(BasicConfiguration configuration, ServerConfiguration serverConfiguration);
	
	RpcClient getRpcClient(BasicConfiguration configuration, ClientConfiguration clientConfiguration);
}
