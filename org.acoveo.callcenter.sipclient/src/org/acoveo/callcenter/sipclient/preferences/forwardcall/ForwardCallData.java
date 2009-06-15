package org.acoveo.callcenter.sipclient.preferences.forwardcall;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

public class ForwardCallData {
	public static class ForwardCallEntry implements Comparable<ForwardCallEntry> {
		private static String suffixNumber = "number";
		private static String suffixRingCount = "ringCount";
		private static String suffixIfBusy = "ifBusy";
		
		private int ringCount;
		private String number;
		private boolean ifBusy;
		
		private ForwardCallEntry() {
			
		}
		
		public ForwardCallEntry(String number, int ringCount, boolean ifBusy) {
			this.number = number;
			this.ringCount = ringCount;
			this.ifBusy = ifBusy;
		}

		public String getNumber() {
			return number;
		}

		public void setNumber(String number) {
			this.number = number;
		}

		public int getRingCount() {
			return ringCount;
		}

		public void setRingCount(int ringCount) {
			this.ringCount = ringCount;
		}

		public static ForwardCallEntry load(IPreferenceStore store, String prefix) {
			if(!prefix.endsWith(".")) {
				prefix += ".";
			}
			if(!store.contains(prefix + suffixNumber)) {
				return null;
			}
			ForwardCallEntry e = new ForwardCallEntry();
			e.setIfBusy(store.getBoolean(prefix + suffixIfBusy));
			e.setNumber(store.getString(prefix + suffixNumber));
			e.setRingCount(store.getInt(prefix + suffixRingCount));
			return e;
		}
		
		public void store(IPreferenceStore store, String prefix) {
			if(!prefix.endsWith(".")) {
				prefix += ".";
			}
			try {
			store.setValue(prefix + suffixNumber, getNumber());
			store.setValue(prefix + suffixRingCount, getRingCount());
			store.setValue(prefix + suffixIfBusy, isIfBusy());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void remove(IPreferenceStore store, String prefix) {
			ForwardCallEntry.removeByPrefix(store, prefix);
		}
				
		public static void removeByPrefix(IPreferenceStore store, String prefix) {
			if(!prefix.endsWith(".")) {
				prefix += ".";
			}
			store.setToDefault(prefix + suffixNumber);
			store.setToDefault(prefix + suffixRingCount);
			store.setToDefault(prefix + suffixIfBusy);
		}
		
		@Override
		public int compareTo(ForwardCallEntry o) {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ForwardCallEntry) {
				ForwardCallEntry e = (ForwardCallEntry) obj;
				if(getNumber().equals(e.getNumber()) && getRingCount() == e.getRingCount()) {
					return true;
				}
			}
			return false;
		}

		public void setIfBusy(boolean ifBusy) {
			this.ifBusy = ifBusy;
		}

		public boolean isIfBusy() {
			return ifBusy;
		}
	} // class ForwardCallEntry
	
	public static final String PREF_NAME_PREFIX = "CallForward.";
	private int preferenceMaxHistory = 5;
	private List<ForwardCallEntry> historyEntries;
	private ForwardCallEntry forward;
	private static Boolean available = null;
	
	public ForwardCallData(IPreferenceStore store) {
		loadData(store);
	}
	
	public static boolean isAvailable(IPreferenceStore store) {
		if(available == null) {
			String key = PREF_NAME_PREFIX + "enabled";
			available = new Boolean(store.contains(key) && store.getBoolean(key));
		}
		return available.booleanValue();
	}
	
	private void loadData(IPreferenceStore store) {
		// load active forward if available
		forward = ForwardCallEntry.load(store, PREF_NAME_PREFIX);
		
		// load history
		historyEntries = new ArrayList<ForwardCallEntry>(preferenceMaxHistory);
		for(int i=0; i < preferenceMaxHistory; i++) {
			ForwardCallEntry e = ForwardCallEntry.load(store, PREF_NAME_PREFIX + Integer.toString(i));
			if(e == null) {
				break;
			}
			historyEntries.add(e);
		}
	}
	
	public ForwardCallEntry getActiveForward() {
		return forward;
	}
	
	public List<ForwardCallEntry> getHistoryEntries() {
		return historyEntries;
	}

	/**
	 * 
	 * @param number the destination number
	 * @param ringCount When <b>ifBusy</b> is <i>false</i>, the value <i>0</i> means immediately forward, 
	 * else it's the number of seconds to wait.
	 * @param ifBusy forward the call if the phone is busy or the call is rejected.
	 */
	public ForwardCallEntry setForward(IPreferenceStore store, String number, int ringCount, boolean ifBusy) {
		forward = new ForwardCallEntry(number, ringCount, ifBusy);
		
		// remove from history if its already used
		historyEntries.remove(forward);
		// and add the forward at first position
		historyEntries.add(0, forward);
		
		store(store);
		return forward;
	}
	
	private void store(IPreferenceStore store) {
		for(int i=0; i<preferenceMaxHistory; i++) {
			String pref = PREF_NAME_PREFIX + Integer.toString(i);
			ForwardCallEntry.removeByPrefix(store, pref);
			
			if(historyEntries.get(i) != null) {
				historyEntries.get(i).store(store, pref);
			}
		}
	}
}
