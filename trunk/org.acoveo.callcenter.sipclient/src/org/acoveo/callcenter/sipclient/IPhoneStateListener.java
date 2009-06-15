/**
 * 
 */
package org.acoveo.callcenter.sipclient;

import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneState;
import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneStateTransition;

public interface IPhoneStateListener {
	/** Notifies the listener of a change to the state of the softphone
	 * 
	 * ATTENTION: You MUST NOT modify the phone state during the execution of this
	 * method. Use Display.getDefault().asyncExec() to call the PhoneState methods.
	 * @param oldState The old state the phone was in
	 * @param newState The new state the phone transitioned to
	 * @param transitionReason The reason for the transition
	 */
	public void stateChanged(PhoneState oldState, PhoneState newState, PhoneStateTransition transitionReason);
	
	
	public void callerInformationChanged(CallInformation callInformation);
	
	/** Notifies listeners about a change to the conferencing state
	 * @param newEnabled Whether conferencing is now enabled
	 */
	public void conferencingChanged(boolean newEnabled);
}