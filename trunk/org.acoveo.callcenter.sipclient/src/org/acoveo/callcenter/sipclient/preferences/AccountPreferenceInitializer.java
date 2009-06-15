package org.acoveo.callcenter.sipclient.preferences;

import org.acoveo.callcenter.sipclient.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class AccountPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = getPreferenceStore();
		AccountPreferencePage.initDefaults(store);
	}

	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

}
