package org.acoveo.callcenter.softphone;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.Activator;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.acoveo.callcenter.sipclient.preferences.AccountPreferencePage;
import org.acoveo.callcenter.sipclient.view.LoginDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
    public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(250, 450));
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(false);

		// ask for username and password if not set
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String sipUser = store.getString(AccountPreferencePage.PREF_SIP_USER);
		String sipPwd = store.getString(AccountPreferencePage.PREF_SIP_PASSWORD);

		if(PjsuaClient.getSipUser().isEmpty()) { // Could have been set by a command line argument
			if (sipUser.isEmpty()) {
				LoginDialog loginDialog = new LoginDialog(new Shell(PlatformUI.getWorkbench().getDisplay(), SWT.DIALOG_TRIM |SWT.SYSTEM_MODAL | SWT.ON_TOP), Messages.ApplicationWorkbenchWindowAdvisor_1, null,
						Messages.ApplicationWorkbenchWindowAdvisor_2, MessageDialog.QUESTION, new String[] { Messages.ApplicationWorkbenchWindowAdvisor_3,
								Messages.ApplicationWorkbenchWindowAdvisor_4 }, 0, sipUser);
	
				int returnCode = loginDialog.open();
				if (returnCode == InputDialog.OK) {
					sipUser = loginDialog.getUsername();
					sipPwd = loginDialog.getPassword();
				}
			}
			PjsuaClient.setSipUser(sipUser);
			PjsuaClient.setSipPwd(sipPwd);
		}
	}
}
