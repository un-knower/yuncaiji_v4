package cn.uway.ucloude.rpc.protocal;

public class RpcCommandHelper {
	private static final int RPC_TYPE = 0; // 0, REQUEST_COMMAND
    private static final int RPC_ONEWAY = 1; // 1, RPC

    public static void markResponseType(RpcCommand RpcCommand) {
        int bits = 1 << RPC_TYPE;
        RpcCommand.setFlag(RpcCommand.getFlag() | bits);
    }

    public static boolean isResponseType(RpcCommand RpcCommand) {
        int bits = 1 << RPC_TYPE;
        return (RpcCommand.getFlag() & bits) == bits;
    }

    public static void markOnewayRPC(RpcCommand RpcCommand) {
        int bits = 1 << RPC_ONEWAY;
        RpcCommand.setFlag(RpcCommand.getFlag() | bits);
    }

    public static boolean isOnewayRPC(RpcCommand RpcCommand) {
        int bits = 1 << RPC_ONEWAY;
        return (RpcCommand.getFlag() & bits) == bits;
    }

    public static RpcCommandType getRpcCommandType(RpcCommand RpcCommand) {
        if (RpcCommandHelper.isResponseType(RpcCommand)) {
            return RpcCommandType.RESPONSE_COMMAND;
        }
        return RpcCommandType.REQUEST_COMMAND;
    }
}
