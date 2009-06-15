package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.view.SoftPhoneViewPart;
import org.acoveo.callcenter.iconstore.IconStore;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

public class FilterInternalNrsAction extends Action implements ISelfUpdatingAction {
	public static String TOOLTIP_FILTER_ON = Messages.FilterInternalNrsAction_0;
	public static String TOOLTIP_FILTER_OFF = Messages.FilterInternalNrsAction_1;
	
	public FilterInternalNrsAction() {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HISTORY_FILTER_OFF)));
		setToolTipText(TOOLTIP_FILTER_OFF);
		setChecked(true);
	}

	@Override
	public void run() {
		super.run();
		SoftPhoneViewPart view = (SoftPhoneViewPart) PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage()
				.findView(SoftPhoneViewPart.ID);
		view.toggleFilter();
		update();
	}

	@Override
	public boolean shouldBeVisible() {
		return true;
	}

	@Override
	public void update() {
		if(!isChecked()) {
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HISTORY_FILTER_ON)));
			setToolTipText(TOOLTIP_FILTER_ON);
		} else {
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_HISTORY_FILTER_OFF)));
			setToolTipText(TOOLTIP_FILTER_OFF);		
		}
	}
}
