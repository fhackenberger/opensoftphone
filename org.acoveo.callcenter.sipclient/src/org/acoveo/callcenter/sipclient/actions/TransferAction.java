package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.Activator;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneState;
import org.acoveo.callcenter.sipclient.view.TransferDialog;
import org.acoveo.callcenter.sipclient.view.TransferDialog.TransferType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.PlatformUI;
import org.pjsip.pjsua.pjsuaConstants;

public class TransferAction extends Action  implements ISelfUpdatingAction {
	public static String TOOLTIP = Messages.TransferAction_0;
	public static String TEXT = ""; //$NON-NLS-1$
	
	public TransferAction() {
		super(TEXT, AS_PUSH_BUTTON);
		setToolTipText(TOOLTIP);
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor((IconStore.PHONE_TRANSFER)));
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
		case ACTIVE_CALL:
			// Fall through
		case ACTIVE_CALL_AND_ON_HOLD:
			boolean blindOnly = state == PhoneState.ACTIVE_CALL;
			TransferType defaultTransferType = state == PhoneState.ACTIVE_CALL_AND_ON_HOLD ? TransferType.ATTENDED_TRANSFER : TransferType.BLIND_TRANSFER;
			TransferDialog d = new TransferDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.TransferAction_2, Messages.TransferAction_1, "", null, blindOnly, defaultTransferType); //$NON-NLS-1$
			int returnCode = d.open();
			if (returnCode == InputDialog.OK) {
				final int callId = state.getActiveCallId();
				if(callId >=0) {
					String number = d.getValue();
					TransferType transerType = d.getSelectedTransferType();
					if (transerType == TransferType.ATTENDED_TRANSFER) {
						// The call of the call which is to be transferred
						final int transferCallId = state.getOnHoldCallId();
						PjsuaClient.pjsuaWorker.asyncExec(new Runnable() {
							@Override
							public void run() {
								int status = PjsuaClient.call_xfer_replaces(transferCallId, callId, 0, null);
								if(status != pjsuaConstants.PJ_SUCCESS) {
									Activator.getLogger().error("Could initiate an attended transfer for call id " + transferCallId + " to replace " + callId + ". Code: " + status); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								}
							}
						});
					} else {
						forwardCall(number);
					}
					notifyResult(true);
				}
				break;
			}
			notifyResult(false);
			break;
		default:
			notifyResult(false);
		}
	}

	@Override
	public boolean shouldBeVisible() {
		return true;
	}

	@Override
	public void update() {
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		switch(state) {
		case ACTIVE_CALL:
			// Fall through
		case ACTIVE_CALL_AND_ON_HOLD:
			setEnabled(true);
			break;
		default:
			setEnabled(false);
			break;
		}
	}
	
	public static void forwardCall(final String number) {
		PjsuaClient.PhoneState state = PjsuaClient.getPhoneState();
		final int callId = state.getActiveCallId();
		if(callId >=0) {
			PjsuaClient.pjsuaWorker.asyncExec(new Runnable() {
				@Override
				public void run() {
					int status = PjsuaClient.call_xfer(callId, PjsuaClient.pj_str_copy(PjsuaClient.createSipUrlFromNumber(number)), null);
					if(status != pjsuaConstants.PJ_SUCCESS) {
						Activator.getLogger().error("Could not transfer call id " + callId + " to " + number + ". Code: " + status); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			});
		}
	}

}
