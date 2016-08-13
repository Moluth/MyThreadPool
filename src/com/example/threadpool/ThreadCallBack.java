package com.example.threadpool;

/**
 * �ص��ӿڣ���{@link #run()}��д���̴߳��룬��{@link #finishRun()}��д���߳��� ���߳�ִ�к�Ĵ��롣
 * @author Moluth
 *
 */
public interface ThreadCallBack {

	/** ���гɹ� */
	public static final int RUN_STATE_OK = 1;

	/** ����ʧ�� */
	public static final int RUN_STATE_WRONG = 0;

	/**
	 * ���߳��еĴ���
	 * @throws Exception ִ��ʧ��
	 */
	public void run() throws Exception;
	
	/**
	 * ���߳��� ���߳�ִ�к�Ĵ���
	 * @param runState ����״ֵ̬
	 */
	public void finishRun(int runState);
}
