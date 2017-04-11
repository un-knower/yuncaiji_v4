package cn.uway.framework.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 任务队列
 * <p>
 * 默认队列大小{@link #DEFAULT_TASK_QUEUE_SIZE}.<br>
 * <b>注意：在此队列中的任务为等待被触发的任务，而不是正在运行的任务.</b>
 * </p>
 * 
 * @author MikeYang
 * @Date 2012-10-29
 * @version 1.0
 * @since 3.0
 * @see ArrayBlockingQueue
 * @see Task
 */
public class TaskQueue {

	/** 任务队列默认大小,为200 */
	private static final int DEFAULT_TASK_QUEUE_SIZE = 200;

	private BlockingQueue<Task> currTaskQueue; // 当前任务队列

	private int taskQueueSize = DEFAULT_TASK_QUEUE_SIZE; // 任务队列大小,初始化为200

	/** 任务map **/
	public Map<Long, Task> taskMap;

	/**
	 * 默认构造方法
	 * <p>
	 * 以默认任务队列大小{@link #DEFAULT_TASK_QUEUE_SIZE}构造队列
	 * </p>
	 */
	public TaskQueue() {
		super();
		currTaskQueue = new ArrayBlockingQueue<Task>(taskQueueSize);
	}

	/**
	 * 指定队列大小方式构造队列
	 * 
	 * @param size
	 *            队列大小，如果为非正整数，则使用默认大小{@link #DEFAULT_TASK_QUEUE_SIZE}
	 */
	public TaskQueue(int size) {
		super();
		if (size > 0)
			this.taskQueueSize = size;
		currTaskQueue = new ArrayBlockingQueue<Task>(taskQueueSize);
	}

	/**
	 * 添加任务到任务队列
	 * <p>
	 * 如果队列满了，则会被阻塞直到队列有空间时再添加.
	 * </p>
	 * 
	 * @param task
	 *            任务{@link Task}
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public void put(Task task) throws InterruptedException {
		// 添加任务到队列
		if (!currTaskQueue.contains(task))
			currTaskQueue.put(task);

		// 添加任务到map
		if (taskMap == null) {
			taskMap = new HashMap<Long, Task>();
		}
		taskMap.put(task.getId(), task);
	}

	/**
	 * 获取队列的第一个任务的索引
	 * 
	 * @return first Task
	 */
	public Task peek() {
		return currTaskQueue.peek();
	}

	/**
	 * 剔除队列的第一个元素
	 */
	public void poll() {
		currTaskQueue.poll();
	}

	/**
	 * 从任务队列中取出一条任务
	 * <p>
	 * 如果任务队列中没有任务，则阻塞等待队列中有任务为止.
	 * </p>
	 * 
	 * @return 队列头的任务{@link Task}
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public Task take() throws InterruptedException {
		return currTaskQueue.take();
	}

	/**
	 * 获取当前所有的任务
	 * 
	 * @return 当前{@link Task}列表
	 */
	public synchronized List<Task> getCurrentTaskList() {
		List<Task> tasks = new ArrayList<Task>();
		Iterator<Task> itr = currTaskQueue.iterator();
		while (itr.hasNext()) {
			tasks.add(itr.next());
		}
		return tasks;
	}
	
	public int getQueueTaskSize() {
		return currTaskQueue.size();
	}

	/**
	 * 清空任务队列
	 */
	public synchronized void clear() {
		currTaskQueue.clear();
	}

	/**
	 * 获取任务队列的容量
	 */
	public int getCapacity() {
		return this.taskQueueSize;
	}
}
