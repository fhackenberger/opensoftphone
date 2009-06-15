package org.acoveo.callcenter.softphone;

import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "org.acoveo.callcenter.softPhone.perspective"; //$NON-NLS-1$

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
        configurer.setSaveAndRestore(true);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	@Override
	public void postStartup() {
		super.postStartup();
		PjsuaClient.pjsuaWorker.asyncExec(new Runnable() {
			@Override
			public void run() {
				PjsuaClient.connectToSipServer();
			}
		});
	}
	
}
