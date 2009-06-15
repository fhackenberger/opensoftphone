package org.acoveo.callcenter.sipclient.view;

import org.acoveo.callcenter.nls.Messages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginDialog extends MessageDialog {

	public LoginDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
			int dialogImageType, String[] dialogButtonLabels, int defaultIndex, String initUsername) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
		usernameString = initUsername;
	}

	protected Text username = null;
	protected String usernameString;
	
	protected Text password = null;
	protected String passwordString = ""; //$NON-NLS-1$
	
	

	@Override
	protected Control createDialogArea(Composite parent) {
        // create composite
        
        Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		//GridDataFactory.fillDefaults().span(2, 1).applyTo(composite);
		composite.setLayout(new GridLayout(2, false));
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.LoginDialog_1);
		username = new Text(composite, SWT.BORDER);
		if(usernameString != null) {
			username.setText(usernameString);
		}
        username.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				usernameString = username.getText();
			}
        	
        });
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(username);
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.LoginDialog_2);
		password = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(password);
		password.setEchoChar('\u25CF');
        password.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				passwordString = password.getText();
			}
        	
        });
        
        return composite;
	}
	
	public String getPassword() {
		return passwordString;
	}
	
	public String getUsername() {
		return usernameString;
	}
}