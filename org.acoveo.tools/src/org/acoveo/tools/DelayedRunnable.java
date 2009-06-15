package org.acoveo.tools;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Delays a task for a certain amount of time after receiving a series of execution requests
 * 
 * @author Florian Hackenberger (florian.hackenberger@acoveo.com)
 */
public class DelayedRunnable<T> {
	long defaultTimeoutMs = 300;
	long timeoutMs = 300;
	Callable<T> callable;
	final Executor threadPool;
	FutureTask<Boolean> delayedExecutionTask = null;

	public DelayedRunnable(long timeoutMs, Callable<T> callable, Executor executor) {
		this.timeoutMs = timeoutMs;
		this.defaultTimeoutMs = timeoutMs;
		this.callable = callable;
		if(executor != null) {
			threadPool = executor;
		}else {
			threadPool = Executors.newFixedThreadPool(1);
		}
	}
	
	public DelayedRunnable(long timeoutMs, Callable<T> callable) {
		this(timeoutMs, callable, null);
	}

	protected class DelayedExecutionTask implements Callable<Boolean> {
		@Override
		public Boolean call() throws Exception {
			try {
				Thread.sleep(timeoutMs);
			} catch (InterruptedException e) {
				return false;
			}
			callable.call();
			return true;
		}
	};

	/** Schedules the task to run after the timeout if no further
	 * execution requests are scheduled until the timeout.
	 */
	public synchronized void requestExecution() {
		requestExecution(defaultTimeoutMs);
	}
	
	/** Schedules the task to run after the timeout if no further
	 * execution requests are scheduled until the timeout.
	 */
	public synchronized void requestExecution(long timeoutMs) {
		this.timeoutMs = timeoutMs;
		if (delayedExecutionTask == null) {
			delayedExecutionTask = new FutureTask<Boolean>(new DelayedExecutionTask());
			threadPool.execute(delayedExecutionTask);
		} else {
			delayedExecutionTask.cancel(true);
			// We have been able to cancel the running task, therefore we
			// schedule another one
			delayedExecutionTask = new FutureTask<Boolean>(new DelayedExecutionTask());
			threadPool.execute(delayedExecutionTask);
		}
	}
	
	/** Cancels the execution of a previously scheduled task
	 */
	public synchronized void cancelExecution() {
		if (delayedExecutionTask != null) {
			delayedExecutionTask.cancel(true);
		}
	}
}
