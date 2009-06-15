package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.eclipse.jface.action.Action;

public class DialAction extends Action implements ISelfUpdatingAction {
	String dialString;
	
	/** Construct a new action based on the given dial string
	 * 
	 * @param dialString Must not be null
	 */
	public DialAction(String dialString) {
		super("&" + dialString, AS_PUSH_BUTTON); //$NON-NLS-1$
		this.dialString = dialString;
		setToolTipText(Messages.DialAction_1 + dialString);
	}
	
	@Override
	public boolean shouldBeVisible() {
		return true;
	}

	@Override
	public void update() {
	}

	@Override
	public void run() {
		super.run();
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		switch(state) {
		case ACTIVE_CALL:
			// Fall through
		case ACTIVE_CALL_AND_ON_HOLD:
			final int callId = state.getActiveCallId();
			if(callId >= 0) {
				PjsuaClient.pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						PjsuaClient.call_dial_dtmf(callId, PjsuaClient.pj_str_copy(dialString));
					}
				});
				notifyResult(true);
			}
			notifyResult(true);
			break;
		default:
			PjsuaClient.getDialDestination().append(dialString);
			notifyResult(true);
		}
//XXX To port	
//		if(!Activator.getDefault().isCallOnHold()) {
//			Sound sound = new Sound();
//			sound.init(DTMFTones.DTMF_TONES.get(dialString), DTMFTones.DTMF_AMPLITUDE);
//			sound.setRepeat(0);
//			PjsuaClient.getInstance().playSound(sound, true);
//		}
	}
}
