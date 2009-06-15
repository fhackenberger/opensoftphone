package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.eclipse.jface.action.Action;

public class ClearDestinationAction extends Action {
	
	/** Construct a new action
	 */
	public ClearDestinationAction() {
		super("", AS_PUSH_BUTTON); //$NON-NLS-1$
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_CLEAR_DESTINATION)));
		setToolTipText(Messages.ClearDestinationAction_1);
	}

	@Override
	public void run() {
		super.run();
		PjsuaClient.getDialDestination().clear();
	}
}
