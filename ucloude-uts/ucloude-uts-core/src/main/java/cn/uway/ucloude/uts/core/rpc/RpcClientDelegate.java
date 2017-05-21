package cn.uway.ucloude.uts.core.rpc;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.ec.EventInfo;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.AsyncCallback;
import cn.uway.ucloude.rpc.RpcClient;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.rpc.exception.RpcException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.uts.core.EcTopic;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.exception.JobTrackerNotFoundException;
import cn.uway.ucloude.uts.core.loadbalance.LoadBalance;

public class RpcClientDelegate {

	private static final ILogger LOGGER = LoggerManager.getLogger(RpcClientDelegate.class);

	private RpcClient rpcClient;

	private UtsContext context;

	// JobTracker 是否可用
	private volatile boolean serverEnable = false;

	private List<Node> jobTrackers;

	public RpcClientDelegate(RpcClient rpcClient, UtsContext context) {
		this.rpcClient = rpcClient;
		this.context = context;
		this.jobTrackers = new CopyOnWriteArrayList<Node>();
	}

	private Node getJobTrackerNode() throws JobTrackerNotFoundException {
		try {

			if (jobTrackers.size() == 0) {
				LOGGER.info("rpc client delegate getJobTrackerNode no available jobTracker");
				throw new JobTrackerNotFoundException("no available jobTracker!");
			}
			// 连JobTracker的负载均衡算法
			LoadBalance loadBalance = ServiceFactory.load(LoadBalance.class, context.getConfiguration(),
					ExtConfigKeys.JOB_TRACKER_SELECT_LOADBALANCE);
			return loadBalance.select(jobTrackers, context.getConfiguration().getIdentity());
		} catch (JobTrackerNotFoundException e) {
			this.serverEnable = false;
			// publish msg
			EventInfo eventInfo = new EventInfo(EcTopic.NO_JOB_TRACKER_AVAILABLE);
			context.getEventCenter().publishAsync(eventInfo);
			throw e;
		}
	}

	public void start() {
		try {
			rpcClient.start();
		} catch (RpcException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean contains(Node jobTracker) {
		return jobTrackers.contains(jobTracker);
	}

	public void addJobTracker(Node jobTracker) {
		if (!contains(jobTracker)) {
			jobTrackers.add(jobTracker);
		}
	}

	public boolean removeJobTracker(Node jobTracker) {
		return jobTrackers.remove(jobTracker);
	}

	/**
	 * 同步调用
	 */
	public RpcCommand invokeSync(RpcCommand request) throws JobTrackerNotFoundException {
		LOGGER.info("rpc client delegate invokeSync");
		Node jobTracker = getJobTrackerNode();

		try {
			RpcCommand response = rpcClient.invokeSync(jobTracker.getAddress(), request, context.getConfiguration().getInvokeTimeoutMillis());
			this.serverEnable = true;
			return response;
		} catch (Exception e) {
			// 将这个JobTracker移除
			jobTrackers.remove(jobTracker);
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
			// 只要不是节点 不可用, 轮询所有节点请求
			return invokeSync(request);
		}
	}

	/**
	 * 异步调用
	 */
	public void invokeAsync(RpcCommand request, AsyncCallback asyncCallback) throws JobTrackerNotFoundException {

		Node jobTracker = getJobTrackerNode();
		LOGGER.info("rpc client delegate invokeAsync");
		try {
			rpcClient.invokeAsync(jobTracker.getAddress(), request, context.getConfiguration().getInvokeTimeoutMillis(), asyncCallback);
			this.serverEnable = true;
		} catch (Throwable e) {
			// 将这个JobTracker移除
			jobTrackers.remove(jobTracker);
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
			// 只要不是节点 不可用, 轮询所有节点请求
			invokeAsync(request, asyncCallback);
		}
	}

	/**
	 * 单向调用
	 */
	public void invokeOneway(RpcCommand request) throws JobTrackerNotFoundException {
		LOGGER.info("rpc client delegate invokeOneway");
		Node jobTracker = getJobTrackerNode();

		try {
			rpcClient.invokeOneway(jobTracker.getAddress(), request, context.getConfiguration().getInvokeTimeoutMillis());
			this.serverEnable = true;
		} catch (Throwable e) {
			// 将这个JobTracker移除
			jobTrackers.remove(jobTracker);
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
			// 只要不是节点 不可用, 轮询所有节点请求
			invokeOneway(request);
		}
	}

	public void registerProcessor(int requestCode, RpcProcessor processor, ExecutorService executor) {
		rpcClient.registerProcessor(requestCode, processor, executor);
	}

	public void registerDefaultProcessor(RpcProcessor processor, ExecutorService executor) {
		rpcClient.registerDefaultProcessor(processor, executor);
	}

	public boolean isServerEnable() {
		return serverEnable;
	}

	public void setServerEnable(boolean serverEnable) {
		this.serverEnable = serverEnable;
	}

	public void shutdown() {
		rpcClient.shutdown();
	}

	public RpcClient getRpcClient() {
		return rpcClient;
	}
}
