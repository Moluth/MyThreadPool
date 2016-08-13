package com.example.threadpool;

import java.util.ArrayList;
import java.util.LinkedList;
import android.os.Handler;
import android.os.Message;

/**
 * 
 * @author Moluth
 * @version 1.0
 * @see 主要用于简化安卓中使用多线程的步骤，控制线程数量。采用无优先级的方式，向每个子线程中添加任务。<br>
 *      该类的对象是单例的，不使用时调用 {@link #pause()} 方法,想要再次启用要调用{@link #start()}方法。<br>
 *      之后不再调用该对象，调用 {@link #stop()}，调用该方法后，该类将不能使用。<br>
 *      <br>
 *      使用方法:MyThreadPool.{@link #getInstance()}.{@link #addTask()};<br>
 *      会复用该对象内的线程，将你的任务加入{@link #indicator}所指向的线程中，该线程中可能会有若干任务排队等待，
 *      不能保证你的任务被立即执行。
 * 
 */
public class MyThreadPool {

	/** 表示该对象处于暂停状态 */
	protected static final int STATE_PAUSE = 2;

	/** 表示该对象处于正在使用状态 */
	protected static final int STATE_RUN = 1;

	/** 表示该对象处于停用状态 */
	protected static final int STATE_STOP = 0;

	/** 同时开启的线程最大数量，可以根据需要修改该值 */
	protected static final int THREAD_NUMBER = 15;

	/** 该类的单例 */
	private static final MyThreadPool instance = new MyThreadPool();

	/** 用于存放所有的线程 */
	private ArrayList<PoolThread> threadPool = new ArrayList<>(THREAD_NUMBER + 2);

	/** 已经执行完成的任务队列 */
	private LinkedList<ThreadCallBack> finishQueue = new LinkedList<>();

	/**
	 * 执行完成的任务结果码队列，目前取值有 : {@link ThreadCallBack#RUN_STATE_OK } 和
	 * {@link ThreadCallBack#RUN_STATE_WRONG }
	 */
	private LinkedList<Integer> runStateQueue = new LinkedList<>();
	
	/** 控制所有线程的运行状态，取值范围 {@link #STATE_RUN} {@link #STATE_PAUSE} {@link #STATE_STOP}*/
	protected int state = 1;
	
	/** 线程指示器，指向某个线程，指向的线程将会被添加任务，放入其任务队列  */
	private int indicator;
	
	/** 用于实现，执行任务后，在主线程中调用结束方法  */
	private ThreadPoolHandler tph = new ThreadPoolHandler();
	
	/**
	 * 创建若干个线程，这些线程每个线程会有一个任务队列，如果任务队列中没有任务，会处于查询-睡眠状态。
	 * 如果有任务，择执行任务。
	 */
	private MyThreadPool() {
		for (int i = 0; i < THREAD_NUMBER; i++) {
			PoolThread ph = new PoolThread(this);
			ph.start();
			threadPool.add(ph);
		}
	}

	/**
	 * 获取MyThreadPool对象的实例
	 * @return {@link MyThreadPool}
	 */
	public static synchronized MyThreadPool getInstance() {
		return instance;
	}
	/**
	 * 调用该方法，还没被执行到的任务将不会被执行，正在执行的任务执行完成后，所处的子线程将会退出，该类不能被使用
	 */
	public void close() {
		state = STATE_STOP;
		threadPool.clear();
		// finishQueue.clear();这两项不需要清除，会自动清空
		// stateQueue.clear();
	}
	
	/**
	 * 当调用{@link #pause}方法后，没被执行到的任务将全部阻塞，需要调用start,才可以重新执行
	 */
	public void start() {
		state = STATE_RUN;
	}
	
	/**
	 * 调用该方法，所有没有执行的任务会处于阻塞状态，正在执行的任务会正常完成执行<br>
	 * 如果需要继续使用，开启之前阻塞的任务，要调用{@link #start}
	 */
	public void pause() {
		state = STATE_PAUSE;
	}
	
	/**
	 * 添加任务，任务添加后，会按顺序添加到子线程中，这种方式并不好，需要有人来改良<br>
	 * 改良方法:<br>
	 * 将子线程的任务队列去掉，给当前类添加一个等待队列，哪个线程把任务执行完毕了，立即去等待队列中去新任务，要给任务队列添加一个同步锁。<br>
	 * 改良后，所有子线程的执行时间会明显缩短，实时性会提高。
	 * @param tcb {@link ThreadCallBack }
	 */
	public void addTask(ThreadCallBack tcb) {
		if (tcb != null) {
			threadPool.get(indicator++).addThread(tcb);
			indicator %= THREAD_NUMBER;
		}
	}
	
	/**
	 * 任务执行完成后，加入完成队列
	 * @param tcb {@link ThreadCallBack }
	 * @param runState {@link ThreadCallBack#RUN_STATE_OK } 和 {@link ThreadCallBack#RUN_STATE_WRONG }
	 */
	protected void taskFinish(ThreadCallBack tcb, int runState) {
		synchronized (finishQueue) {
			finishQueue.push(tcb);
			runStateQueue.push(runState);
		}
		tph.sendEmptyMessage(0);
	}

	/**
	 * 用于执行后调用 在主线程中调用 {@link ThreadCallBack#finishRun(int)}方法
	 */
	private void runFinsh() {
		synchronized (finishQueue) {
			while (finishQueue.size() > 0){
				try{ finishQueue.pop().finishRun(runStateQueue.pop());}catch(Exception e){};
			}
		}
	}
	
	static class ThreadPoolHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			MyThreadPool.getInstance().runFinsh();
			super.handleMessage(msg);
		}
	}
}
