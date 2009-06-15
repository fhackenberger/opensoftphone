package org.acoveo.callcenter.sipclient;

public class PjsipException extends Exception {
	private static final long serialVersionUID = 7500080833442901313L;

	public PjsipException() {
	}

	public PjsipException(String message) {
		super(message);
	}

	public PjsipException(Throwable cause) {
		super(cause);
	}

	public PjsipException(String message, Throwable cause) {
		super(message, cause);
	}

}
