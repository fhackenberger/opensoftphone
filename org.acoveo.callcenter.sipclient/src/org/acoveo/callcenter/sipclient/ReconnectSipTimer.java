/**
 * 
 */
package org.acoveo.callcenter.sipclient;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneState;

class ReconnectSipTimer {
	public int TIMEOUT_SEC = 30;
	FutureTask<Boolean> reconnectSipFutureTask = null;
	Executor threadPool = Executors.newFixedThreadPool(1);
	
	ReconnectSipTimer() {
	}
	
	protected class ReconnectSipTask implements Callable<Boolean> {
		@Override
		public Boolean call() throws Exception {
			while(PjsuaClient.getPhoneState() == PhoneState.UNREGISTERED) {
				Activator.getDefault().getWorkbench().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						PjsuaClient.connectToSipServer();
					}
				});
				try {
					Thread.sleep(TIMEOUT_SEC * 1000);
				}catch(InterruptedException e) {
					// Ignore
				}
			}
			return Boolean.TRUE;
		}
	}
	public synchronized void scheduleReconnect() {
		System.out.println("scheduleReconnect() called"); //$NON-NLS-1$
		if(reconnectSipFutureTask == null || reconnectSipFutureTask.isDone()) {
			reconnectSipFutureTask = new FutureTask<Boolean>(new ReconnectSipTask());
			threadPool.execute(reconnectSipFutureTask);
		}
	}
}