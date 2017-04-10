package cn.uway.ucloude.uts.core.protocol.command;

import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.core.UtsContext;

public class CommandBodyWrapper {
	private UtsConfiguration configuration;
	
	public CommandBodyWrapper(UtsConfiguration configuration){
		this.configuration = configuration;
	}
	
	 public <T extends AbstractRpcCommandBody> T wrapper(T commandBody) {
	        commandBody.setNodeGroup(configuration.getNodeGroup());
	        commandBody.setNodeType(configuration.getNodeType().name());
	        commandBody.setIdentity(configuration.getIdentity());
	        return commandBody;
	    }

	    public static <T extends AbstractRpcCommandBody> T wrapper(UtsContext context, T commandBody) {
	        return context.getCommandBodyWrapper().wrapper(commandBody);
	    }
}
