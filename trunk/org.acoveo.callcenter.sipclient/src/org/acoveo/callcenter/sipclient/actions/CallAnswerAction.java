package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.eclipse.jface.action.Action;

public class CallAnswerAction extends Action implements ISelfUpdatingAction {
	
	
	public CallAnswerAction() {
//		super(CallAction.TEXT, AS_PUSH_BUTTON);
		super("", AS_PUSH_BUTTON); //$NON-NLS-1$
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_CALL));
		setToolTipText(CallAction.TOOLTIP);
		setEnabled(true);
	}
	
	@Override
	public boolean shouldBeVisible() {
		return true;
	}

	@Override
	public void update() {
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		switch(state) {
		case INCOMING_CALL:
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_RINGING));
			setToolTipText(AnswerAction.TOOLTIP);
			setEnabled(true);
			break;
		case IDLE:
			// Fall through
		case CALL_ON_HOLD:
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_CALL));
			setToolTipText(CallAction.TOOLTIP);
			setEnabled(true);
			break;
		default:
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_CALL));
			setToolTipText(CallAction.TOOLTIP);
			setEnabled(false);
			break;
		}
	}

	@Override
	public void run() {
		super.run();
		
		if(!isEnabled()) {
			notifyResult(false);
			return;
		}
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		switch(state) {
		case INCOMING_CALL:
			state.callAnswer();
			notifyResult(true);
			break;
		case IDLE:
			// Fall through
		case CALL_ON_HOLD:
			CallAction.initiateCall();
			notifyResult(true);
			break;
		default:
			notifyResult(false);
			break;
		}
	}
}
