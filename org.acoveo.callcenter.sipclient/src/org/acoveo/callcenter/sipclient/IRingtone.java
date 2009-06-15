package org.acoveo.callcenter.sipclient;

public interface IRingtone {

	public abstract void createTone() throws Exception;

	public abstract void destroyTone();

	public abstract void startRinging();

	public abstract void stopRinging();

	public abstract boolean isRinging();

}