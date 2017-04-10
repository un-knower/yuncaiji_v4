package cn.uway.ucloude.uts.jobtracker.cmd;

import cn.uway.ucloude.cmd.HttpCmdProcessor;
import cn.uway.ucloude.cmd.HttpCmdRequest;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.support.ClientNotifier;

/**
 * 一些系统配置更改CMD
 * @author uway
 *
 */
public class SysConfigModifyHttpCmd implements HttpCmdProcessor{

	 @Override
	    public String nodeIdentity() {
	        return null;
	    }

	    @Override
	    public String getCommand() {
	        return null;
	    }

	    @Override
	    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {
	        return null;
	    }

}
