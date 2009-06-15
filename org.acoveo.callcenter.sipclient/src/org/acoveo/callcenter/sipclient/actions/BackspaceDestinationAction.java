package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.eclipse.jface.action.Action;

public class BackspaceDestinationAction extends Action {
	
	/** Construct a new action
	 */
	public BackspaceDestinationAction() {
		super(Messages.BackspaceDestinationAction_0, AS_PUSH_BUTTON);
		setToolTipText(Messages.BackspaceDestinationAction_1);
	}

	@Override
	public void run() {
		super.run();
		PjsuaClient.getDialDestination().backspace();
		notifyResult(true);
	}
}
