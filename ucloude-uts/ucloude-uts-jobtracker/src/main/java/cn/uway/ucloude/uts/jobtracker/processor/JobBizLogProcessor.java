package cn.uway.ucloude.uts.jobtracker.processor;

import java.util.List;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.LogType;
import cn.uway.ucloude.uts.core.domain.BizLog;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.command.BizLogSendRequest;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobBizLogProcessor extends AbstractRpcProcessor {

	
	public JobBizLogProcessor(JobTrackerContext context) {
		super(context);
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {
		// TODO Auto-generated method stub
		BizLogSendRequest requestBody = request.getBody();
		List<BizLog> bizLogs = requestBody.getBizLogs();
		if(CollectionUtil.isEmpty(bizLogs)){
			for(BizLog bizLog:bizLogs){
				JobLogPo jobLogPo = new JobLogPo();
                jobLogPo.setGmtCreated(SystemClock.now());
                jobLogPo.setLogTime(bizLog.getLogTime());
                jobLogPo.setTaskTrackerNodeGroup(bizLog.getTaskTrackerNodeGroup());
                jobLogPo.setTaskTrackerIdentity(bizLog.getTaskTrackerIdentity());
                jobLogPo.setJobId(bizLog.getJobId());
                jobLogPo.setTaskId(bizLog.getTaskId());
                jobLogPo.setRealTaskId(bizLog.getRealTaskId());
                jobLogPo.setJobType(bizLog.getJobType());
                jobLogPo.setMsg(bizLog.getMsg());
                jobLogPo.setSuccess(true);
                jobLogPo.setLevel(bizLog.getLevel());
                jobLogPo.setLogType(LogType.BIZ);
                context.getJobLogger().log(jobLogPo);
			}
		}
		return RpcCommand.createResponseCommand(JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code(), "");
	}

}
