package com.example.threadpool;

import java.util.LinkedList;

public class PoolThread extends Thread {

	MyThreadPool mtp;
	private LinkedList<ThreadCallBack> runQueue = new LinkedList<>();

	PoolThread(MyThreadPool mtp) {
		this.mtp = mtp;
	}

	public void addThread(ThreadCallBack tcb) {
		runQueue.push(tcb);
	}

	@Override
	public void run() {
		while (true) {
			if (mtp.state == MyThreadPool.STATE_PAUSE) {
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
				}
				continue;
			} else if (mtp.state == MyThreadPool.STATE_STOP) {
				break;
			}
			if (runQueue.size() == 0) {
				// 减少cpu消耗，又不至于等待时间太长
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			} else {
				ThreadCallBack tcb = runQueue.pop();
				int runState = ThreadCallBack.RUN_STATE_OK;
				try {
					tcb.run();
				} catch (Exception e) {
					e.printStackTrace();
					runState = ThreadCallBack.RUN_STATE_WRONG;
				}
				mtp.taskFinish(tcb, runState);
			}
		}
	}
}
