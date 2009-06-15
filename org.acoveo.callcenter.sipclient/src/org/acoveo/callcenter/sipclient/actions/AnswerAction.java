package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.eclipse.jface.action.Action;

public class AnswerAction extends Action {
	public static String TOOLTIP = Messages.AnswerAction_0;
	public static String TEXT = Messages.AnswerAction_1;

	public AnswerAction() {
		super(TEXT, AS_PUSH_BUTTON);
		setToolTipText(TOOLTIP);
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
		case INCOMING_CALL:
			state.callAnswer();
			notifyResult(true);
			break;
		default:
			notifyResult(false);
			break;
		}
	}

}

