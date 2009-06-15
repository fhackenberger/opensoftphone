/**
 * Baseclass for shortcutbuttons and groups/tenants.
 * 
 * This im plements the basic functionality and field.
 * 
 * @author brandner.s@nekom.com
 */
package org.acoveo.callcenter.sipclient.preferences.shortcutbuttons;

import org.eclipse.jface.preference.IPreferenceStore;

public abstract class ShortcutItem implements Comparable<ShortcutItem> {
	
	private static final String PREF_NAME = "name";
	private static final String PREF_ACTIVE = "active";
	private static final String PREF_POSITION = "position";
	
	private String name = "";
	private int position = 0;
	private boolean active = true;
	private IPreferenceStore preferenceStore;
	private String preferencePrefix;
	
	protected ShortcutItem(String name, boolean active, int position) {
		this.name = name;
		this.active = active;
		this.position = 0;
	}
	
	protected ShortcutItem(IPreferenceStore store, String prefix) {
		preferenceStore = store;
		setPreferencePrefix(prefix);
	}
	
	public ShortcutItem setPreferenceStore(IPreferenceStore store) {
		this.preferenceStore = store;
		return this;
	}
	
	public IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}
	
	public String getName() {
		return name;
	}
	
	public ShortcutItem setName(String name) {
		this.name = name;
		return this;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public ShortcutItem setActive(boolean active) {
		this.active = active;
		return this;
	}
	
	public int getPosition() {
		return position;
	}
	
	/**
	 * Set the position inside the sort sequence.
	 * @param position 1: highest priority, n: lower priority, 0: disable ordering, results in position at the end
	 * @return self
	 */
	public ShortcutItem setPosition(int position) {
		this.position = (position < 0 ? 0 : position);
		return this;
	}
	
	/**
	 * Prefix for preference name 
	 * @return String with a "." at the end
	 */
	protected abstract String getChildPreferencePrefix();
	
	public ShortcutItem setPreferencePrefix(String prefix) {
		if(!prefix.endsWith(".") && prefix.length() > 0) {
			prefix+= ".";
		}
		this.preferencePrefix = prefix;
		return this;
	}
	
	public String getPreferencePrefix() {
		return preferencePrefix;
	}
	
	public final boolean store() {
		if(getPreferenceStore() == null || preferencePrefix == null) {
			return false;
		}
		log("store", "store prefix " + preferencePrefix);
		IPreferenceStore store = getPreferenceStore();
		store.setValue(preferencePrefix + PREF_NAME, getName());
		store.setValue(preferencePrefix + PREF_ACTIVE, isActive());
		store.setValue(preferencePrefix + PREF_POSITION, getPosition());
		return doStore();
	}
	
	/**
	 * Groups and Buttons have to implement a <code>doStore()</code> 
	 * function that can be called from the preference page.
	 * 
	 * @return <code>true</code> if the data could be written to the store.
	 */
	protected abstract boolean doStore();
	
	/**
	 * Removes the item from the store.
	 * @return
	 */
	public final boolean remove() {
		if(getPreferenceStore() == null || preferencePrefix == null) {
			return false;
		}
		log("remove", "remove prefix " + preferencePrefix);
		IPreferenceStore store = getPreferenceStore();
		store.setToDefault(preferencePrefix + PREF_NAME);
		store.setToDefault(preferencePrefix + PREF_ACTIVE);
		store.setDefault(preferencePrefix + PREF_POSITION, -1);
		store.setToDefault(preferencePrefix + PREF_POSITION);
		return doRemove();
	}
	
	protected abstract boolean doRemove();
	
	/**
	 * Load default values from ShortcutItem
	 */
	public final boolean load() {
		if(getPreferenceStore() == null || getPreferencePrefix() == null) {
			return false;
		}
		IPreferenceStore store = getPreferenceStore();
		log("load", "load prefix " + preferencePrefix);
		setName(store.getString(preferencePrefix + PREF_NAME));
		setActive(store.getBoolean(preferencePrefix + PREF_ACTIVE));
		setPosition(store.getInt(preferencePrefix + PREF_POSITION));
		return doLoad();
	}
	
	protected abstract boolean doLoad();

	/**
	 * 
	 * @param store
	 * @param prefix Prefix, e.g. "Shorcut.Group.". If ending "." is missing
	 * @return
	 */
	protected boolean exists() {
		return preferenceStore.contains(preferencePrefix + PREF_NAME);
	}

	@Override
	public int compareTo(ShortcutItem o) {
		if(position == o.getPosition()) {
			// if the position is the same, sort by name
			return name.compareToIgnoreCase(o.getName());
		}
		
		if(position == 0) {
			// only this could be 0, 0 means at the end
			return 1;
		} else if(o.getPosition()==0) {
			// other is at the end
			return -1;
		}
		
		return (position < o.getPosition() ? -1 : 1);
	}

	protected void log(String function, String message) {
		//System.out.println("ShortcutItem::"+ function +"() - " + message);
	}
}
