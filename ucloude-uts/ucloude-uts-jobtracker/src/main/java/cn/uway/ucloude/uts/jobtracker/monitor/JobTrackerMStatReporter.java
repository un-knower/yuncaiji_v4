package cn.uway.ucloude.uts.jobtracker.monitor;

import java.util.concurrent.atomic.AtomicLong;

import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.domain.monitor.JobTrackerMData;
import cn.uway.ucloude.uts.core.domain.monitor.MData;
import cn.uway.ucloude.uts.core.monitor.AbstractMStatReporter;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobTrackerMStatReporter extends AbstractMStatReporter {
	
	public JobTrackerMStatReporter(JobTrackerContext context){
		super(context);
	}
	  // 接受的任务数
    private AtomicLong receiveJobNum = new AtomicLong(0);
    // 分发出去的任务数
    private AtomicLong pushJobNum = new AtomicLong(0);
    // 执行成功个数
    private AtomicLong exeSuccessNum = new AtomicLong(0);
    // 执行失败个数
    private AtomicLong exeFailedNum = new AtomicLong(0);
    // 延迟执行个数
    private AtomicLong exeLaterNum = new AtomicLong(0);
    // 执行异常个数
    private AtomicLong exeExceptionNum = new AtomicLong(0);
    // 修复死任务数
    private AtomicLong fixExecutingJobNum = new AtomicLong(0);
    
    public void incReceiveJobNum() {
        receiveJobNum.incrementAndGet();
    }

    public void incPushJobNum() {
        pushJobNum.incrementAndGet();
    }

    public void incPushJobNum(int num) {
        for (int i = 0; i < num; i++) {
            pushJobNum.incrementAndGet();
        }
    }

    public void incExeSuccessNum() {
        exeSuccessNum.incrementAndGet();
    }

    public void incExeFailedNum() {
        exeFailedNum.incrementAndGet();
    }

    public void incExeLaterNum() {
        exeLaterNum.incrementAndGet();
    }

    public void incExeExceptionNum() {
        exeExceptionNum.incrementAndGet();
    }

    public void incFixExecutingJobNum() {
        fixExecutingJobNum.incrementAndGet();
    }

	@Override
	protected MData collectMData() {
		// TODO Auto-generated method stub
		JobTrackerMData mData = new JobTrackerMData();
        mData.setReceiveJobNum(receiveJobNum.getAndSet(0));
        mData.setExeExceptionNum(exeExceptionNum.getAndSet(0));
        mData.setExeFailedNum(exeFailedNum.getAndSet(0));
        mData.setExeSuccessNum(exeSuccessNum.getAndSet(0));
        mData.setExeLaterNum(exeLaterNum.getAndSet(0));
        mData.setFixExecutingJobNum(fixExecutingJobNum.getAndSet(0));
        mData.setPushJobNum(pushJobNum.getAndSet(0));
		return mData;
	}

	@Override
	protected NodeType getNodeType() {
		// TODO Auto-generated method stub
		return NodeType.JOB_TRACKER;
	}


}
