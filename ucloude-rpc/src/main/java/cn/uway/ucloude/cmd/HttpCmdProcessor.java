package cn.uway.ucloude.cmd;

/**
 * Cmd处理器
 * @author uway
 *
 */
public interface HttpCmdProcessor {
	String nodeIdentity();

    String getCommand();

    HttpCmdResponse execute(HttpCmdRequest request) throws Exception;
}
