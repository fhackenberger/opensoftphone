package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.acoveo.callcenter.sipclient.preferences.SoftPhonePreferencePage;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class SettingsAction extends Action implements ISelfUpdatingAction {
	public static String TEXT = Messages.SettingsAction_0;
	
	public SettingsAction() {
		super(TEXT, AS_PUSH_BUTTON);
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_SETTINGS));
		setEnabled(true);
	}
	
	@Override
	public void update() {
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		switch(state) {
		case UNREGISTERED:
			//Fall through
		case IDLE:
			setEnabled(true);
			break;
		default:
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
		PreferencesUtil.createPreferenceDialogOn(null, SoftPhonePreferencePage.ID, null, null).open();
		notifyResult(true);
	}

	@Override
	public boolean shouldBeVisible() {
		return true;
	}
}
