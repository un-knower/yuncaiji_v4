package cn.uway.ucloude.uts.jobclient.support;

import java.util.concurrent.atomic.AtomicLong;

import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.domain.monitor.JobClientMData;
import cn.uway.ucloude.uts.core.domain.monitor.MData;
import cn.uway.ucloude.uts.core.monitor.AbstractMStatReporter;

public class JobClientMStatReporter extends AbstractMStatReporter {

	
	public JobClientMStatReporter(UtsContext context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    // 提交成功的个数
    private AtomicLong submitSuccessNum = new AtomicLong(0);
    // 提交失败的个数
    private AtomicLong submitFailedNum = new AtomicLong(0);
    // 存储FailStore的个数s
    private AtomicLong failStoreNum = new AtomicLong(0);
    // 提交FailStore的个数
    private AtomicLong submitFailStoreNum = new AtomicLong(0);
    // 处理的反馈的个数
    private AtomicLong handleFeedbackNum = new AtomicLong(0);

    public void incSubmitSuccessNum(int num) {
        submitSuccessNum.addAndGet(num);
    }

    public void incSubmitFailedNum(int num) {
        submitFailedNum.addAndGet(num);
    }

    public void incFailStoreNum() {
        failStoreNum.incrementAndGet();
    }

    public void incSubmitFailStoreNum(int num) {
        submitFailStoreNum.addAndGet(num);
    }

    public void incHandleFeedbackNum(int num) {
        handleFeedbackNum.addAndGet(num);
    }
	@Override
	protected MData collectMData() {
        JobClientMData mData = new JobClientMData();
        mData.setSubmitSuccessNum(submitSuccessNum.getAndSet(0));
        mData.setSubmitFailedNum(submitFailedNum.getAndSet(0));
        mData.setFailStoreNum(failStoreNum.getAndSet(0));
        mData.setSubmitFailStoreNum(submitFailStoreNum.getAndSet(0));
        mData.setHandleFeedbackNum(handleFeedbackNum.getAndSet(0));
        return mData;
	}

	@Override
	protected NodeType getNodeType() {
		// TODO Auto-generated method stub
		return NodeType.JOB_CLIENT;
	}
	
}
