package cn.uway.ucloude.uts.spring.tasktracker;


import java.lang.reflect.Method;

import cn.uway.ucloude.uts.core.domain.Action;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.tasktracker.Result;
import cn.uway.ucloude.uts.tasktracker.runner.JobContext;
import cn.uway.ucloude.uts.tasktracker.runner.JobRunner;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 */
public class JobRunnerBuilder {

    public static JobRunner build(final Object targetObject, final Method targetMethod, final Class<?>[] pTypes) {

        return new JobRunner() {
            @Override
            public Result run(JobContext jobContext) throws Throwable {
                if (pTypes == null || pTypes.length == 0) {
                    return (Result) targetMethod.invoke(targetObject);
                }
                Object[] pTypeValues = new Object[pTypes.length];

                for (int i = 0; i < pTypes.length; i++) {
                    if (pTypes[i] == Job.class) {
                        pTypeValues[i] = jobContext.getJob();
                    } else if (pTypes[i] == JobContext.class) {
                        pTypeValues[i] = jobContext;
                    } else {
                        pTypeValues[i] = null;
                    }
                }
                Class<?> returnType = targetMethod.getReturnType();
                if (returnType != Result.class) {
                    return new Result(Action.EXECUTE_SUCCESS);
                }
                return (Result) targetMethod.invoke(targetObject, pTypeValues);
            }
        };
    }
}
