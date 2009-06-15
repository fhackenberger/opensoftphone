package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneState;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.PlatformUI;

/** The shortcut action is used for buttons which can forward and/or dial defined numbers
 */
public class ShortcutButtonAction extends Action implements ISelfUpdatingAction {
	private static String TPL_TOOLTIP_CALL = Messages.ShortcutButtonAction_0;
	private static String TPL_TOOLTIP_CALL_SUFFIX = Messages.ShortcutButtonAction_1;
	private static String TPL_TOOLTIP_FORWARD = Messages.ShortcutButtonAction_2;
	private static String TPL_TOOLTIP_FORWARD_SUFFIX = Messages.ShortcutButtonAction_3;
	
	private String number;
	private String caption;
	private boolean forward;
	private boolean suffixDialing;
	
	public ShortcutButtonAction(String number, String caption, boolean forward, boolean suffixDialing) {
		super("", AS_PUSH_BUTTON); //$NON-NLS-1$
		
		this.number = number;
		this.caption = caption;
		this.forward = forward;
		this.suffixDialing = suffixDialing;
		
		setText(this.caption);
		
		update();		
	}

	@Override
	public void run() {
		super.run();
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		
		// Suffix dialing
		String suffix = ""; //$NON-NLS-1$
		if(suffixDialing) {
			InputDialog suffixDialog = new InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.ShortcutButtonAction_6, Messages.ShortcutButtonAction_7, "", null); //$NON-NLS-1$
			int returnCode = suffixDialog.open();
			if (returnCode == InputDialog.OK) {
				suffix = suffixDialog.getValue().trim();
			}
		}
		String numberToDial = number+suffix;
		
		if(forward && (state == PhoneState.ACTIVE_CALL || state == PhoneState.ACTIVE_CALL_AND_ON_HOLD)) {
			TransferAction.forwardCall(numberToDial);
		}else {
			CallAction.initiateCallToDestination(numberToDial);
		}
		update();
	}

	@Override
	public boolean shouldBeVisible() {
		return true;
	}

	@Override
	public void update() {
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		
		if(forward && (state == PhoneState.ACTIVE_CALL || state == PhoneState.ACTIVE_CALL_AND_ON_HOLD)) {
			// icon for forward
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_TRANSFER)));
			
			if(suffixDialing) {
				setToolTipText(String.format(TPL_TOOLTIP_FORWARD_SUFFIX, number));
			} else {
				setToolTipText(String.format(TPL_TOOLTIP_FORWARD, number));
			}
			
		} else {
			// icon for call
			setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_CALL)));
			
			if(suffixDialing) {
				setToolTipText(String.format(TPL_TOOLTIP_CALL_SUFFIX, number));
			}
			else {
				setToolTipText(String.format(TPL_TOOLTIP_CALL, number));
			}
		}
		setText(caption);
	}

	public String getCaption() {
		return caption;
	}
}

