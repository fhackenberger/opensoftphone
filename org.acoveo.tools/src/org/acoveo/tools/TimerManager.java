package org.acoveo.tools;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class manages runnables that should be run at certain times. 
 * 
 * @author tkenner
 *
 */
public class TimerManager {
	Map<Date, Runnable> timerRunnableMap = new TreeMap<Date, Runnable>();
	
	Thread thread;
	
	public TimerManager() {
		thread = new Thread(){
			@Override
			public void run() {
				while(true) {
					try {
						checkForMatureRunnables();
						if(timerRunnableMap.size() > 0) {
							long sleepTime = ((Date)timerRunnableMap.keySet().toArray()[0]).getTime()-System.currentTimeMillis();
							if(sleepTime > 0) {
								Thread.sleep(sleepTime);
							}
						} else {
							Thread.sleep(Long.MAX_VALUE);
						}
					} catch (InterruptedException e) {
						// do nothing than wake up
					}
				}
			}
		};
		thread.start();
	}
	
	/**
	 * Add a runnable that should be run at given date. 
	 * 
	 * @param date
	 * @param runnable
	 */
	public void addRunnable(Date date, Runnable runnable) {
		timerRunnableMap.put(date, runnable);
		thread.interrupt();
	}
	
	private void checkForMatureRunnables() {
		if(timerRunnableMap.size() < 1) {
			return;
		}
		
		Date date = (Date)timerRunnableMap.keySet().toArray()[0];
		if(date.before(new Date(System.currentTimeMillis()))) {
			timerRunnableMap.get(date).run();
			timerRunnableMap.remove(date);
			checkForMatureRunnables();
		}
	}
}
