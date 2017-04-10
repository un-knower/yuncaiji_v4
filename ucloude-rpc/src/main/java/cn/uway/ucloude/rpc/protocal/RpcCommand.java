package cn.uway.ucloude.rpc.protocal;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import cn.uway.ucloude.rpc.RpcCommandBody;
import cn.uway.ucloude.serialize.JsonConvert;

/**
 * Rpc模块中，服务器与客户端通过传递RpcCommand来交互,远程通信命令行
 * @author uway
 *
 */
public class RpcCommand implements Serializable{
	private static final long serialVersionUID = -6424506729433386206L;
	private static final AtomicInteger requestId = new AtomicInteger(0);
	
	/**
     * Header 部分
     */
    private int code;
    private int subCode;
    private int version = 0;
    private int opaque;
    private int flag = 0;
    private String remark;
    private int sid = -1;   // serializableTypeId
    /**
     * body
     */
    private transient RpcCommandBody body;

    private RpcCommand() {

    }

    public static RpcCommand createRequestCommand(int code, RpcCommandBody body) {
        RpcCommand cmd = new RpcCommand();
        cmd.setCode(code);
        cmd.setBody(body);
        cmd.setOpaque(requestId.getAndIncrement());
        return cmd;
    }

    public static RpcCommand createResponseCommand(int code, String remark, RpcCommandBody body) {
        RpcCommand cmd = new RpcCommand();
        RpcCommandHelper.markResponseType(cmd);
        cmd.setCode(code);
        cmd.setRemark(remark);
        cmd.setBody(body);
        cmd.setOpaque(requestId.getAndIncrement());
        return cmd;
    }

    public static RpcCommand createResponseCommand(int code, RpcCommandBody body) {
        return createResponseCommand(code, null, body);
    }

    public static RpcCommand createResponseCommand(int code) {
        return createResponseCommand(code, null, null);
    }

    public static RpcCommand createResponseCommand(int code, String remark) {
        return createResponseCommand(code, remark, null);
    }

    public void setBody(RpcCommandBody body) {
        this.body = body;
    }

    @SuppressWarnings("unchecked")
	public <T extends RpcCommandBody> T getBody() {
        return (T) body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public int getFlag() {
        return flag;
    }

    public int getSubCode() {
        return subCode;
    }

    public void setSubCode(int subCode) {
        this.subCode = subCode;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    @Override
    public String toString() {
        return "RpcCommand{" +
                "code=" + code +
                ", subCode=" + subCode +
                ", version=" + version +
                ", opaque=" + opaque +
                ", flag=" + flag +
                ", remark='" + remark + '\'' +
                ", sid='" + sid + '\'' +
                ", body=" + JsonConvert.serialize(body) +
                '}';
    }
}
