package cn.uway.ucloude.uts.jobtracker.sender;

public enum JobPushResult {
	NO_JOB, // 没有任务可执行
    SUCCESS, //推送成功
    FAILED,      //推送失败
    SENT_ERROR
}
