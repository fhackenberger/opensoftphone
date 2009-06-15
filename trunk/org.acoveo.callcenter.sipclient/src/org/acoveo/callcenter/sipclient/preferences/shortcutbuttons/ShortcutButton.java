package org.acoveo.callcenter.sipclient.preferences.shortcutbuttons;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Implements button specific fields.
 * @author brandner
 *
 */
public class ShortcutButton extends ShortcutItem {
	
	private static final String PREF_NUMBER = "number";
	private static final String PREF_FORWARD = "forward";
	private static final String PREF_DIALPAD = "dialpad";
	private static final String PREF_SUFFIXDIALING = "suffixDialing";
	
	private String number = "";
	private boolean forward = false;
	private boolean dialpad = false;
	private boolean suffixDialing = false;
	

	public ShortcutButton(String name, boolean active, int position) {
		super(name, active, position);
		// TODO Auto-generated constructor stub
	}

	public ShortcutButton(IPreferenceStore store, String prefix) {
		super(store, prefix);
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public boolean isForward() {
		return forward;
	}

	public void setForward(boolean forward) {
		this.forward = forward;
	}

	public boolean isDialpad() {
		return dialpad;
	}

	public void setDialpad(boolean dialpad) {
		this.dialpad = dialpad;
	}

	public boolean isSuffixDialing() {
		return suffixDialing;
	}

	public void setSuffixDialing(boolean suffixDialing) {
		this.suffixDialing = suffixDialing;
	}

	@Override
	protected boolean doLoad() {
		IPreferenceStore store = getPreferenceStore();
		setNumber(store.getString(getPreferencePrefix() + PREF_NUMBER));
		setForward(store.getBoolean(getPreferencePrefix() + PREF_FORWARD));
		setDialpad(store.getBoolean(getPreferencePrefix() + PREF_DIALPAD));
		setSuffixDialing(store.getBoolean(getPreferencePrefix() + PREF_SUFFIXDIALING));
		return true;
	}

	@Override
	protected boolean doRemove() {
		IPreferenceStore store = getPreferenceStore();
		store.setToDefault(getPreferencePrefix() + PREF_NUMBER);
		store.setToDefault(getPreferencePrefix() + PREF_FORWARD);
		store.setToDefault(getPreferencePrefix() + PREF_DIALPAD);
		store.setToDefault(getPreferencePrefix() + PREF_SUFFIXDIALING);
		return true;
	}

	@Override
	protected boolean doStore() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(getPreferencePrefix() + PREF_NUMBER, getNumber());
		store.setValue(getPreferencePrefix() + PREF_FORWARD, isForward());
		store.setValue(getPreferencePrefix() + PREF_DIALPAD, isDialpad());
		store.setValue(getPreferencePrefix() + PREF_SUFFIXDIALING, isSuffixDialing());
		return true;
	}

	@Override
	protected String getChildPreferencePrefix() {
		return null;
	}

	public static ShortcutButton load(IPreferenceStore store,
			String prefix) {
		ShortcutButton sb = new ShortcutButton(store, prefix);
		if(!sb.exists()) {
			return null;
		}
		sb.load();
		return sb;
	}

}
