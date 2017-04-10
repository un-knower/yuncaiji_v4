package cn.uway.ucloude.uts.core.protocol.command;

import java.util.List;

import cn.uway.ucloude.rpc.exception.RpcCommandFieldCheckException;

public class JobAskRequest extends AbstractRpcCommandBody {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8915546303981805483L;

	List<String> jobIds;

    public List<String> getJobIds() {
        return jobIds;
    }

    public void setJobIds(List<String> jobIds) {
        this.jobIds = jobIds;
    }

    @Override
    public void checkFields() throws RpcCommandFieldCheckException {
        if (jobIds == null || jobIds.size() == 0) {
            throw new RpcCommandFieldCheckException("jobIds could not be empty");
        }
    }
}
