package org.acoveo.callcenter.sipclient.preferences;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.Activator;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.acoveo.callcenter.sipclient.RingtoneSpecification;
import org.acoveo.tools.Tuple;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.pjsip.pjsua.pjmedia_snd_dev_info;

public class SoftPhonePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final int DEFAULT_RINGTONE_VOLUME = 2000;
	
	public static final String ID = "org.acoveo.callcenter.sipclient.preferences.SoftPhonePreferencePage"; //$NON-NLS-1$
	
	public static final String PREF_INPUT_DEVICE = "AudioDevices.inputDevice"; //$NON-NLS-1$
	public static final String PREF_OUTPUT_DEVICE = "AudioDevices.outputDevice"; //$NON-NLS-1$
	public static final String PREF_RING_DEVICE = "AudioDevices.ringDevice"; //$NON-NLS-1$
	public static final String PREF_RING_PCSPEAKER = "AudioDevices.ringPcSpeaker"; //$NON-NLS-1$
	public static final String PREF_RINGTONE_FREQ1 = "Ringtone.freq1"; //$NON-NLS-1$
	public static final String PREF_RINGTONE_FREQ2 = "Ringtone.freq2"; //$NON-NLS-1$
	public static final String PREF_RINGTONE_ON_MS = "Ringtone.onMs"; //$NON-NLS-1$
	public static final String PREF_RINGTONE_OFF_MS = "Ringtone.offMs"; //$NON-NLS-1$
	public static final String PREF_RINGTONE_COUNT = "Ringtone.count"; //$NON-NLS-1$
	public static final String PREF_RINGTONE_INTERVAL_MS = "Ringtone.intervalMs"; //$NON-NLS-1$
	public static final String PREF_RINGTONE_VOLUME = "Ringtone.volume"; //$NON-NLS-1$
	public static final String PREF_RINGBACKTONE_FREQ1 = "Ringbacktone.freq1"; //$NON-NLS-1$
	public static final String PREF_RINGBACKTONE_FREQ2 = "Ringbacktone.freq2"; //$NON-NLS-1$
	public static final String PREF_RINGBACKTONE_ON_MS = "Ringbacktone.onMs"; //$NON-NLS-1$
	public static final String PREF_RINGBACKTONE_OFF_MS = "Ringbacktone.offMs"; //$NON-NLS-1$
	public static final String PREF_RINGBACKTONE_COUNT = "Ringbacktone.count"; //$NON-NLS-1$
	public static final String PREF_RINGBACKTONE_INTERVAL_MS = "Ringbacktone.intervalMs"; //$NON-NLS-1$
	public static final String PREF_RINGBACKTONE_VOLUME = "Ringbacktone.volume"; //$NON-NLS-1$
	public static final String PREF_SPECIALTONE_FREQ1 = "SpecialTone.freq1";
	public static final String PREF_SPECIALTONE_FREQ2 = "SpecialTone.freq2";
	public static final String PREF_SPECIALTONE_FREQ3 = "SpecialTone.freq3";
	public static final String PREF_SPECIALTONE_ON_MS = "SpecialTone.onMs";
	public static final String PREF_SPECIALTONE_INTERVAL_MS = "SpecialTone.intervalMs";
	public static final String PREF_SPECIALTONE_DURATION_MS = "SpecialTone.durationMs";
	public static final String PREF_SPECIALTONE_VOLUME = "SpecialTone.volume";
	public static final String PREF_HISTORY_DAYS = "History.entries.max"; //$NON-NLS-1$
	public static final String PREF_NOTIFICATION_TIMEOUT = "Notification.timeoutSec"; //$NON-NLS-1$
	public static final String PREF_LOGOUT_AUTOMATICALLY = "Shutdown.logoutAutomatically";
	public static final String PREF_RUN_COMMAND_STRING = "RunCommand1.commandString"; //$NON-NLS-1$
	public static final String PREF_RUN_COMMAND_TEXT = "RunCommand1.buttonText"; //$NON-NLS-1$
	public static final String PREF_RUN_COMMAND_TOOLTIP = "RunCommand1.buttonTooltip"; //$NON-NLS-1$
	public static final String PREF_STRESS_TEST_MODE = "Stresstest.enabled"; //$NON-NLS-1$
	
	public static final String OUTPUT_DEVICE_LABEL = Messages.SoftPhonePreferencePage_0;
	public static final String INPUT_DEVICE_LABEL = Messages.SoftPhonePreferencePage_1;
	public static final String RING_DEVICE_LABEL = Messages.SoftPhonePreferencePage_24;
	public static final String RING_PCSPEAKER_LABEL = Messages.SoftPhonePreferencePage_25;
	public static final String RINGTONE_FREQ1_LABEL = Messages.SoftPhonePreferencePage_26;
	public static final String RINGTONE_FREQ2_LABEL = Messages.SoftPhonePreferencePage_27;
	public static final String RINGTONE_ON_MS_LABEL = Messages.SoftPhonePreferencePage_28;
	public static final String RINGTONE_OFF_MS_LABEL = Messages.SoftPhonePreferencePage_29;
	public static final String RINGTONE_COUNT_LABEL = Messages.SoftPhonePreferencePage_30;
	public static final String RINGTONE_INTERVAL_MS_LABEL = Messages.SoftPhonePreferencePage_31;
	public static final String RINGTONE_VOLUME_LABEL = Messages.SoftPhonePreferencePage_32;
	public static final String RINGBACKTONE_FREQ1_LABEL = Messages.SoftPhonePreferencePage_33;
	public static final String RINGBACKTONE_FREQ2_LABEL = Messages.SoftPhonePreferencePage_34;
	public static final String RINGBACKTONE_ON_MS_LABEL = Messages.SoftPhonePreferencePage_35;
	public static final String RINGBACKTONE_OFF_MS_LABEL = Messages.SoftPhonePreferencePage_36;
	public static final String RINGBACKTONE_COUNT_LABEL = Messages.SoftPhonePreferencePage_37;
	public static final String RINGBACKTONE_INTERVAL_MS_LABEL = Messages.SoftPhonePreferencePage_38;
	public static final String RINGBACKTONE_VOLUME_LABEL = Messages.SoftPhonePreferencePage_39;
	public static final String SPECIALTONE_FREQ1_LABEL = "Special tone frequency 1";
	public static final String SPECIALTONE_FREQ2_LABEL = "Special tone frequency 2";
	public static final String SPECIALTONE_FREQ3_LABEL = "Special tone frequency 3";
	public static final String SPECIALTONE_ON_MS_LABEL = "Special tone on (ms)";
	public static final String SPECIALTONE_INTERVAL_MS_LABEL = "Special tone interval (ms)";
	public static final String SPECIALTONE_DURATION_MS_LABEL = "Special tone duration (ms)";
	public static final String SPECIALTONE_VOLUME_LABEL = "Special tone volume";
	public static final String MAX_HISTORY_DAYS_LABEL = Messages.SoftPhonePreferencePage_40;
	public static final String NOTIFICATION_TIMEOUT_LABEL = Messages.SoftPhonePreferencePage_41;
	public static final String LOGOUT_AUTOMATICALLY_LABEL = "Logout automatically on close";
	public static final String RUN_COMMAND_STRING_LABEL = "Run command button exec"; //$NON-NLS-1$
	public static final String RUN_COMMAND_TEXT_LABEL = "Run command button text"; //$NON-NLS-1$
	public static final String RUN_COMMAND_TOOLTIP_LABEL = "Run command button tooltip"; //$NON-NLS-1$
	public static final String STRESS_TEST_MODE_LABEL = Messages.SoftPhonePreferencePage_42;

	public static final String NULL_SOUND_DEVICE = "null"; //$NON-NLS-1$
	
	/** Keep in sync with {@link RingtoneSpecification#RingtoneSpecification(IPreferenceStore, Collection)} */ 
	public static final String[] RING_TONE_PARAMETERS = {PREF_RINGTONE_FREQ1, PREF_RINGTONE_FREQ2,
		PREF_RINGTONE_ON_MS, PREF_RINGTONE_OFF_MS,
		PREF_RINGTONE_COUNT, PREF_RINGTONE_INTERVAL_MS,
		PREF_RINGTONE_VOLUME
	};

	/** Keep in sync with {@link RingtoneSpecification#RingtoneSpecification(IPreferenceStore, Collection)} */ 
	public static final String[] RING_BACK_TONE_PARAMETERS = {PREF_RINGBACKTONE_FREQ1, PREF_RINGBACKTONE_FREQ2,
		PREF_RINGBACKTONE_ON_MS, PREF_RINGBACKTONE_OFF_MS,
		PREF_RINGBACKTONE_COUNT, PREF_RINGBACKTONE_INTERVAL_MS,
		PREF_RINGBACKTONE_VOLUME
	};

	/**All properties which require a restart of the application to take effect
	 * Maps from PREF_XXX to XXX_LABEL
	 */
	public static Map<String, String> propertiesRequiringApplicationRestart;
	
	static {
		propertiesRequiringApplicationRestart = new HashMap<String, String>();
		propertiesRequiringApplicationRestart.put(AccountPreferencePage.PREF_SIP_NETWORK_ADDRESS, AccountPreferencePage.SIP_NETWORK_ADDRESS_LABEL);
	}
	
	public SoftPhonePreferencePage() {
		super(GRID);
	}
	
	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		// Collect all audio devices for the combo box
		Collection<Tuple<Integer, pjmedia_snd_dev_info>> audioDevices = null;
		RunnableFuture<Collection<Tuple<Integer, pjmedia_snd_dev_info>>> getAllSoundCardInfoFuture = new FutureTask<Collection<Tuple<Integer,pjmedia_snd_dev_info>>>(new Callable<Collection<Tuple<Integer, pjmedia_snd_dev_info>>>() {
			@Override
			public Collection<Tuple<Integer, pjmedia_snd_dev_info>> call() {
				return PjsuaClient.getAllSoundCardInfo();
			}
		});
		PjsuaClient.pjsuaWorker.asyncExec(getAllSoundCardInfoFuture);

		// We are within the display thread and must not call pjsuaWorker.syncExec
		// therefore we work around it.
		while(true) {
			try {
				audioDevices = getAllSoundCardInfoFuture.get(100, TimeUnit.MILLISECONDS);
				break;
			}catch(ExecutionException e) {
				Activator.getLogger().error("Exception while getting soundcard info", e);
				return;
			}catch(Exception e) { // Timeout or interrupted
				// Make sure we don't deadlock
				Display.getCurrent().readAndDispatch();
			}
		}
		
		String[][] deviceChoice = null;
		if(audioDevices.isEmpty()) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.SoftPhonePreferencePage_44, Messages.SoftPhonePreferencePage_45);
			deviceChoice = new String[1][2];
			deviceChoice[audioDevices.size()][0] = "<no sound>"; //$NON-NLS-1$
			deviceChoice[audioDevices.size()][1] = NULL_SOUND_DEVICE;
		}else {
			deviceChoice = new String[audioDevices.size() + 1][2];
			int index = 0;
			for(Tuple<Integer, pjmedia_snd_dev_info> device : audioDevices) {
				deviceChoice[index][0] = device.getSecond().getName();
				deviceChoice[index][1] = deviceChoice[index][0];
				index++;
			}
			deviceChoice[audioDevices.size()][0] = "<no sound>"; //$NON-NLS-1$
			deviceChoice[audioDevices.size()][1] = NULL_SOUND_DEVICE;
		}
		

		addField(new ComboFieldEditor(PREF_INPUT_DEVICE, INPUT_DEVICE_LABEL , deviceChoice, parent));
		addField(new ComboFieldEditor(PREF_OUTPUT_DEVICE, OUTPUT_DEVICE_LABEL, deviceChoice, parent));
		addField(new ComboFieldEditor(PREF_RING_DEVICE, RING_DEVICE_LABEL, deviceChoice, parent));
		addField(new BooleanFieldEditor(PREF_RING_PCSPEAKER, RING_PCSPEAKER_LABEL, parent));
		IntegerFieldEditor ringtoneField = new IntegerFieldEditor(PREF_RINGTONE_FREQ1, RINGTONE_FREQ1_LABEL, parent);
		ringtoneField.setValidRange(1, Integer.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGTONE_FREQ2, RINGTONE_FREQ2_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGTONE_ON_MS, RINGTONE_ON_MS_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGTONE_OFF_MS, RINGTONE_OFF_MS_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGTONE_COUNT, RINGTONE_COUNT_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGTONE_INTERVAL_MS, RINGTONE_INTERVAL_MS_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGTONE_VOLUME, RINGTONE_VOLUME_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGBACKTONE_FREQ1, RINGBACKTONE_FREQ1_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGBACKTONE_FREQ2, RINGBACKTONE_FREQ2_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGBACKTONE_ON_MS, RINGBACKTONE_ON_MS_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGBACKTONE_OFF_MS, RINGBACKTONE_OFF_MS_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGBACKTONE_COUNT, RINGBACKTONE_COUNT_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGBACKTONE_INTERVAL_MS, RINGBACKTONE_INTERVAL_MS_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_RINGBACKTONE_VOLUME, RINGBACKTONE_VOLUME_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_SPECIALTONE_FREQ1, SPECIALTONE_FREQ1_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_SPECIALTONE_FREQ2, SPECIALTONE_FREQ2_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_SPECIALTONE_FREQ3, SPECIALTONE_FREQ3_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_SPECIALTONE_ON_MS, SPECIALTONE_ON_MS_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_SPECIALTONE_INTERVAL_MS, SPECIALTONE_INTERVAL_MS_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_SPECIALTONE_DURATION_MS, SPECIALTONE_DURATION_MS_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		ringtoneField = new IntegerFieldEditor(PREF_SPECIALTONE_VOLUME, SPECIALTONE_VOLUME_LABEL, parent);
		ringtoneField.setValidRange(1, Short.MAX_VALUE);
		addField(ringtoneField);
		addField(new IntegerFieldEditor(PREF_HISTORY_DAYS, MAX_HISTORY_DAYS_LABEL, parent));
		IntegerFieldEditor notificationTimeoutEditor = new IntegerFieldEditor(PREF_NOTIFICATION_TIMEOUT, NOTIFICATION_TIMEOUT_LABEL, parent);
		notificationTimeoutEditor.setValidRange(-1, Integer.MAX_VALUE);
		addField(notificationTimeoutEditor);
		
		addField(new BooleanFieldEditor(PREF_LOGOUT_AUTOMATICALLY, LOGOUT_AUTOMATICALLY_LABEL, parent));
		
		addField(new StringFieldEditor(PREF_RUN_COMMAND_STRING, RUN_COMMAND_STRING_LABEL, parent));
		addField(new StringFieldEditor(PREF_RUN_COMMAND_TEXT, RUN_COMMAND_TEXT_LABEL, parent));
		addField(new StringFieldEditor(PREF_RUN_COMMAND_TOOLTIP, RUN_COMMAND_TOOLTIP_LABEL, parent));

		addField(new BooleanFieldEditor(PREF_STRESS_TEST_MODE, STRESS_TEST_MODE_LABEL, parent));
	}

	@Override
	public void init(IWorkbench workbench) {
		
	}
	
	/**
	 * Initializes the default preference values for this preference store.
	 * 
	 * @param store
	 */
	public static void initDefaults(final IPreferenceStore store) {
		int numSndDevices = PjsuaClient.pjsuaWorker.syncExecNoExc(new Callable<Integer>() {
			@Override
			public Integer call() {
				return PjsuaClient.snd_get_dev_count();
			}
		});
		if (numSndDevices > 0) {
			PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
				@Override
				public void run() {
					int[] inputDevices = { 0 };
					int[] outputDevices = { 0 };

					PjsuaClient.get_snd_dev(inputDevices, outputDevices);
					if (inputDevices[0] < 0) {
						inputDevices[0] = 0;
					}
					if (outputDevices[0] < 0) {
						outputDevices[0] = 0;
					}
					pjmedia_snd_dev_info info = new pjmedia_snd_dev_info();
					PjsuaClient.get_snd_dev_info(info, inputDevices[0]);
					store.setDefault(PREF_INPUT_DEVICE, new String(info.getName()));
					PjsuaClient.get_snd_dev_info(info, outputDevices[0]);
					store.setDefault(PREF_OUTPUT_DEVICE, new String(info.getName()));
					if (System.getProperty("os.name", "").contains("Windows")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						// See if we can find Microsoft sound mapper devices and use
						// them
						for (int devId : inputDevices) {
							PjsuaClient.get_snd_dev_info(info, devId);
							if (info.getName().contains("Microsoft Soundmapper - Input")) { //$NON-NLS-1$
								store.setDefault(PREF_INPUT_DEVICE, new String(info.getName()));
								break;
							}
						}
						for (int devId : outputDevices) {
							PjsuaClient.get_snd_dev_info(info, devId);
							if (info.getName().contains("Microsoft Soundmapper - Output")) { //$NON-NLS-1$
								store.setDefault(PREF_OUTPUT_DEVICE, new String(info.getName()));
								break;
							}
						}
					}
					store.setDefault(PREF_RING_DEVICE, NULL_SOUND_DEVICE);
				}
			});
		} else {
			store.setDefault(PREF_INPUT_DEVICE, NULL_SOUND_DEVICE);
			store.setDefault(PREF_OUTPUT_DEVICE, NULL_SOUND_DEVICE);
			store.setDefault(PREF_RING_DEVICE, NULL_SOUND_DEVICE);
		}
		store.setDefault(PREF_RING_PCSPEAKER, false);
		store.setDefault(PREF_RINGTONE_FREQ1, 800);
		store.setDefault(PREF_RINGTONE_FREQ2, 640);
		store.setDefault(PREF_RINGTONE_ON_MS, 200);
		store.setDefault(PREF_RINGTONE_OFF_MS, 100);
		store.setDefault(PREF_RINGTONE_COUNT, 3);
		store.setDefault(PREF_RINGTONE_INTERVAL_MS, 3000);
		store.setDefault(PREF_RINGTONE_VOLUME, DEFAULT_RINGTONE_VOLUME);
		store.setDefault(PREF_RINGBACKTONE_FREQ1, 400);
		store.setDefault(PREF_RINGBACKTONE_FREQ2, 450);
		store.setDefault(PREF_RINGBACKTONE_ON_MS, 400);
		store.setDefault(PREF_RINGBACKTONE_OFF_MS, 200);
		store.setDefault(PREF_RINGBACKTONE_COUNT, 2);
		store.setDefault(PREF_RINGBACKTONE_INTERVAL_MS, 2000);
		store.setDefault(PREF_RINGBACKTONE_VOLUME, DEFAULT_RINGTONE_VOLUME);
		store.setDefault(PREF_SPECIALTONE_FREQ1, 900);
		store.setDefault(PREF_SPECIALTONE_FREQ2, 1400);
		store.setDefault(PREF_SPECIALTONE_FREQ3, 1800);
		store.setDefault(PREF_SPECIALTONE_ON_MS, 400);
		store.setDefault(PREF_SPECIALTONE_INTERVAL_MS, 1000);
		store.setDefault(PREF_SPECIALTONE_DURATION_MS, 3400);
		store.setDefault(PREF_SPECIALTONE_VOLUME, DEFAULT_RINGTONE_VOLUME);
		store.setDefault(PREF_HISTORY_DAYS, "30"); //$NON-NLS-1$
		store.setDefault(PREF_NOTIFICATION_TIMEOUT, "0"); //$NON-NLS-1$
		store.setDefault(PREF_LOGOUT_AUTOMATICALLY, false);
		store.setDefault(PREF_RUN_COMMAND_STRING, "");
		store.setDefault(PREF_RUN_COMMAND_TEXT, "");
		store.setDefault(PREF_RUN_COMMAND_TOOLTIP, "");
		store.setDefault(PREF_STRESS_TEST_MODE, false);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * The soft phone preference page implementation of a
	 * <code>PreferencePage</code> method loads all the field editors with
	 * their default values.
	 */
	protected void performDefaults() {
		initDefaults(getPreferenceStore());
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if(!super.performOk()) {
			return false;
		}
		return true;
	}
	
}
