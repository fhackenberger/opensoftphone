package org.acoveo.callcenter.sipclient;

import org.acoveo.callcenter.sipclient.PjsuaClient.CallDirection;

/**
 * This class contains all required information from the telephone call.
 */
public class CallInformation {
	private String subject;
	private String name;
	private String number;
	private String displayString;
	private String sipUrl;
	private CallDirection callDirection = CallDirection.INVALID;
	
	public CallInformation() {
	}
	
	public CallInformation(String subject, String name, String number, String displayString, String sipUrl, CallDirection callDirection) {
		this.subject = subject;
		this.name = name;
		this.number = number;
		this.displayString = displayString;
		this.sipUrl = sipUrl;
		this.callDirection = callDirection;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getDisplayString() {
		return displayString;
	}

	public void setDisplayString(String displayString) {
		this.displayString = displayString;
	}

	public String getSipUrl() {
		return sipUrl;
	}

	public void setSipUrl(String sipUrl) {
		this.sipUrl = sipUrl;
	}

	public CallDirection getCallDirection() {
		return callDirection;
	}

	public void setCallDirection(CallDirection callDirection) {
		this.callDirection = callDirection;
	}
}
