package com.example.threadpool;

import java.util.ArrayList;
import java.util.LinkedList;
import android.os.Handler;
import android.os.Message;

/**
 * 
 * @author Moluth
 * @version 1.0
 * @see ��Ҫ���ڼ򻯰�׿��ʹ�ö��̵߳Ĳ��裬�����߳����������������ȼ��ķ�ʽ����ÿ�����߳����������<br>
 *      ����Ķ����ǵ����ģ���ʹ��ʱ���� {@link #pause()} ����,��Ҫ�ٴ�����Ҫ����{@link #start()}������<br>
 *      ֮���ٵ��øö��󣬵��� {@link #stop()}�����ø÷����󣬸��ཫ����ʹ�á�<br>
 *      <br>
 *      ʹ�÷���:MyThreadPool.{@link #getInstance()}.{@link #addTask()};<br>
 *      �Ḵ�øö����ڵ��̣߳�������������{@link #indicator}��ָ����߳��У����߳��п��ܻ������������Ŷӵȴ���
 *      ���ܱ�֤�����������ִ�С�
 * 
 */
public class MyThreadPool {

	/** ��ʾ�ö�������ͣ״̬ */
	protected static final int STATE_PAUSE = 2;

	/** ��ʾ�ö���������ʹ��״̬ */
	protected static final int STATE_RUN = 1;

	/** ��ʾ�ö�����ͣ��״̬ */
	protected static final int STATE_STOP = 0;

	/** ͬʱ�������߳�������������Ը�����Ҫ�޸ĸ�ֵ */
	protected static final int THREAD_NUMBER = 15;

	/** ����ĵ��� */
	private static final MyThreadPool instance = new MyThreadPool();

	/** ���ڴ�����е��߳� */
	private ArrayList<PoolThread> threadPool = new ArrayList<>(THREAD_NUMBER + 2);

	/** �Ѿ�ִ����ɵ�������� */
	private LinkedList<ThreadCallBack> finishQueue = new LinkedList<>();

	/**
	 * ִ����ɵ�����������У�Ŀǰȡֵ�� : {@link ThreadCallBack#RUN_STATE_OK } ��
	 * {@link ThreadCallBack#RUN_STATE_WRONG }
	 */
	private LinkedList<Integer> runStateQueue = new LinkedList<>();
	
	/** ���������̵߳�����״̬��ȡֵ��Χ {@link #STATE_RUN} {@link #STATE_PAUSE} {@link #STATE_STOP}*/
	protected int state = 1;
	
	/** �߳�ָʾ����ָ��ĳ���̣߳�ָ����߳̽��ᱻ������񣬷������������  */
	private int indicator;
	
	/** ����ʵ�֣�ִ������������߳��е��ý�������  */
	private ThreadPoolHandler tph = new ThreadPoolHandler();
	
	/**
	 * �������ɸ��̣߳���Щ�߳�ÿ���̻߳���һ��������У�������������û�����񣬻ᴦ�ڲ�ѯ-˯��״̬��
	 * �����������ִ������
	 */
	private MyThreadPool() {
		for (int i = 0; i < THREAD_NUMBER; i++) {
			PoolThread ph = new PoolThread(this);
			ph.start();
			threadPool.add(ph);
		}
	}

	/**
	 * ��ȡMyThreadPool�����ʵ��
	 * @return {@link MyThreadPool}
	 */
	public static synchronized MyThreadPool getInstance() {
		return instance;
	}
	/**
	 * ���ø÷�������û��ִ�е������񽫲��ᱻִ�У�����ִ�е�����ִ����ɺ����������߳̽����˳������಻�ܱ�ʹ��
	 */
	public void close() {
		state = STATE_STOP;
		threadPool.clear();
		// finishQueue.clear();�������Ҫ��������Զ����
		// stateQueue.clear();
	}
	
	/**
	 * ������{@link #pause}������û��ִ�е�������ȫ����������Ҫ����start,�ſ�������ִ��
	 */
	public void start() {
		state = STATE_RUN;
	}
	
	/**
	 * ���ø÷���������û��ִ�е�����ᴦ������״̬������ִ�е�������������ִ��<br>
	 * �����Ҫ����ʹ�ã�����֮ǰ����������Ҫ����{@link #start}
	 */
	public void pause() {
		state = STATE_PAUSE;
	}
	
	/**
	 * �������������Ӻ󣬻ᰴ˳����ӵ����߳��У����ַ�ʽ�����ã���Ҫ����������<br>
	 * ��������:<br>
	 * �����̵߳��������ȥ��������ǰ�����һ���ȴ����У��ĸ��̰߳�����ִ������ˣ�����ȥ�ȴ�������ȥ������Ҫ������������һ��ͬ������<br>
	 * �������������̵߳�ִ��ʱ����������̣�ʵʱ�Ի���ߡ�
	 * @param tcb {@link ThreadCallBack }
	 */
	public void addTask(ThreadCallBack tcb) {
		if (tcb != null) {
			threadPool.get(indicator++).addThread(tcb);
			indicator %= THREAD_NUMBER;
		}
	}
	
	/**
	 * ����ִ����ɺ󣬼�����ɶ���
	 * @param tcb {@link ThreadCallBack }
	 * @param runState {@link ThreadCallBack#RUN_STATE_OK } �� {@link ThreadCallBack#RUN_STATE_WRONG }
	 */
	protected void taskFinish(ThreadCallBack tcb, int runState) {
		synchronized (finishQueue) {
			finishQueue.push(tcb);
			runStateQueue.push(runState);
		}
		tph.sendEmptyMessage(0);
	}

	/**
	 * ����ִ�к���� �����߳��е��� {@link ThreadCallBack#finishRun(int)}����
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
