# MyThreadPool
一个简单的java线程管理类
***
注意：只能在安卓中使用，不能保证添加的任务立即被执行,最好不要执行耗时太长的任务，比如Thread.sleep(100000);这会使处于同一队列的其他任务处于等待状态。
***
只要导入MyThreadPool.jar即可开始使用，MyThreadPool.getInstance().addTask(ThreadCallBack tcb);
ThreadCallBack 中定义了两个方法：
* public void run() throws Exception;
*	public void finishRun(int runState);
***
定义了两个常量：
* public static final int RUN_STATE_OK = 1;//运行成功
* public static final int RUN_STATE_WRONG = 0;//运行错误

***

其中run()中是子线程，finishRun()中是主线程.finishRun()有一个参数，是运行结果的状态，这个状态只能靠运行是否异常来判断
