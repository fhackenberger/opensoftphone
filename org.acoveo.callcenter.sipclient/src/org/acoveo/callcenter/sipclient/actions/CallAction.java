package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.eclipse.jface.action.Action;

public class CallAction extends Action {
	public static String TOOLTIP = Messages.CallAction_0;
	public static String TEXT = Messages.CallAction_1;
	
	public CallAction() {
		super(TEXT, AS_PUSH_BUTTON);
		setToolTipText(TOOLTIP);
		setEnabled(true);
	}

	@Override
	public void run() {
		super.run();
		
		if(!isEnabled()) {
			notifyResult(false);
			return;
		}
		initiateCall();
		notifyResult(true);
	}
	
	public static void initiateCall() {
		String destination = PjsuaClient.getDialDestination().getDialDestination();
		PjsuaClient.getDialDestination().clear();
		initiateCallToDestination(destination);
	}
	
	public static void initiateCallToDestination(String destination) {
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		switch(state) {
		case IDLE:
			// Fall through
		case CALL_ON_HOLD:
			if(destination != null && !destination.isEmpty()) {
				PjsuaClient.getPhoneState().dialNumber(PjsuaClient.createSipUrlFromNumber(destination));
			}
			break;
		default:
			break;
		}
	}

}
