package org.acoveo.callcenter.sipclient.preferences.shortcutbuttons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Implements the field for tenants and a list of ShortcutButton objects.
 * 
 * @author brandner
 *
 */
public class ShortcutButtonGroup extends ShortcutItem {
	
	public static final String PREF_TENANT = "tenant";
	public static final String PREF_COLUMN_COUNT = "columnCount";
	
	private String tenant = "";
	private int columnCount;
	private List<ShortcutButton> buttons = new ArrayList<ShortcutButton>();

	protected ShortcutButtonGroup(IPreferenceStore store, String prefix) {
		super(store, prefix);
	}
	
	public ShortcutButtonGroup(String name, String tenant, boolean active, int position) {
		super(name, active, position);
		this.setTenant(tenant);
	}
	
	public ShortcutButtonGroup setTenant(String tenant) {
		this.tenant = tenant;
		return this;
	}

	public String getTenant() {
		return tenant;
	}

	@Override
	public boolean doStore() {
		boolean result = storeGroup();
		// start saving only if group is saved
		if(result && buttons != null) {
			for(int i=0; i < buttons.size(); i++) {
				ShortcutButton b = buttons.get(i);
				b.setPreferenceStore(getPreferenceStore());
				b.setPreferencePrefix(getChildPreferencePrefix(i));
				if(!b.store()) {
					result = false;
				}
			}
		}
		return result;
	}
	/**
	 * Store private fields
	 * @return true on success
	 */
	private boolean storeGroup() {
		getPreferenceStore().setValue(getPreferencePrefix() + PREF_TENANT, getTenant());
		getPreferenceStore().setValue(getPreferencePrefix() + PREF_COLUMN_COUNT, getColumnCount());
		return true;
	}
	
	@Override
	protected boolean doRemove() {
		String pref = getPreferencePrefix();
		getPreferenceStore().setToDefault(pref + PREF_TENANT);
		getPreferenceStore().setToDefault(pref + PREF_COLUMN_COUNT);
		return doRemoveShortcutButtons();
	}
	
	private boolean doRemoveShortcutButtons() {
		for(ShortcutButton btn: getShortcutButtons()) {
			btn.remove();
		}
		return true;
	}

	@Override
	protected String getChildPreferencePrefix() {
		return getChildPreferencePrefix(-1);
	}
	
	protected String getChildPreferencePrefix(int pos) {
		// "Shortcut.Button.<name>"
		StringBuffer sb = new StringBuffer(ShortcutButtonGroupsPreferencePage.PREF_SHORTCUT_BUTTON_PREFIX);
		sb.append(".").append(getName());
		if(pos > -1) {
			sb.append(".").append(pos);
		}
		return sb.toString();
	}

	/**
	 * Loads a group and the buttons from a given store with the prefix.
	 * @param store
	 * @param prefix
	 * @return
	 */
	public static ShortcutButtonGroup load(IPreferenceStore store, String prefix) {
		ShortcutButtonGroup grp = new ShortcutButtonGroup(store, prefix);
		if(!grp.exists()) {
			// if no name is set, return nothing
			return null;
		}
		grp.load();
		return grp;
	}

	@Override
	protected boolean doLoad() {
		IPreferenceStore store = getPreferenceStore();
		setTenant(store.getString(getPreferencePrefix() + PREF_TENANT));
		setColumnCount(store.getInt(getPreferencePrefix() + PREF_COLUMN_COUNT));
		return doLoadButtons();
	}
	
	private boolean doLoadButtons() {
		int i = 0, failed = 0;
		String prefix = getChildPreferencePrefix();
		StringBuffer sb;
		while(i < ShortcutButtonGroupsPreferencePage.MAX_SHORTCUT_BUTTONS && failed < 3) {
			sb = new StringBuffer(prefix);
			sb.append(".").append(i).append(".");
			ShortcutButton btn = ShortcutButton.load(getPreferenceStore(), sb.toString());
			if(btn!=null) {
				buttons.add(btn);
			} else {
				failed++;
			}
			i++;
		}
		return true;
	}
	
	public List<ShortcutButton> getShortcutButtons() {
		return buttons;
	}

	public ShortcutButton addButton() {
		ShortcutButton button = new ShortcutButton("", true, 0);
		buttons.add(button);
		return button;
	}

	public void sortButtons() {
		Collections.sort(buttons);
		for(int i=0; i<buttons.size(); i++) {
			ShortcutButton btn = buttons.get(i);
			if(btn.getPosition() > 0) {
				// 0 should be always 0 to stay at the end of the list
				// multiplicator 10 is used to be able to use the number 15,
				// to set the position between 10 and 20, its easier than rewrite every position
				btn.setPosition( (i+1) * 10 );
			}
		}
	}

	public int size() {
		return buttons.size();
	}

	public boolean removeIfChanged() {
		// FIXME nur entfernen wenn sich etwas grundlegend wichtiges geändert hat. in dem fall wäre das die position. performance steigerung
		return remove();
	}

	public List<ShortcutButton> getShortcutButtons(boolean activeState) {
		List<ShortcutButton> result = new ArrayList<ShortcutButton>();
		for(ShortcutButton btn: buttons) {
			if(btn.isActive()) {
				result.add(btn);
			}
		}
		return result;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = (columnCount < 1 ? 1 : columnCount);
	}

	public int getColumnCount() {
		return columnCount;
	}

	public boolean removeButton(ShortcutButton button) {
		if(buttons.contains(button)) {
			buttons.remove(button);
			button.remove();
			return true;
		}
		return false;
	}
}
