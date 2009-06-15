package org.acoveo.callcenter.sipclient;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneState;
import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneStateTransition;
import org.acoveo.callcenter.sipclient.preferences.AccountPreferencePage;
import org.acoveo.tools.RandomDecisionMaker;
import org.eclipse.swt.widgets.Display;
import org.pjsip.pjsua.pjsua_call_info;

public class PhoneTester implements IPhoneStateListener {
	boolean ignoreActive = false;
	
	RandomDecisionMaker decisionMaker = new RandomDecisionMaker();
	
	Runnable answerRunnable = new Runnable() {
		@Override
		public void run() {
			PjsuaClient.getPhoneState().callAnswer();
		}
	};

	Runnable rejectRunnable = new Runnable() {
		@Override
		public void run() {
			PjsuaClient.getPhoneState().callReject();
		}
	};

	Runnable hangupRunnable = new Runnable() {
		@Override
		public void run() {
			PjsuaClient.getPhoneState().callHangUp();
		}
	};
	
	Runnable holdRunnable = new Runnable() {
		@Override
		public void run() {
			PjsuaClient.getPhoneState().callHold();
		}
	};
	
	Runnable unholdRunnable = new Runnable() {
		@Override
		public void run() {
			PjsuaClient.getPhoneState().callUnhold();
		}
	};
	
	@Override
	public void stateChanged(PhoneState oldState, PhoneState newState,
			PhoneStateTransition transitionReason) {
		int callId = -1;
		switch(newState) {
		case ACTIVE_CALL:
			if(!ignoreActive) {
				ignoreActive = false;
				callId = PjsuaClient.getPhoneState().getActiveCallId();
				switch(decisionMaker.makeDecision(new float[] {3.0f, 1.0f})) {
				case 0: // Hangup after 50 seconds
					logMessage("Going to hangup call from " + formatCallInfo(callId) + " in 50 seconds"); //$NON-NLS-1$ //$NON-NLS-2$
					Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
						@Override
						public void run() {
							logMessage("Hanging up active call"); //$NON-NLS-1$
							Display.getDefault().syncExec(hangupRunnable);
						}
					}, 50, TimeUnit.SECONDS);
					break;
				case 1: // Put the call on hold
					logMessage("Putting call from " + formatCallInfo(callId) + " on hold"); //$NON-NLS-1$ //$NON-NLS-2$
					Display.getDefault().asyncExec(holdRunnable);
					break;
				default:
					break;
				}
			}
			break;
		case ACTIVE_CALL_AND_ON_HOLD:
			break;
		case CALL_ON_HOLD:
			callId = PjsuaClient.getPhoneState().getOnHoldCallId();
			switch(decisionMaker.makeDecision(new float[] {3.0f, 1.0f})) {
			case 0: // Unhold the call
				logMessage("Retrieving call from " + formatCallInfo(callId)); //$NON-NLS-1$
				Display.getDefault().asyncExec(unholdRunnable);
				break;
			case 1: // Hangup
				logMessage("Going to hangup held call from " + formatCallInfo(callId) + " in 10 seconds"); //$NON-NLS-1$ //$NON-NLS-2$
				Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
					@Override
					public void run() {
						logMessage("Hanging up call on hold"); //$NON-NLS-1$
						Display.getDefault().syncExec(hangupRunnable);
					}
				}, 10, TimeUnit.SECONDS);
				break;
			default:
				break;
			}
			break;
		case IDLE:
			switch(decisionMaker.makeDecision(new float[] {4.0f, 1.0f})) {
			case 0: // Do nothing
				logMessage("Doing nothing"); //$NON-NLS-1$
				break;
			default:
				break;
			}
			break;
		case INCOMING_CALL:
			if(oldState == PhoneState.IDLE) { // We don't want to accept incoming calls while there is an active call
				callId = PjsuaClient.getPhoneState().getRingingCallId();
				switch(decisionMaker.makeDecision(new float[] {1.0f, 4.0f, 1.0f})) {
				case 0: // Let it ring, do nothing
					logMessage("Letting call from " + formatCallInfo(callId) + " ring"); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case 1: // Answer the call
					logMessage("Answering call from " + formatCallInfo(callId)); //$NON-NLS-1$
					Display.getDefault().asyncExec(answerRunnable);
					break;
				case 2: // Reject the call
					logMessage("Rejecting call from " + formatCallInfo(callId)); //$NON-NLS-1$
					Display.getDefault().asyncExec(rejectRunnable);
					break;
				default:
					break;
				}
			}
			break;
		case OUTGOING_CALL:
			break;
		case UNREGISTERED:
			break;
		default:
			break;
		}
	}

	protected void logMessage(String message) {
		String sipUser = Activator.getDefault().getPreferenceStore().getString(AccountPreferencePage.PREF_SIP_USER);
		Activator.getLogger().info("SOFTPHONE " + sipUser + ": " + message); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String formatCallInfo(final int callId) {
		if(callId < 0) {
			return ""; //$NON-NLS-1$
		}
		final pjsua_call_info info = new pjsua_call_info();
		PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
			@Override
			public void run() {
				PjsuaClient.call_get_info(callId, info);
			}
		});
		
		String number = info.getRemote_contact().getPtr();
		String name = info.getRemote_info().getPtr();
		return PjsuaClient.buildDisplayString(number, name);
	}

	@Override
	public void callerInformationChanged(CallInformation callInformation) {
		// Do nothing
	}

	@Override
	public void conferencingChanged(boolean newEnabled) {
		// Do nothing
	}
}
