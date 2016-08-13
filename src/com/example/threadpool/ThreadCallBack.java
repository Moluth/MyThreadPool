package com.example.threadpool;

/**
 * 回调接口，在{@link #run()}中写子线程代码，在{@link #finishRun()}中写主线程中 子线程执行后的代码。
 * @author Moluth
 *
 */
public interface ThreadCallBack {

	/** 运行成功 */
	public static final int RUN_STATE_OK = 1;

	/** 运行失败 */
	public static final int RUN_STATE_WRONG = 0;

	/**
	 * 子线程中的代码
	 * @throws Exception 执行失败
	 */
	public void run() throws Exception;
	
	/**
	 * 主线程中 子线程执行后的代码
	 * @param runState 运行状态值
	 */
	public void finishRun(int runState);
}
