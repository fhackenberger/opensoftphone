package org.acoveo.callcenter.sipclient;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;

public class RingtoneSpecification {
	public RingtoneSpecification() {
	}
	public RingtoneSpecification(IPreferenceStore prefStore, Collection<String> preferenceNames) {
		Iterator<String> iter = preferenceNames.iterator();
		freq1 = (short)prefStore.getInt(iter.next());
		freq2 = (short)prefStore.getInt(iter.next());
		onMs = (short)prefStore.getInt(iter.next());
		offMs = (short)prefStore.getInt(iter.next());
		ringCount = (short)prefStore.getInt(iter.next());
		intervalMs = (short)prefStore.getInt(iter.next());
		volume = (short)prefStore.getInt(iter.next());
	}
	// The parameters of the ringtone
	public short freq1;
	public short freq2;
	public short onMs;
	public short offMs;
	public short ringCount;
	public short intervalMs;
	public short volume;
}
