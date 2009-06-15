package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.Activator;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.acoveo.callcenter.sipclient.preferences.AccountPreferencePage;
import org.eclipse.jface.action.Action;

public class VoicemailAction extends Action  implements ISelfUpdatingAction {
	public static String TOOLTIP_VOICEMAIL = Messages.VoicemailAction_0;
	
	public VoicemailAction() {
		super("", AS_PUSH_BUTTON); //$NON-NLS-1$
		setToolTipText(TOOLTIP_VOICEMAIL);
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_VOICEMAIL)));
		setEnabled(false);
	}

	@Override
	public void run() {
		super.run();
		if(!isEnabled()) {
			notifyResult(false);
			return;
		}
		String destination = Activator.getDefault().getPreferenceStore().getString(AccountPreferencePage.PREF_VOICEMAIL_EXTENSION);
		CallAction.initiateCallToDestination(destination);
	}

	@Override
	public boolean shouldBeVisible() {
		return true;
	}

	@Override
	public void update() {
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		if(state == PjsuaClient.PhoneState.IDLE || state == PjsuaClient.PhoneState.CALL_ON_HOLD) {
			setEnabled(true);
		}else {
			setEnabled(false);
		}
	}

}
