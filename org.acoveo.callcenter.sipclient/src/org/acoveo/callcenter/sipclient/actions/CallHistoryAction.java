package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.view.SoftPhoneViewPart;
import org.acoveo.callcenter.iconstore.IconStore;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

public class CallHistoryAction extends Action implements ISelfUpdatingAction {
	public static String TOOLTIP_SHOW_HISTORY = Messages.CallHistoryAction_0;
	public static String TOOLTIP_HIDE_HISTORY = Messages.CallHistoryAction_1;
	
	private boolean hideHistory = true;
	
	public CallHistoryAction() {
		super("", AS_PUSH_BUTTON); //$NON-NLS-1$
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_CALL_HISTORY)));
		setToolTipText(TOOLTIP_SHOW_HISTORY);
	}

	@Override
	public void run() {
		super.run();
		SoftPhoneViewPart view = (SoftPhoneViewPart) PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage()
				.findView(SoftPhoneViewPart.ID);
		view.toggleCallHistory();
		hideHistory = !hideHistory;
		update();
	}

	@Override
	public boolean shouldBeVisible() {
		return true;
	}

	@Override
	public void update() {
		if(hideHistory) {
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_CALL_HISTORY)));
			setToolTipText(TOOLTIP_SHOW_HISTORY);
		} else {
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HIDE_CALL_HISTORY)));
			setToolTipText(TOOLTIP_HIDE_HISTORY);		
		}
	}
}
