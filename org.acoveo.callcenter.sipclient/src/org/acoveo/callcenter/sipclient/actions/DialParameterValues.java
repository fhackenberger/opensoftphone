package org.acoveo.callcenter.sipclient.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

public class DialParameterValues implements IParameterValues {
	public static final Map<String, String> parameterValues;
	
	static {
		parameterValues = new HashMap<String, String>();
		parameterValues.put("Dial 0", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial 1", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial 2", "2"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial 3", "3"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial 4", "4"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial 5", "5"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial 6", "6"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial 7", "7"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial 8", "8"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial 9", "9"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial *", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		parameterValues.put("Dial #", "#"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public Map<String,String> getParameterValues() {
		return parameterValues;
	}
}
