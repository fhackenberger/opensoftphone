package org.acoveo.tools;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** This class implements a worker which can execute runnables in a sync and async fashion
 * 
 * The worker can be given tasks which are executed within the worker thread, either
 * synchronously or asynchronously.
 * The syncExec methods are reentrant (i.e. you can call syncExec withing a runnable run
 * with syncExec without causing any harm)
 * This class provides support for resource ordering (Dijkstra's solution), by providing
 * a method for registering lower priority threads (e.g. the display thread). Lower priority
 * threads are not allowed to call the sync methods and will get a runtime exception (or
 * assertion if enabled).
 * See http://en.wikipedia.org/wiki/Dining_philosophers_problem#Resource_hierarchy_solution
 * @author Florian Hackenberger (florian.hackenberger@acoveo.com)
 *
 */
public class RunnableLoopWorker implements Runnable {
	private Set<Thread> lowerPriorityThreads = new HashSet<Thread>();
	private boolean assertPriorityViolation = false;
	private Thread workerThread;
	private final LinkedList<RunnerStruct<?>> queue;

	private class RunnerStruct<T> {
		public RunnerStruct(Runnable runnable) {
			this.runnable = runnable;
		}
		public RunnerStruct(Runnable runnable, Lock lock, Condition syncCondition) {
			this(runnable);
			this.lock = lock;
			this.syncCondition = syncCondition;
		}
		public RunnerStruct(Callable<T> callable) {
			this.callable = callable;
		}
		public RunnerStruct(Callable<T> callable, Lock lock, Condition syncCondition) {
			this(callable);
			this.lock = lock;
			this.syncCondition = syncCondition;
		}		Runnable runnable;
		Callable<T> callable;
		Lock lock;
		Condition syncCondition;
		boolean finished = false;
		T returnValue;
		Exception exception;
		RuntimeException runtimeException;
	}
	
	public RunnableLoopWorker() {
		queue = new LinkedList<RunnerStruct<?>>();
	}
	
	/** Execute a callable from within the worker thread synchronously
	 * {@code RuntimeException}s are thrown by this method, if the callable
	 * throws one
	 * @param callable The callable to execute
	 * @throws Exception The exception possibly thrown by the callable
	 * @throws RejectedExecutionException Thrown if called by a lower priority thread
	 */
	public <T> T syncExec(Callable<T> callable) throws RejectedExecutionException, Exception {
		Thread currentThread = Thread.currentThread();
		if(currentThread == workerThread) {
			return callable.call();
		}
		if(lowerPriorityThreads.contains(currentThread)) {
			if(assertPriorityViolation) assert false : "lower priority thread called syncExec";
			throw new RejectedExecutionException();
		}
		final Lock lock = new ReentrantLock();
		Condition cond = lock.newCondition();
		RunnerStruct<T> r = new RunnerStruct<T>(callable, lock, cond);
		synchronized (queue) {
			queue.addLast(r);
			queue.notify();
		}
		lock.lock();
		if(r.finished) {
			lock.unlock();
			if(r.runtimeException != null) {
				throw r.runtimeException;
			}
			if(r.exception != null) {
				throw r.exception;
			}
			return r.returnValue;
		}
		cond.awaitUninterruptibly();
		lock.unlock();
		if(r.runtimeException != null) {
			throw r.runtimeException;
		}
		if(r.exception != null) {
			throw r.exception;
		}
		return r.returnValue;
	}
	
	/** Execute a callable from within the worker thread synchronously
	 * This method silently catches the exception which may be thrown by
	 * the callable for convenience.
	 * {@code RuntimeException}s are thrown by this method, if the callable
	 * throws one
	 * @param callable The callable to execute
	 * @throws RejectedExecutionException Thrown if called by a lower priority thread
	 */
	public <T> T syncExecNoExc(Callable<T> callable) throws RejectedExecutionException {
		try {
			return syncExec(callable);
		}catch(Exception e) {
			//Silent catch
		}
		return null;
	}

	/** Execute a runnable from within the worker thread synchronously
	 * {@code RuntimeException}s are thrown by this method, if the runnable
	 * throws one
	 * @param runnable The runnable to execute
	 * @throws RejectedExecutionException Thrown if called by a lower priority thread
	 */
	public void syncExec(Runnable runnable) throws RejectedExecutionException {
		Thread currentThread = Thread.currentThread();
		if(currentThread == workerThread) {
			runnable.run();
			return;
		}
		if(lowerPriorityThreads.contains(currentThread)) {
			if(assertPriorityViolation) assert false : "lower priority thread called syncExec";
			throw new RejectedExecutionException();
		}
		final Lock lock = new ReentrantLock();
		Condition cond = lock.newCondition();
		RunnerStruct<Object> r = new RunnerStruct<Object>(runnable, lock, cond);
		synchronized (queue) {
			queue.addLast(r);
			queue.notify();
		}
		lock.lock();
		if(r.finished) {
			lock.unlock();
			if(r.runtimeException != null) {
				throw r.runtimeException;
			}
			return;
		}
		cond.awaitUninterruptibly();
		lock.unlock();
		if(r.runtimeException != null) {
			throw r.runtimeException;
		}
	}

	/** Execute a runnable from within the worker thread asynchronously
	 * This method swallows {@code RuntimeException}s.
	 * @param runnable The runnable to execute
	 */
	public void asyncExec(Runnable runnable) {
		synchronized (queue) {
			queue.addLast(new RunnerStruct<Object>(runnable));
			queue.notify();
		}
	}
	
	/** Execute a future from within the worker thread asynchronously
	 * This method swallows {@code RuntimeException}s.
	 * @param future The future to execute
	 */
	public void asyncExec(RunnableFuture<?> future) {
		synchronized (queue) {
			queue.addLast(new RunnerStruct<Object>(future));
			queue.notify();
		}
	}
	
	/** Register a lower priority thread
	 * A lower priority thread is not allowed to call the syncExec methods
	 * of this class. It will get a {@code RejectedExecutionException} in that case.
	 * @param thread The thread to register
	 */
	public void registerLowerPriorityThread(Thread thread) {
		lowerPriorityThreads.add(thread);
	}
	
	/** De-registers a lower priority thread
	 * @param thread The thread to de-register
	 */
	public void deregisterLowerPriorityThread(Thread thread) {
		lowerPriorityThreads.remove(thread);
	}
	
	/** Enables/disables assertion of priority violations
	 * If enabled, this class will call assert if a lower priority thread
	 * calls one of the syncExec methods.
	 * @param enable Wether to enable assertion
	 */
	public void assertPriorityViolation(boolean enable) {
		assertPriorityViolation = enable;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		RunnerStruct<?> r;
		workerThread = Thread.currentThread();
		while (true) {
			synchronized (queue) {
				while (queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException ignored) {
					}
				}
				r = queue.removeFirst();
			}

			try {
				if(r.runnable != null) {
					r.runnable.run();
				}else {
					RunnerStruct<Object> castedR = (RunnerStruct<Object>)r;
					Object result = castedR.callable.call();
					castedR.returnValue = result;
				}
			} catch (RuntimeException e) {
				r.runtimeException = e;
			} catch (Exception e) {
				r.exception = e;
			}
			r.finished = true;
			if(r.syncCondition != null) {
				r.lock.lock();
				r.syncCondition.signal();
				r.lock.unlock();
			}
		}

	}
}
