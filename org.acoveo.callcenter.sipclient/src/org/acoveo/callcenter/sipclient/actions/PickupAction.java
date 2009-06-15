package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.PlatformUI;

public class PickupAction extends Action implements ISelfUpdatingAction {
	public static String TOOLTIP_VOICEMAIL = Messages.PickupAction_0;
	public static String PICKUP_PREFIX = "*8"; //$NON-NLS-1$

	public PickupAction() {
		super("", AS_PUSH_BUTTON); //$NON-NLS-1$
		setToolTipText(TOOLTIP_VOICEMAIL);
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_PICKUP)));
	}

	@Override
	public void run() {
		super.run();

		if (!isEnabled()) {
			notifyResult(false);
			return;
		}

		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		switch (state) {
		case IDLE:
			// Fall through
		case CALL_ON_HOLD:
			// Fall through
			InputDialog dialogue = new InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					Messages.PickupAction_3, Messages.PickupAction_4, "", null); //$NON-NLS-1$
			dialogue.open();
			String number = dialogue.getValue();

			CallAction.initiateCallToDestination(PICKUP_PREFIX + number);
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
		switch (state) {
		case IDLE:
			// Fall through
		case CALL_ON_HOLD:
			// Fall through
			setEnabled(true);
			break;
		default:
			setEnabled(false);
			break;
		}
	}

}
