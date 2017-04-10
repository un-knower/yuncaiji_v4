package cn.uway.ucloude.uts.jobclient.support;

import cn.uway.ucloude.uts.core.exception.JobSubmitException;

public class JobSubmitProtectException extends JobSubmitException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5978691375181647651L;
	int concurrentSize;

    public JobSubmitProtectException(int concurrentSize) {
        super();
        this.concurrentSize = concurrentSize;
    }

    public JobSubmitProtectException(int concurrentSize, String message) {
        super(message);
        this.concurrentSize = concurrentSize;
    }

    public JobSubmitProtectException(int concurrentSize, String message, Throwable cause) {
        super(message, cause);
        this.concurrentSize = concurrentSize;
    }

    public JobSubmitProtectException(int concurrentSize, Throwable cause) {
        super(cause);
        this.concurrentSize = concurrentSize;
    }
}
