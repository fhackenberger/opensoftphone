package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.view.SoftPhoneViewPart;
import org.acoveo.callcenter.iconstore.IconStore;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

public class ClearHistoryAction extends Action {
	public static String TOOLTIP_CLEAR_HISTORY = Messages.ClearHistoryAction_0;
	
	public ClearHistoryAction() {
		super("", AS_PUSH_BUTTON); //$NON-NLS-1$
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HISTORY_CLEAR)));
		setToolTipText(TOOLTIP_CLEAR_HISTORY);
	}

	@Override
	public void run() {
		super.run();
		SoftPhoneViewPart view = (SoftPhoneViewPart) PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage()
				.findView(SoftPhoneViewPart.ID);
		view.clearHistory();
	}

}
