package org.acoveo.callcenter.sipclient.preferences;

import org.acoveo.callcenter.sipclient.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class SoftPhonePreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = getPreferenceStore();
		SoftPhonePreferencePage.initDefaults(store);
	}

	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
