package cn.uway.ucloude.rpc;

import java.io.Serializable;

import cn.uway.ucloude.rpc.exception.RpcCommandFieldCheckException;

public interface RpcCommandBody extends Serializable {
	public void checkFields() throws RpcCommandFieldCheckException;
}
