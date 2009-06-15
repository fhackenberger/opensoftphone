package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.view.SoftPhoneViewPart;
import org.acoveo.callcenter.iconstore.IconStore;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

public class ShowHideTimeAction extends Action implements ISelfUpdatingAction {
	public static String TOOLTIP_SHOW_TIME = Messages.ShowHideTimeAction_0;
	public static String TOOLTIP_HIDE_TIME = Messages.ShowHideTimeAction_1;
	
	public ShowHideTimeAction() {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HISTORY_SHOW_TIME)));
		setToolTipText(TOOLTIP_SHOW_TIME);
	}

	@Override
	public void run() {
		super.run();
		SoftPhoneViewPart view = (SoftPhoneViewPart) PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage()
				.findView(SoftPhoneViewPart.ID);
		view.toggleTime();
		update();
	}

	@Override
	public boolean shouldBeVisible() {
		return true;
	}

	@Override
	public void update() {
		if(!isChecked()) {
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HISTORY_SHOW_TIME)));
			setToolTipText(TOOLTIP_SHOW_TIME);
		} else {
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HISTORY_HIDE_TIME)));
			setToolTipText(TOOLTIP_HIDE_TIME);		
		}
	}
}
