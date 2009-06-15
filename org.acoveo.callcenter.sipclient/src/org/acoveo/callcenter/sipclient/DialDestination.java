package org.acoveo.callcenter.sipclient;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class DialDestination {
	StringBuffer dialDestination = new StringBuffer();
	private ListenerList propertyChangeListeners = new ListenerList();

	public void addListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}

	public void removeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}

	public void append(String string) {
		dialDestination.append(string);
		firePropertyChange();
	}
	
	public void replace(int start, int end, String str) {
		if(str != null) {
			dialDestination.replace(start, end, str);
			firePropertyChange();
		}
	}
	
	public void clear() {
		dialDestination.delete(0, dialDestination.length());
		firePropertyChange();
	}
	
	public void backspace() {
		if(dialDestination.length() > 0) {
			dialDestination.delete(dialDestination.length()-1, dialDestination.length());
			firePropertyChange();
		}
	}
	
	public String getDialDestination() {
		return dialDestination.toString();
	}

	private void firePropertyChange() {
		PropertyChangeEvent event = new PropertyChangeEvent(this, "dialDestination", null, dialDestination.toString()); //$NON-NLS-1$
		Object[] listeners = propertyChangeListeners.getListeners();
		for (Object listener : listeners) {
			((IPropertyChangeListener)listener).propertyChange(event);
		}
	}
}
