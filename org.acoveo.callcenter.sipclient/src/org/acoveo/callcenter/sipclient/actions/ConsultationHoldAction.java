package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.eclipse.jface.action.Action;

public class ConsultationHoldAction extends Action  implements ISelfUpdatingAction {
	public static String HOLD_TOOLTIP = Messages.ConsultationHoldAction_0;
	public static String UNHOLD_TOOLTIP = Messages.ConsultationHoldAction_1;
	
	public ConsultationHoldAction() {
		super("", AS_PUSH_BUTTON); //$NON-NLS-1$
		setToolTipText(HOLD_TOOLTIP);
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HOLD)));
		setEnabled(false);
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
		case ACTIVE_CALL:
			state.callHold();
			notifyResult(true);
			break;
		case CALL_ON_HOLD:
			state.callUnhold();
			notifyResult(true);
			break;
		default:
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
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HOLD)));
			setToolTipText(HOLD_TOOLTIP);
			setEnabled(true);
			break;
		case CALL_ON_HOLD:
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_UNHOLD)));
			setToolTipText(UNHOLD_TOOLTIP);
			setEnabled(true);
			break;
		default:
			setEnabled(false);
			break;
		}
	}

}
