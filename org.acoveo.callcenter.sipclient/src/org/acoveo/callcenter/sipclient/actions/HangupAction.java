package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.eclipse.jface.action.Action;

public class HangupAction extends Action  implements ISelfUpdatingAction {
	public static String TOOLTIP = Messages.HangupAction_0;
	public static String TEXT = ""; //$NON-NLS-1$
	public HangupAction() {
		super(TEXT, AS_PUSH_BUTTON);
		setToolTipText(TOOLTIP);
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HANGUP)));
		setEnabled(false);
	}

	@Override
	public void run() {
		super.run();
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		switch(state) {
		case ACTIVE_CALL:
			// Fall through
		case ACTIVE_CALL_AND_ON_HOLD:
			state.callHangUp();
			notifyResult(true);
			break;
		case OUTGOING_CALL:
			state.dialCancel();
			notifyResult(true);
			break;
		case INCOMING_CALL:
			state.callReject();
			notifyResult(true);
			break;
		default:
			notifyResult(false);
			break;
		}
	}

	@Override
	public boolean shouldBeVisible() {
		return true;
	}

	@Override
	public void update() {
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		switch(state) {
		case ACTIVE_CALL:
			// Fall through
		case ACTIVE_CALL_AND_ON_HOLD:
			// Fall through
		case OUTGOING_CALL:
			// Fall through
		case INCOMING_CALL:
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_HANGUP));
			setToolTipText(TOOLTIP);
			setEnabled(true);
			break;
		default:
			setEnabled(false);
			break;
		}
	}

}
