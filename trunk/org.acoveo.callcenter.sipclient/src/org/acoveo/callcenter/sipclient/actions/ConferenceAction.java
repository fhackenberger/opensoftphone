package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneState;
import org.eclipse.jface.action.Action;

/** Toggles conferencing on/off
 * @author Florian Hackenberger (florian.hackenberger@acoveo.com)
 */
public class ConferenceAction extends Action implements ISelfUpdatingAction {
	public static String TOOLTIP = "Enable conferencing";
	public static String TOOLTIP_OFF = "Disable conferencing";

	public ConferenceAction() {
		super("", AS_PUSH_BUTTON); //$NON-NLS-1$
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_CONFERENCE));
		setToolTipText(TOOLTIP);
		setEnabled(false);
	}

	@Override
	public boolean isEnabled() {
		PhoneState state = PjsuaClient.getPhoneState();
		if(state == PhoneState.ACTIVE_CALL_AND_ON_HOLD) {
			return true;
		}
		return false;
	}
	

	@Override
	public boolean shouldBeVisible() {
		return true;
	}
	
	@Override
	public void update() {
		PhoneState state = PjsuaClient.getPhoneState();
		if(state == PhoneState.ACTIVE_CALL_AND_ON_HOLD) {
			if(state.isConferencingEnabled()) {
				setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_CONFERENCE_OFF));
				setToolTipText(TOOLTIP_OFF);
			}else {
				setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_CONFERENCE));
				setToolTipText(TOOLTIP);
			}
			setEnabled(true);
		}else {
			setEnabled(false);
		}
	}

	@Override
	public void run() {
		super.run();
		if(!isEnabled()) {
			notifyResult(false);
			return;
		}
		if(PjsuaClient.getPhoneState().isConferencingEnabled()) {
			PjsuaClient.getPhoneState().disableConferencing();
		}else {
			PjsuaClient.getPhoneState().enableConferencing();
		}
	}
}