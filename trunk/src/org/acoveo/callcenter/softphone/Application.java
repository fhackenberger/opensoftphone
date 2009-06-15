package org.acoveo.callcenter.softphone;

import java.io.IOException;
import java.net.URL;

import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		// Working around a problematic org.apache.batik.pdf plugin which exports org.apache.commons.logging
		// See https://www.acoveo.com/mediawiki/index.php/Logging_Callcenter
		System.getProperties().put("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

		// reconfigure log4j for this application
		URL log4jFile = null;
		try {
			log4jFile = FileLocator.toFileURL(FileLocator.find(Activator.getDefault().getBundle(), new Path("log4j.properties"), null)); //$NON-NLS-1$
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(log4jFile != null) {
			PropertyConfigurator.configure(log4jFile);
		}
		
		// Check for command line arguments
		String[] applicationArguments = {};
		if(context.getArguments().containsKey(IApplicationContext.APPLICATION_ARGS)) {
			applicationArguments = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		}
		for(int index = 0; index < applicationArguments.length; index++) {
			String currArg = applicationArguments[index];
			if(currArg.equals("--sipUser") && index + 1 < applicationArguments.length) { //$NON-NLS-1$
				String sipUser = applicationArguments[++index];
				PjsuaClient.setSipUser(sipUser);
				continue;
			}
			if(currArg.equals("--sipPwd")) { //$NON-NLS-1$
				String sipPwd = applicationArguments[++index];
				PjsuaClient.setSipPwd(sipPwd);
				continue;
			}
		}
		Activator.getLogger().info("Softphone started..."); //$NON-NLS-1$
		Display display = PlatformUI.createDisplay();
		int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
		if (returnCode == PlatformUI.RETURN_RESTART)
			return IApplication.EXIT_RESTART;
		else
			return IApplication.EXIT_OK;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		Activator.getLogger().info("Softphone stopped..."); //$NON-NLS-1$
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
