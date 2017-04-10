package cn.uway.ucloude.uts.jobclient.support;

import java.util.List;

import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.exception.JobSubmitException;

public interface JobSubmitExecutor<T> {
	T execute(List<Job> jobs) throws JobSubmitException;
}
