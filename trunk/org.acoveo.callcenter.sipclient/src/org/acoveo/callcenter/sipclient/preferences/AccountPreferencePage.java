package org.acoveo.callcenter.sipclient.preferences;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.Activator;
import org.acoveo.tools.NetworkTools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class AccountPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "org.acoveo.callcenter.sipclient.preferences.AccountPreferencePage"; //$NON-NLS-1$

	public static final String PREF_SIP_NETWORK_ADDRESS = "SIP.networkAddress"; //$NON-NLS-1$
	public static final String PREF_SIP_AUTH_REALM = "SIP.authRealm"; //$NON-NLS-1$
	public static final String PREF_SIP_USER = "SIP.user"; //$NON-NLS-1$
	public static final String PREF_SIP_HOST = "SIP.host"; //$NON-NLS-1$
	public static final String PREF_SIP_PASSWORD = "SIP.password"; //$NON-NLS-1$
	public static final String PREF_SIP_SERVER_PORT = "SIP.serverPort"; //$NON-NLS-1$
	public static final String PREF_ECF_PHONE_SERVER_PORT = "ECFPhone.serverPort";
	public static final String PREF_VOICEMAIL_EXTENSION = "Callcenter.voicemailExtension"; //$NON-NLS-1$
	public static final String SIP_NETWORK_ADDRESS_LABEL = Messages.AccountPreferencePage_0;
	public static final String SIP_AUTH_REALM = Messages.AccountPreferencePage_8;
	public static final String SIP_USER_LABEL = Messages.AccountPreferencePage_9;
	public static final String SIP_HOST_LABEL = Messages.AccountPreferencePage_10;
	public static final String SIP_PASSWORD_LABEL = Messages.AccountPreferencePage_11;
	public static final String SIP_SERVER_PORT_LABEL = Messages.AccountPreferencePage_12;
	public static final String ECF_PHONE_SERVER_PORT_LABEL = "ECF server port label";
	public static final String VOICEMAIL_LABEL = Messages.AccountPreferencePage_26;
	
	public AccountPreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		// Collect all network addresses for the combo box
		String[][] networkAddressChoice = null;
		Collection<InetAddress> addresses = null;
		try {
			addresses = NetworkTools.getAllNonLocalNetworkAddresses();
		}catch (SocketException e) {
			addresses = Collections.emptyList();
		}
		if(addresses.isEmpty()) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.AccountPreferencePage_27, Messages.AccountPreferencePage_28);
			networkAddressChoice = new String[][] {{"default", ""}}; //$NON-NLS-1$ //$NON-NLS-2$
		}else {
			networkAddressChoice = new String[addresses.size() + 1][2];
			networkAddressChoice[0][0] = "default"; //$NON-NLS-1$
			networkAddressChoice[0][1] = ""; //$NON-NLS-1$
			int index = 1;
			for(InetAddress address : addresses) {
				networkAddressChoice[index][0] = address.getHostAddress();
				networkAddressChoice[index][1] = networkAddressChoice[index][0];
				index++;
			}
		}
		addField(new ComboFieldEditor(PREF_SIP_NETWORK_ADDRESS, SIP_NETWORK_ADDRESS_LABEL, networkAddressChoice, parent));
		addField(new StringFieldEditor(PREF_SIP_AUTH_REALM, SIP_AUTH_REALM, parent));
		addField(new StringFieldEditor(PREF_SIP_HOST, SIP_HOST_LABEL, parent));
		addField(new IntegerFieldEditor(PREF_SIP_SERVER_PORT, SIP_SERVER_PORT_LABEL, parent));
		addField(new StringFieldEditor(PREF_SIP_USER, SIP_USER_LABEL, parent));
		addField(new StringFieldEditor(PREF_SIP_PASSWORD, SIP_PASSWORD_LABEL, parent));
		addField(new IntegerFieldEditor(PREF_ECF_PHONE_SERVER_PORT, ECF_PHONE_SERVER_PORT_LABEL, parent));
		addField(new StringFieldEditor(PREF_VOICEMAIL_EXTENSION, VOICEMAIL_LABEL, parent));
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	/**
	 * Initializes the default preference values for this preference store.
	 * 
	 * @param store
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(PREF_SIP_NETWORK_ADDRESS, ""); //$NON-NLS-1$
		store.setDefault(PREF_SIP_AUTH_REALM, "*"); //$NON-NLS-1$
		store.setDefault(PREF_SIP_HOST, ""); //$NON-NLS-1$
		store.setDefault(PREF_SIP_SERVER_PORT, 5060);
		store.setDefault(PREF_SIP_USER, ""); //$NON-NLS-1$
		store.setDefault(PREF_SIP_PASSWORD, ""); //$NON-NLS-1$
		store.setDefault(PREF_ECF_PHONE_SERVER_PORT, 30001); //$NON-NLS-1$
		store.setDefault(PREF_VOICEMAIL_EXTENSION, "2007"); //$NON-NLS-1$
	}
	
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * The soft phone preference page implementation of a
	 * <code>PreferencePage</code> method loads all the field editors with
	 * their default values.
	 */
	protected void performDefaults() {
		initDefaults(getPreferenceStore());
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if(!super.performOk()) {
			return false;
		}
		return true;
	}

}
