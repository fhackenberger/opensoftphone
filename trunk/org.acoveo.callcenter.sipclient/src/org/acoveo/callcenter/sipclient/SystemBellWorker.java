package org.acoveo.callcenter.sipclient;

import java.awt.Toolkit;

public class SystemBellWorker implements Runnable {
	boolean beepOn = false;
	boolean useSystemBell = false;
	long intervalMilli = 2000;
	
	@Override
	public void run() {
		while(true) {
			if(beepOn) {
				if(useSystemBell) {
					System.out.print ( "\007" ); //$NON-NLS-1$
					System.out.flush();
				}else {
					try {
						Toolkit.getDefaultToolkit().beep();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(intervalMilli);
			}catch(InterruptedException e) {
				// Do nothing
			}
		}
	}

	public void beepOn() {
		beepOn = true;
	}
	
	public void beepOff() {
		beepOn = false;
	}
	
	public void setInterval(long intervalMilli) {
		this.intervalMilli = intervalMilli;
	}
	
	public void useSystemBell(boolean systemBell) {
		useSystemBell = systemBell;
	}
}
