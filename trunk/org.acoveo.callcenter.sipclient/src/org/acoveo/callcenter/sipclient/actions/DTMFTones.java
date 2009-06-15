package org.acoveo.callcenter.sipclient.actions;

import java.util.HashMap;
import java.util.Map;

public class DTMFTones {
	public static final Map<String, String> DTMF_TONES;
	public static final double DTMF_AMPLITUDE = 0.1;
	
	private static String duration = "333"; //$NON-NLS-1$
	
	// from http://www.mediacollege.com/audio/tone/dtmf.html
	private static String[] freqCols = {"1209","1336","1477"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static String[] freqRows = {"697","770","852","941"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	static {
		DTMF_TONES = new HashMap<String, String>();
		DTMF_TONES.put("1", freqCols[0] + "+" + freqRows[0] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("2", freqCols[1] + "+" + freqRows[0] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("3", freqCols[2] + "+" + freqRows[0] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("4", freqCols[0] + "+" + freqRows[1] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("5", freqCols[1] + "+" + freqRows[1] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("6", freqCols[2] + "+" + freqRows[1] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("7", freqCols[0] + "+" + freqRows[2] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("8", freqCols[1] + "+" + freqRows[2] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("9", freqCols[2] + "+" + freqRows[2] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("*", freqCols[0] + "+" + freqRows[3] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("0", freqCols[1] + "+" + freqRows[3] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		DTMF_TONES.put("#", freqCols[2] + "+" + freqRows[3] + "/" + duration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
