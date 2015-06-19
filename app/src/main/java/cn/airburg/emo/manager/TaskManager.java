package cn.airburg.emo.manager;

import cn.airburg.emo.utils.LogUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 任务管理器
 * Created by u17 on 2015/1/25.
 */
public class TaskManager implements Runnable {

	/**
	 * 任务执行间隔
	 */
	private static final int TIME_INTERVAL = 500 ;

	/**
	 * 任务管理器单例对象
	 */
	private static TaskManager sInstance = null ;

	/**
	 * 任务队列
	 */
	private List<Task> mTaskList ;

	/**
	 * 线程运行状态
	 */
	private volatile boolean mRunning  ;

	/**
	 * 受保护的构造方法
	 */
	protected TaskManager() {
		LogUtils.d(" -> ");
		mTaskList = new CopyOnWriteArrayList<Task>() ;

		// 开始任务
		start();
	}

	/**
	 * 获取任务管理器对象
	 * @return 任务管理器
	 */
	public static TaskManager getInstance() {
		if(null == sInstance) {
			synchronized (TaskManager.class) {
				if(null == sInstance) {
					sInstance = new TaskManager() ;
				}
			}
		}

		return sInstance ;
	}

	/**
	 * 开始任务
	 */
	public void start() {
		LogUtils.d(" -> ");
        // 清除所有任务
        mTaskList.clear();

        // 设置运行标志
		mRunning = true ;

		// 启动任务线程
		new Thread(this).start();
	}

	/**
	 * 判断线程时候再运行
	 */
	public boolean isRunning() {
		return mRunning ;
	}

	/**
	 * 停止任务
	 */
	public void stop() {
		LogUtils.d(" -> ");
		mRunning = false ;
	}

	/**
	 * 添加执行的任务
	 */
	public void addTask(Task task) {
		if(null != task) {
			mTaskList.add(task);
		}
	}

	@Override
	public void run() {
		Task task = null ;
		while(mRunning) {
			if(mTaskList.size() > 0) {
				task = mTaskList.get(0) ;
				if(null != task) {
					task.run();
				}
				// 移除执行过的任务
				mTaskList.remove(0) ;
			}

			try {
				Thread.sleep(TIME_INTERVAL);
			} catch (InterruptedException e) {
				LogUtils.e(e.getMessage());
				e.printStackTrace();
			}
		}

        // 清除所有任务
        mTaskList.clear();
	}

	/**
	 * 任务
	 */
	public interface Task extends Runnable {}
}
