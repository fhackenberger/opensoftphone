package org.acoveo.tools;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/** Executes a task a certain time after it was scheduled
 *
 * Executes a task a certain time after it was scheduled, ignoring scheduling
 * requests during the period between the first scheduling request and the 
 * execution time. This helper is useful e.g. for ensuring that a view is refreshed
 * as most every N ms.
 * @author Florian Hackenberger (florian.hackenberger@acoveo.com)
 *
 * @param <T>
 */
public class MinimumIntervalRunnable<T> {
	long timeoutMs = 300;
	Callable<T> callable;
	final Executor threadPool;
	FutureTask<Boolean> delayedExecutionTask = null;

	public MinimumIntervalRunnable(long timeoutMs, Callable<T> callable, Executor executor) {
		this.timeoutMs = timeoutMs;
		this.callable = callable;
		if(executor != null) {
			threadPool = executor;
		}else {
			threadPool = Executors.newFixedThreadPool(1);
		}
	}
	
	public MinimumIntervalRunnable(long timeoutMs, Callable<T> callable) {
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
			delayedExecutionTask = null;
			return true;
		}
	};

	/** Schedules the task to run after the default timeout.
	 */
	public synchronized void requestExecution() {
		if (delayedExecutionTask == null) {
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
