package org.acoveo.callcenter.sipclient.view;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;

import org.acoveo.callcenter.guiLibrary.TableSelectionColorChangeListener;
import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.Activator;
import org.acoveo.callcenter.sipclient.CallInformation;
import org.acoveo.callcenter.sipclient.IPhoneStateListener;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneState;
import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneStateTransition;
import org.acoveo.callcenter.sipclient.actions.CallAction;
import org.acoveo.callcenter.sipclient.actions.CallAnswerAction;
import org.acoveo.callcenter.sipclient.actions.CallHistoryAction;
import org.acoveo.callcenter.sipclient.actions.ClearDestinationAction;
import org.acoveo.callcenter.sipclient.actions.ClearHistoryAction;
import org.acoveo.callcenter.sipclient.actions.ConferenceAction;
import org.acoveo.callcenter.sipclient.actions.ConsultationHoldAction;
import org.acoveo.callcenter.sipclient.actions.DialAction;
import org.acoveo.callcenter.sipclient.actions.FilterInternalNrsAction;
import org.acoveo.callcenter.sipclient.actions.ForwardCallAction;
import org.acoveo.callcenter.sipclient.actions.HangupAction;
import org.acoveo.callcenter.sipclient.actions.ISelfUpdatingAction;
import org.acoveo.callcenter.sipclient.actions.PickupAction;
import org.acoveo.callcenter.sipclient.actions.RunExternalCommandAction;
import org.acoveo.callcenter.sipclient.actions.SettingsAction;
import org.acoveo.callcenter.sipclient.actions.ShowHideTimeAction;
import org.acoveo.callcenter.sipclient.actions.TransferAction;
import org.acoveo.callcenter.sipclient.actions.VoicemailAction;
import org.acoveo.callcenter.sipclient.preferences.AccountPreferencePage;
import org.acoveo.callcenter.sipclient.preferences.SoftPhonePreferencePage;
import org.acoveo.callcenter.sipclient.preferences.forwardcall.ForwardCallData;
import org.acoveo.callcenter.sipclient.preferences.shortcutbuttons.ShortcutButtonGroup;
import org.acoveo.callcenter.sipclient.preferences.shortcutbuttons.ShortcutButtonGroupsPreferencePage;
import org.acoveo.callcenter.sipclient.view.CallHistory.CallHistoryEntry;
import org.acoveo.callcenter.sipclient.view.shortcutbutton.ShortcutButtonTabItem;
import org.acoveo.tools.DelayedRunnable;
import org.acoveo.tools.Filesystem;
import org.acoveo.tools.Tuple;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.pjsip.pjsua.Callback;
import org.pjsip.pjsua.pj_str_t;

public class SoftPhoneViewPart extends ViewPart {
	// Specifies how long we wait before applying a series of preference changes
	public static final long PREFERENCES_APPLY_DELAY_MS = 300;
	protected class PhoneStateListener extends Callback implements IPhoneStateListener {
		final Composite parent;
		
		public PhoneStateListener(Composite parent) {
			this.parent = parent;
		}

		@Override
		public void stateChanged(PjsuaClient.PhoneState oldState, final PjsuaClient.PhoneState newState,
				PjsuaClient.PhoneStateTransition transitionReason) {
			parent.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					updateAllActions();
					updateCallerIdText();
				}
			});
			if(oldState == PhoneState.INCOMING_CALL) {
				final SoftPhoneNotificationPopupDialog pd = popupDialogue;
				if(pd != null) {
					// Stop the popup as there is no incoming call anymore
					popupDialogue = null;
					parent.getDisplay().asyncExec(new Runnable() {
						public void run() {
							pd.close();
						}
					});
				}
			}

			switch (newState) {
			case OUTGOING_CALL: {
				PjsuaClient.ringBackStart();
				break;
			}
			case INCOMING_CALL: {
				PjsuaClient.ringStart();
				newState.callIndicateRinging();
				int callId = newState.getRingingCallId();
				if (callId >= 0) {
					final CallInformation callInfo = newState.getCallerInformation();
					IPreferenceStore store = Activator.getDefault().getPreferenceStore();
					final int timeout = store.getInt(SoftPhonePreferencePage.PREF_NOTIFICATION_TIMEOUT);
					if(timeout >= 0) {
						parent.getDisplay().asyncExec(new Runnable() {
							public void run() {
								Collection<NotificationContent> contentList = new LinkedList<NotificationContent>();
								NotificationContent content = new NotificationContent();
								Collection<ActionContributionItem> actionsList = new LinkedList<ActionContributionItem>();
	
								CallAnswerAction callAnswerAction = new CallAnswerAction();
								callAnswerAction.update();
	
								HangupAction hangupAction = new HangupAction();
								hangupAction.update();
	
								actionsList.add(new ActionContributionItem(callAnswerAction));
								actionsList.add(new ActionContributionItem(hangupAction));
	
								content.actions = actionsList;
								content.message = callInfo.getDisplayString() + Messages.SoftPhoneViewPart_0;
								content.image = IconStore.getDefault().getImageRegistry().get(IconStore.SOFT_PHONE_VIEW);
								contentList.add(content);
								popupDialogue = new SoftPhoneNotificationPopupDialog(parent.getShell(), timeout);
								popupDialogue.setContents(contentList);
								popupDialogue.open();
							}
						});
					}
				}
				break;
			}
			case UNREGISTERED: {
				PjsuaClient.ringStop();
				PjsuaClient.ringBackStop();
				parent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateStatusIcon();
					}
				});
				break;
			}
			default: {
				PjsuaClient.ringStop();
				PjsuaClient.ringBackStop();
				if (oldState == PhoneState.OUTGOING_CALL && transitionReason == PhoneStateTransition.CALL_DISCONNECTED_NOT_FOUND) {
					specialRingTimer.ringSpecial();
				}
				if (oldState == PjsuaClient.PhoneState.UNREGISTERED) {
					parent.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							updateStatusIcon();
						}
					});
				}
				break;
			}
			}
		}

		@Override
		public void on_pager(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, pj_str_t mime_type,
				pj_str_t body) {
			if (statusLine != null) {
				final String message = body.getPtr();
				if(!PjsuaClient.isProtocolMessage(message)) {
					Display display = statusLine.getDisplay();
					if (display != null) {
						display.asyncExec(new Runnable() {
							public void run() {
								statusLine.setText(message);
							}
						});
					}
				}
			}
		}

		@Override
		public void callerInformationChanged(final CallInformation callInformation) {
			parent.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					activateShortcutButtonTabForSubject(callInformation);
				}
			});
			
		}
		
		@Override
		public void conferencingChanged(boolean newEnabled) {
			Display display = registeredLabel.getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						updateAllActions();
					}
				});
			}
		}
	}

	/** This class provides support for playing the special tone for a certain time
	 * 
	 * The {@code PjsuaClient#specialToneStart()} and {@code PjsuaClient#specialToneStop()}
	 * method must be called from the GUI thread, therefore we need a display.
	 * @author Florian Hackenberger (florian.hackenberger@acoveo.com)
	 *
	 */
	protected class SpecialRingTimer {
		public int timeoutMs;
		Display display;
		Executor threadPool = Executors.newFixedThreadPool(1);
		FutureTask<Boolean> specialRingFutureTask = null;
		
		public SpecialRingTimer(Display parent, int timeout) {
			this.display = parent;
			this.timeoutMs = timeout;
		}
		
		protected class SpecialRingTask implements Callable<Boolean> {
			@Override
			public Boolean call() throws Exception {
				display.syncExec(new Runnable() {
					@Override
					public void run() {
						PjsuaClient.specialToneStart();
					}
				});
				try {
					// Give the property change listeners a chance to interrupt us
					Thread.sleep(timeoutMs);
				}catch (InterruptedException e) {
					// ignore
				}
				display.syncExec(new Runnable() {
					@Override
					public void run() {
						PjsuaClient.specialToneStop();
					}
				});
				specialRingFutureTask = null;
				return true;
			}
		};
		public synchronized void ringSpecial() {
			if(specialRingFutureTask == null) {
				specialRingFutureTask = new FutureTask<Boolean>(new SpecialRingTask());
				threadPool.execute(specialRingFutureTask);
			}
		}
	}

	/** Applies a series of preference changes stored in changeEvents
	 */
	protected class ApplyPreferencesTask implements Callable<Boolean> {
		Composite parent;
		
		public ApplyPreferencesTask(Composite parent) {
			this.parent = parent;
		}
		@Override
		public Boolean call() throws Exception {
			synchronized (changeEvents) {
				// Apply the changes
				parent.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						boolean applyChanges = true;
						// Check if the changes require an application restart
						LinkedList<PropertyChangeEvent> restartEvents = new LinkedList<PropertyChangeEvent>();
						if(applicationRestartRequired(changeEvents, restartEvents)) {
							StringBuilder message = new StringBuilder(Messages.SoftPhoneViewPart_1 + System.getProperty("line.separator")); //$NON-NLS-1$
							for(PropertyChangeEvent event : restartEvents) {
								String label = SoftPhonePreferencePage.propertiesRequiringApplicationRestart.get(event.getProperty());
								if(label != null) {
									message.append(label + ": " + event.getNewValue().toString() + System.getProperty("line.separator")); //$NON-NLS-1$ //$NON-NLS-2$
								}
							}
							message.append(Messages.SoftPhoneViewPart_5);
							if(MessageDialog.openQuestion(parent.getShell(), Messages.SoftPhoneViewPart_6, message.toString())) {
								if(PlatformUI.getWorkbench().restart()) {
									changeEvents.clear();
									applyChanges = false;
								}
							}
						}
						if(!applyChanges) {
							return;
						}
						String newSipUser = Activator.getDefault().getPreferenceStore().getString(AccountPreferencePage.PREF_SIP_USER);
						String newSipPass = Activator.getDefault().getPreferenceStore().getString(AccountPreferencePage.PREF_SIP_PASSWORD);
						if(!newSipUser.isEmpty()) {
							// We only set the sip user and pass if it would overwrite the current user with a meaningful value
							PjsuaClient.setSipUser(newSipUser);
							PjsuaClient.setSipPwd(newSipPass);
						}
						
						agentIDLabel.setText(PjsuaClient.getSipUser());
						agentIDLabel.pack();
						agentIDLabel.getParent().layout();
						agentIDLabel.getParent().getParent().layout();
						
						createShortcutButtons();
						createShortcutButtonsDialpad(null);
						updateRunCommandAction();
						
						updateAllActions();
						changeEvents.clear();
						
						PjsuaClient.pjsuaWorker.asyncExec(new Runnable() {
							@Override
							public void run() {
								PjsuaClient.setAudioDevices();
								PjsuaClient.connectToSipServer();
							}
						});
					}
				});
			}
			return true;
		}
	};
	
	/** Makes sure we don't reconnect too often
	 * This method serialises property changed events for the {@link SoftPhonePreferencePage}
	 * and makes sure that all events received within {@code #PREFERENCES_APPLY_DELAY_MS}
	 * millisecs are consumed by one sip reconnect. The reason for this scheme is that the eclipse
	 * preference store sends one {@link PropertyChangeEvent} per field which has
	 * been changed by the user, which would cause N reconnects for N changed fields.
	 */
	protected void putPropertyChangeEvent(PropertyChangeEvent event) {
		if(event.getProperty().startsWith(AgentStatisticListener.PREF_NAME_PREFIX)
				|| event.getProperty().startsWith(ForwardCallData.PREF_NAME_PREFIX)) {
			// ignore changes from AgentStatisticListener properties
			return;
		}
		synchronized (changeEvents) {
			changeEvents.add(event);
		}
		applyPreferencesDelayedRunnable.requestExecution();
	}
	
	protected class MasterVolumePollRunnable implements Runnable {
		@Override
		public void run() {
			while(true) {
				//XXX We can never be sure if the masterVolumeControlWidget has just been disposed
				try {
					Tuple<Float, Float> volumeAndRange = new Tuple<Float, Float>();
					Line line = getCurrentMasterVolumeAndRange(volumeAndRange);
					if(masterVolumeControlWidget == null || masterVolumeControlWidget.isDisposed()) {
						return;
					}
					if(line == null) {
						masterVolumeControlWidget.setEnabled(false);
						return;
					}
					if(!skipVolumePolling) {
						final float newValue = (-volumeAndRange.getFirst() / volumeAndRange.getSecond()) * 255.0f + 255.0f;
						masterVolumeControlWidget.getDisplay().syncExec(new Runnable() {
							@Override
							public void run() {
								masterVolumeControlWidget.setSelection((int)newValue);
							}
						});
					}
					Thread.sleep(2 * 1000);
				}catch (InterruptedException e) {
					// Ignore
				}catch(LineUnavailableException e) {
					masterVolumeControlWidget.setEnabled(false);
					return;
				}
			}
		}
	}
	
	protected class MasterVolumeListener implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent event) {
			skipVolumePolling = true;
			int newVolume = masterVolumeControlWidget.getSelection();
			try {
				setMasterVolume(-newVolume / 255.0f + 1.0f);
			}catch(LineUnavailableException e) {
				Activator.getLogger().error("Could not set volume"); //$NON-NLS-1$
				masterVolumeControlWidget.setEnabled(false);
			}
			skipVolumePolling = false;
		}
	}
	
	protected class AgentStatisticListener implements IPhoneStateListener {
		private IPreferenceStore store;
		private boolean calling = false;
		
		private final static int CLEAR_TIME_AFTER_HOURS = 8;
		
		public final static String PREF_NAME_PREFIX = "AgentStatisticListener.";
		
		private final static String PREF_AGENT_LAST_CHANGE_TIMESTAMP = "AgentStatisticListener.LAST_CHANGE_AGENT";
		private final static String PREF_PHONE_LAST_CHANGE_TIMESTAMP = "AgentStatisticListener.LAST_CHANGE_PHONE";

		public final static String PREF_SUM_PAUSE = "AgentStatisticListener.SUM_PAUSE";
		public final static String PREF_SUM_PAUSE_LABEL = "Pause time";
		
		public final static String PREF_SUM_WRAPUP = "AgentStatisticListener.SUM_WRAPUP";
		public final static String PREF_SUM_WRAPUP_LABEL = "Wrapup time";
		
		public final static String PREF_SUM_CALLTIME = "AgentStatisticListener.SUM_CALLTIME";
		public final static String PREF_SUM_CALLTIME_LABEL = "Call time";
		
		public final static String PREF_CNT_PAUSE = "AgentStatisticListener.CNT_PAUSE";
		public final static String PREF_CNT_PAUSE_LABEL = "Pause";
		//private final static String PREF_CNT_WRAPUP = "AgentStatisticListener.";
		public final static String PREF_CNT_CALLS = "AgentStatisticListener.CNT_CALLS";
		public final static String PREF_CNT_CALLS_LABEL = "Calls";
		//private final static String PREF_ = "AgentStatisticListener.";
		
		public AgentStatisticListener(IPreferenceStore store) {
			this.store = store;
		}

		private void agentAdd(String prefSumName) {
			agentAdd(prefSumName, null);
		}
		private void agentAdd(String prefSumName, String prefCntName) {
			DateTime dt = getLastTimestampAgent();
			int seconds = Seconds.secondsBetween(dt, new DateTime()).getSeconds();
			
			AgentStatisticRowData row = null;
			if(prefSumName == PREF_SUM_PAUSE) {
				row = statisticMap.get(AgentStatisticValues.PREF_SUM_PAUSE);
			}
			if(prefSumName == PREF_SUM_WRAPUP) {
				row = statisticMap.get(AgentStatisticValues.PREF_SUM_WRAPUP);
			}

			if(row != null) {
				row.setValue(row.getValue() + seconds);
			}
			
			if(prefCntName == PREF_CNT_PAUSE) {
				row = statisticMap.get(AgentStatisticValues.PREF_CNT_PAUSE);
				row.setValue(row.getValue() + 1);
			}
			
			setNewTimestampAgent();
		}
		
		private void setNewTimestampAgent() {
			store.setValue(PREF_AGENT_LAST_CHANGE_TIMESTAMP, (new DateTime()).toDate().getTime());
		}
		
		private DateTime getLastTimestampAgent() {
			long ts = store.getLong(PREF_AGENT_LAST_CHANGE_TIMESTAMP);
			DateTime dt = new DateTime(ts);
			if(dt.plusHours(CLEAR_TIME_AFTER_HOURS).isBeforeNow()) {
				// more than n-Hours after last action, reset
				store.setValue(PREF_AGENT_LAST_CHANGE_TIMESTAMP, -1L);
				store.setValue(PREF_SUM_PAUSE,0);
				store.setValue(PREF_SUM_WRAPUP,0);
				store.setValue(PREF_CNT_PAUSE,0);
				dt = new DateTime();
			}
			return dt;
		}

		private void processStateChange(PhoneState oldState, PhoneState newState) {
			switch(newState) {
			case ACTIVE_CALL:
				// new call started
				if(oldState == PhoneState.OUTGOING_CALL || oldState == PhoneState.INCOMING_CALL) {
					setNewTimestampPhone();
					calling = true;
				}
				// call on hold, not a new call
				break;
			case IDLE:
				// stopped active call
				if(calling) {
					DateTime dt = getLastTimestampPhone();
					int callTime = Seconds.secondsBetween(dt, new DateTime()).getSeconds();
					AgentStatisticRowData row = statisticMap.get(AgentStatisticValues.PREF_SUM_CALLTIME);
					if(row != null) {
						row.setValue(row.getValue() + callTime);
					}
					row = statisticMap.get(AgentStatisticValues.PREF_CNT_CALLS);
					if(row != null) {
						row.setValue(row.getValue() + 1);
					}
					calling = false;
				}
				break;
			default:
				break;
			}
		}
		
		private DateTime getLastTimestampPhone() {
			long ts = store.getLong(PREF_PHONE_LAST_CHANGE_TIMESTAMP);
			DateTime dt = new DateTime(ts);
			if(dt.plusHours(CLEAR_TIME_AFTER_HOURS).isBeforeNow()) {
				// more than n-Hours after last action, reset
				store.setValue(PREF_SUM_CALLTIME, 0);
				store.setValue(PREF_CNT_CALLS, 0);
				dt = new DateTime();
			}
			return dt;
		}

		private void setNewTimestampPhone() {
			store.setValue(PREF_PHONE_LAST_CHANGE_TIMESTAMP, (new DateTime()).toDate().getTime());
		}
		
		@Override
		public void stateChanged(PhoneState oldState, PhoneState newState,
				PhoneStateTransition transitionReason) {
			processStateChange(oldState, newState);
		}
		
		@Override
		public void callerInformationChanged(CallInformation callInformation) {
		}
		
		@Override
		public void conferencingChanged(boolean newEnabled) {
			// Do nothing
		}
	}
	
	protected static class AgentStatisticLabelProvider extends ColumnLabelProvider {
		public static final int COLUMN_LABEL = 0;
		public static final int COLUMN_VALUE = 1;
		private int colNum;
		public AgentStatisticLabelProvider(Display display, int colNum) {
			this.colNum = colNum;
		}
		@Override
		public String getText(Object element) {
			String label = "";
			if(element instanceof AgentStatisticRowData) {
				AgentStatisticRowData rowData = (AgentStatisticRowData) element;
				label = rowData.get(colNum);
			}
			return label;
		}
		public static String getColumnText(Object obj, int column) {
			if(obj instanceof AgentStatisticRowData) {
				AgentStatisticRowData row = (AgentStatisticRowData) obj;
				return row.get(column);
			}
			return new String();
		}
	}
	
	/**
	 * Encapsulates data for a table row in the statistics view
	 * @author brandner
	 *
	 */
	protected static class AgentStatisticRowData {
		private String label, prefStoreName;
		private IPreferenceStore store;
		private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
		public static final String VALUE_PROPERTY = "value";
		public AgentStatisticRowData(IPreferenceStore store, String label, String prefStoreName) {
			this.store = store;
			this.label = label;
			this.prefStoreName = prefStoreName;
		}
		public String get(int colNum) {
			switch(colNum) {
			case AgentStatisticLabelProvider.COLUMN_LABEL: return getLabel();
			case AgentStatisticLabelProvider.COLUMN_VALUE: return getValueAsText();
			}
			return ""; 
		}
		public int getValue() {
			return store.getInt(prefStoreName);
		}
		public String getValueAsText() {
			int sum = getValue();
			if(prefStoreName == AgentStatisticListener.PREF_SUM_CALLTIME || prefStoreName == AgentStatisticListener.PREF_SUM_PAUSE || prefStoreName == AgentStatisticListener.PREF_SUM_WRAPUP) {
				// format as 1h 25m 45s or 01:25:45
				String format = "%02d:%02d:%02d";
				int h,m,s,rest;
				h = (int) Math.floor(sum / 3600);
				rest = (int) sum - (h * 3600);
				m = (int) Math.floor(rest / 60);
				s = (int) rest - (m * 60);
				return String.format(format, h,m,s);
			}
			return Integer.toString(sum);
		}
		public String getLabel() {
			return label;
		}
		public void setValue(int value) {
			int oldValue = getValue();
			store.setValue(prefStoreName, value);
			changeSupport.firePropertyChange(VALUE_PROPERTY, oldValue, value);
		}
		public void addPropertyListener(PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(listener);
		}
	}
	
	protected enum AgentStatisticValues {
		PREF_CNT_CALLS,
		PREF_SUM_CALLTIME,
		PREF_CNT_PAUSE,
		PREF_SUM_PAUSE,
		PREF_SUM_WRAPUP
	}
	
	protected class AgentStatisticContentProvider implements IStructuredContentProvider {
		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof Map) {
				Map<AgentStatisticValues, AgentStatisticRowData> map = (Map<AgentStatisticValues, AgentStatisticRowData>) inputElement;
				AgentStatisticRowData[] rows = new AgentStatisticRowData[map.size()];
				rows = map.values().toArray(rows);
				return rows;
			}
			return null;
		}
		
		public void setDefaultStoreData(IPreferenceStore store) {
			store.setDefault(AgentStatisticListener.PREF_CNT_CALLS, 0);
			store.setDefault(AgentStatisticListener.PREF_SUM_CALLTIME, 0);
			store.setDefault(AgentStatisticListener.PREF_CNT_PAUSE, 0);
			store.setDefault(AgentStatisticListener.PREF_SUM_PAUSE, 0);
			store.setDefault(AgentStatisticListener.PREF_SUM_WRAPUP, 0);
		}

		@Override
		public void dispose() {
			// do nothing
		}

		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if(oldInput == null && newInput instanceof Map) {
				Map<AgentStatisticValues, AgentStatisticRowData> map = (Map<AgentStatisticValues, AgentStatisticRowData>) newInput;
				for(AgentStatisticRowData row: map.values()) {
					row.addPropertyListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(final java.beans.PropertyChangeEvent evt) {
							final java.beans.PropertyChangeEvent event = evt;
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									tableViewer.refresh(event.getSource());
								}
							});
						}
					});
				}
			}
		}
	}
	
	public class AgentStatisticTableSorter extends TableViewerSorter {
		
		public AgentStatisticTableSorter(TableViewer viewer) {
			this.viewer = viewer;
		}

		private static final int ASCENDING = 0;
		private static final int DESCENDING = 1;
		private int column = 0;
		private int direction = 1;
		private TableViewer viewer;

		/**
		 * Does the sort. If it's a different column from the previous sort, do
		 * an ascending sort. If it's the same column as the last sort, toggle
		 * the sort direction.
		 * 
		 * @param column
		 */
		public void doSort(int column) {
			if (column == this.column) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.column = column;
				direction = ASCENDING;
			}
			// reset sort image
			for(TableColumn col : viewer.getTable().getColumns()) {
				col.setImage(null);
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				viewer.getTable().getColumn(column).setImage(IconStore.getDefault().getImageRegistry().get(IconStore.PHONE_HISTORY_SORT_UP));
			} else {
				viewer.getTable().getColumn(column).setImage(IconStore.getDefault().getImageRegistry().get(IconStore.PHONE_HISTORY_SORT_DOWN));
			}
		}

		/**
		 * Compares the object for sorting
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
			int rc = 0;
			if (e1 != null && e2 != null) {
				//AgentStatisticLabelProvider labelProvider = (AgentStatisticLabelProvider) this.viewer.getLabelProvider();
				// Determine which column and do the appropriate sort
				switch (column) {
				case AgentStatisticLabelProvider.COLUMN_LABEL:
					String columnText1 = AgentStatisticLabelProvider.getColumnText(e1,column);
					String columnText2 = AgentStatisticLabelProvider.getColumnText(e2,column);
					rc = columnText2.compareTo(columnText1);
					break;

				case AgentStatisticLabelProvider.COLUMN_VALUE:
					rc = ((CallHistoryEntry) e1).date.compareTo(((CallHistoryEntry) e2).date);
					break;
				}
				if (direction == DESCENDING) {
					rc = -rc;
				}
			}
			return rc;
		}
	}

	Composite parent;
	
	ApplyPreferencesTask applyPreferencesTask;
	DelayedRunnable<Boolean> applyPreferencesDelayedRunnable;
	LinkedList<PropertyChangeEvent> changeEvents = new LinkedList<PropertyChangeEvent>();
	
	SpecialRingTimer specialRingTimer;
	private PhoneStateListener stateListener;
	private AgentStatisticListener agentStatisticListener;
	private IPropertyChangeListener propChangeListener;
	private IPropertyChangeListener dialStringUpdateListener;

	public static final String ID = "org.acoveo.callcenter.sipclient.SoftPhoneViewPart"; //$NON-NLS-1$

	final ActionContributionItem callAnswerActionContrib = new ActionContributionItem(new CallAnswerAction());
	final ActionContributionItem hangupActionContrib = new ActionContributionItem(new HangupAction());
	final ActionContributionItem transferActionContrib = new ActionContributionItem(new TransferAction());
	final ActionContributionItem consultationHoldActionContrib = new ActionContributionItem(new ConsultationHoldAction());
	final ActionContributionItem pickupActionContrib = new ActionContributionItem(new PickupAction());
	final ActionContributionItem voicemailActionContrib = new ActionContributionItem(new VoicemailAction());
	final ActionContributionItem clearDestinationActionContrib = new ActionContributionItem(
			new ClearDestinationAction());
	final ActionContributionItem conferenceActionContrib = new ActionContributionItem(new ConferenceAction());
	final ActionContributionItem forwardCallActionContrib = new ActionContributionItem(new ForwardCallAction());

	final ActionContributionItem callHistoryActionContrib = new ActionContributionItem(new CallHistoryAction());
	final ActionContributionItem showTimeActionContrib = new ActionContributionItem(new ShowHideTimeAction());
	final ActionContributionItem filterInternalNrsActionContrib = new ActionContributionItem(new FilterInternalNrsAction());
	final ActionContributionItem clearHistoryActionContrib = new ActionContributionItem(new ClearHistoryAction());
	final ActionContributionItem settingsActionContrib = new ActionContributionItem(new SettingsAction());
	
	RunExternalCommandAction runCommandAction;
	ActionContributionItem runCommandActionContrib;
	Composite bottomButtonsComposite;
	
	boolean showTimeInsteadOfSymbol = false;

	int columnTypeWidth = 0;
	int columnNameWidth = 0;
	int columnNumberWidth = 0;
	int columnDateWidth = 0;
	
	Composite dialComposite = null;
	Composite callHistoryComposite = null;
	Composite timeComposite = null;
	Composite shortcutButtonComposite = null;
	Composite shortcutButtonCompositeFirstColumn = null;
	Composite shortcutButtonCompositeSecondColumn = null;
	List<ActionContributionItem> shortcutButtonActionsDialpad;
	List<Widget> shortcutButtonLabelsDialpad;
	Composite shortcutButtonsComposite = null;
	List<ActionContributionItem> shortcutButtonActionsTab;
	List<Widget> shortcutButtonLabelsTab;

	List<CallHistoryEntry> callHistoryMapInit = null;
	
	final static String CALL_FROM = Messages.SoftPhoneViewPart_9;
	final static String CALL_TO = Messages.SoftPhoneViewPart_10;
	final static String NO_ACTIVE_CALL = Messages.SoftPhoneViewPart_11;

	static final String NOT_REGISTERED_TOOLTIP = Messages.SoftPhoneViewPart_12;
	static final String REGISTER_FAILED_TOOLTIP = Messages.SoftPhoneViewPart_13;
	static final String REGISTER_ECF_FAILED_TOOLTIP = "Connection to the status server failed. Registration with the telephone system is active";
	static final String REGISTER_SUCCESS_TOOLTIP = Messages.SoftPhoneViewPart_14;

	static final int GRID_COLUMNS = 2;

//	final Sound ringSound;
	protected Label registeredLabel;
	protected Text agentIDLabel;
	private ScrollingLabel callerIdText;
	private ScrollingLabel statusLine;

	volatile Thread callerIdTextScrollingThread = null;

	public IHandler dialHandler;

	CallHistory callHistory;
	
	Line masterLine = null;
	MasterVolumeListener masterLineListener = new MasterVolumeListener();
	Scale masterVolumeControlWidget = null;
	boolean skipVolumePolling = false;
	
	SoftPhoneNotificationPopupDialog popupDialogue = null;
	
	// Statistics
	protected TableViewer tableViewer;
	HashMap<AgentStatisticValues, AgentStatisticRowData> statisticMap;
	
		
	public SoftPhoneViewPart() {
	}

	/** Checks a list of PropertyChangeEvents for changed Properties which require a restart
	 * 
	 * @param events A list of PropertyChangeEvents
	 * @param result If not null, it will be filled with the properties which required the restart
	 * @return Whether a restart of the application is required
	 */ 
	protected boolean applicationRestartRequired(Collection<PropertyChangeEvent> events, Collection<PropertyChangeEvent> result) {
		for(PropertyChangeEvent event : events) {
			if(SoftPhonePreferencePage.propertiesRequiringApplicationRestart.containsKey(event.getProperty())) {
				if(result != null) {
					result.add(event);
				}else {
					return true;
				}
			}
		}
		if(!result.isEmpty()) {
			return true;
		}
		return false;
	}

	protected void createTrayIcon(final Composite parent) {
		Tray tray = parent.getDisplay().getSystemTray();
		if (tray != null) {
			TrayItem trayIcon = new TrayItem(tray, SWT.NONE);
			trayIcon.setToolTipText(Messages.SoftPhoneViewPart_15);
			trayIcon.setImage(IconStore.getDefault().getImageRegistry().get(IconStore.PHONE_STATUS_REGISTER_SUCCESS));

			trayIcon.addListener(SWT.Show, new Listener() {
				public void handleEvent(Event event) {
					System.out.println("show"); //$NON-NLS-1$
				}
			});
			trayIcon.addListener(SWT.Hide, new Listener() {
				public void handleEvent(Event event) {
					System.out.println("hide"); //$NON-NLS-1$
				}
			});
			trayIcon.addListener(SWT.Selection, new Listener() {
				// one click on tray icon
				public void handleEvent(Event event) {
					System.out.println("click"); //$NON-NLS-1$
				}
			});
			trayIcon.addListener(SWT.DefaultSelection, new Listener() {
				// double click on tray icon
				public void handleEvent(Event event) {
					System.out.println("double click"); //$NON-NLS-1$
				}
			});

			final Menu menu = new Menu(parent);
			MenuItem mi = new MenuItem(menu, SWT.PUSH);
			mi.setText(Messages.SoftPhoneViewPart_2);
			mi.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					System.out.println("Exit Softphone"); //$NON-NLS-1$
				}
			});
			menu.setDefaultItem(mi);

			trayIcon.addListener(SWT.MenuDetect, new Listener() {
				public void handleEvent(Event event) {
					menu.setVisible(true);
				}
			});
		}
	}

	@Override
	public void createPartControl(final Composite parent) {
		this.parent = parent;
		
		// createTrayIcon(parent);

		GridLayoutFactory.swtDefaults().numColumns(GRID_COLUMNS).equalWidth(false).applyTo(parent);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(parent);

		// caller id text field
		callerIdText = new ScrollingLabel(parent, SWT.NONE);
		callerIdText.setText(NO_ACTIVE_CALL);
		callerIdText.setBackground(parent.getBackground());
		GridDataFactory.swtDefaults().span(GRID_COLUMNS, 1).applyTo(callerIdText.getLabel());
		callerIdText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				parent.getChildren()[1].setFocus();
			}

			@Override
			public void focusLost(FocusEvent e) {
				// Do Nothing and be happy
			}
		});

		clearDestinationActionContrib.fill(parent);

		final Text dialDestinationText = new Text(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(dialDestinationText);
		
		ICommandService cmdService = (ICommandService) getSite().getService(ICommandService.class);

		// TODO solve with context and not with handler from command
		Command cmd = cmdService.getCommand("org.acoveo.callcenter.sipclient.commandDial"); //$NON-NLS-1$
		dialHandler = cmd.getHandler();

		dialDestinationText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// deactivate dial commands key bindings
				ICommandService cmdService = (ICommandService) getSite().getService(ICommandService.class);
				Command cmd = cmdService.getCommand("org.acoveo.callcenter.sipclient.commandDial"); //$NON-NLS-1$
				cmd.setHandler(null);

				cmd = cmdService.getCommand("org.acoveo.callcenter.sipclient.commandBackspaceDestination"); //$NON-NLS-1$
				cmd.setHandler(null);

				cmd = cmdService.getCommand("org.acoveo.callcenter.sipclient.commandCallAnswer"); //$NON-NLS-1$
				cmd.setHandler(null);

			}

			@Override
			public void focusLost(FocusEvent e) {
				// activate dial commands key bindings
				ICommandService cmdService = (ICommandService) getSite().getService(ICommandService.class);
				Command cmd = cmdService.getCommand("org.acoveo.callcenter.sipclient.commandDial"); //$NON-NLS-1$
				cmd.setHandler(dialHandler);

				cmd = cmdService.getCommand("org.acoveo.callcenter.sipclient.commandBackspaceDestination"); //$NON-NLS-1$
				cmd.setHandler(dialHandler);

				cmd = cmdService.getCommand("org.acoveo.callcenter.sipclient.commandCallAnswer"); //$NON-NLS-1$
				cmd.setHandler(dialHandler);

				PjsuaClient.getDialDestination().replace(0, Integer.MAX_VALUE, ((Text) e.getSource()).getText());
			}

		});

		dialDestinationText.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
					parent.setFocus();
					PjsuaClient.getDialDestination().replace(0, Integer.MAX_VALUE, ((Text) e.getSource()).getText());
					new CallAction().run();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// Do nothing
			}
		});

		// Button list: answer/call, hangup, forward, ...
		Composite callHandlingComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(6).equalWidth(false).applyTo(callHandlingComposite);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.CENTER, SWT.TOP).span(GRID_COLUMNS, 1)
			.applyTo(callHandlingComposite);
		
		callAnswerActionContrib.fill(callHandlingComposite);
		hangupActionContrib.fill(callHandlingComposite);
		transferActionContrib.fill(callHandlingComposite);
		consultationHoldActionContrib.fill(callHandlingComposite);
		pickupActionContrib.fill(callHandlingComposite);
		conferenceActionContrib.fill(callHandlingComposite);
		
		// agentIDComp
		
		Composite agentIDComp = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).applyTo(agentIDComp);
		GridDataFactory.swtDefaults().span(GRID_COLUMNS, 1).grab(true, false).align(SWT.CENTER, SWT.CENTER).applyTo(agentIDComp);
		
		registeredLabel = new Label(agentIDComp, SWT.NONE);
		registeredLabel.setImage(IconStore.getDefault().getImageRegistry().get(IconStore.PHONE_STATUS_NOT_REGISTERED));
		registeredLabel.setToolTipText(NOT_REGISTERED_TOOLTIP);
		
		Text agentIDTextLabel = new Text(agentIDComp, SWT.NONE);
		agentIDTextLabel.setText(Messages.SoftPhoneViewPart_29);
		agentIDTextLabel.setBackground(parent.getBackground());
		agentIDTextLabel.setEditable(false);
		
		agentIDLabel = new Text(agentIDComp, SWT.NONE);
		agentIDLabel.setText(PjsuaClient.getSipUser());
		agentIDLabel.setBackground(parent.getBackground());
		agentIDLabel.setEditable(false);
		
		createAgentStateButtons(parent);
		
		// End agentIDComp
		
		// create tab folder
		CTabFolder folder = new CTabFolder(parent, SWT.TOP | SWT.BORDER);
		GridDataFactory.swtDefaults().span(GRID_COLUMNS, 1).grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(folder);
		
		// Create tab folder items
		createTabItemMain(folder);
		createTabItemHistory(folder);
		createTabItemShortcutButtons(folder);
		
		bottomButtonsComposite = new Composite(parent, parent.getStyle());
		bottomButtonsComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridDataFactory.swtDefaults().span(GRID_COLUMNS, 1).align(SWT.RIGHT, SWT.BOTTOM).applyTo(bottomButtonsComposite);
		
		// Create the runCommand button
		runCommandAction = new RunExternalCommandAction("", "", null);
		updateRunCommandAction();
		runCommandActionContrib = new ActionContributionItem(runCommandAction);
		runCommandActionContrib.fill(bottomButtonsComposite);

		settingsActionContrib.fill(bottomButtonsComposite);
	
		// status line
		statusLine = new ScrollingLabel(parent, SWT.SHADOW_IN);
		statusLine.setText(""); //$NON-NLS-1$
		statusLine.setBackground(parent.getBackground());
		GridDataFactory.swtDefaults().span(GRID_COLUMNS, 1).grab(true, false).align(SWT.FILL, SWT.BOTTOM).applyTo(statusLine.getLabel());
		statusLine.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				parent.setFocus();
			}

			@Override
			public void focusLost(FocusEvent e) {
				// Do Nothing and be happy
			}
		});

		dialStringUpdateListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				final String eventValue = (String) event.getNewValue();
				dialDestinationText.getDisplay().asyncExec(new Runnable() {
					public void run() {
						dialDestinationText.setText(eventValue);
					}
				});
			}
		};
		PjsuaClient.getDialDestination().addListener(dialStringUpdateListener);

		applyPreferencesTask = new ApplyPreferencesTask(parent);
		applyPreferencesDelayedRunnable = new DelayedRunnable<Boolean>(PREFERENCES_APPLY_DELAY_MS, applyPreferencesTask);
		propChangeListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				putPropertyChangeEvent(event);
			}
		};
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propChangeListener);
		
		int duration = Activator.getDefault().getPreferenceStore().getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_DURATION_MS);
		specialRingTimer = new SpecialRingTimer(parent.getDisplay(), duration);

		stateListener = new PhoneStateListener(parent);
		PjsuaClient.addPhoneStateListener(stateListener);
		
		agentStatisticListener = new AgentStatisticListener(Activator.getDefault().getPreferenceStore());
		createTabItemStatistics(folder);
		PjsuaClient.addPhoneStateListener(agentStatisticListener);
		
		// Register the display thread (this) as a lower priority thread for deadlock prevention
		PjsuaClient.pjsuaWorker.registerLowerPriorityThread(Thread.currentThread());

		PjsuaClient.pjsuaWorker.asyncExec(new Runnable() {
			@Override
			public void run() {
				PjsuaClient.setAudioDevices();
			}
		});
	}



	private void createTabItemMain(CTabFolder folder) {
		// Contains: Dial buttons, short cut buttons
		CTabItem tabItemMain = new CTabItem(folder, SWT.NONE);
		tabItemMain.setText(Messages.SoftPhoneViewPart_33);
		
		Composite tabItemMainComposite = new Composite(folder, SWT.NONE);
		tabItemMain.setControl(tabItemMainComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(tabItemMainComposite);
		GridDataFactory.swtDefaults().grab(true, true).applyTo(tabItemMainComposite);
		
		// First Column: dial buttons
		dialComposite = new Composite(tabItemMainComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(dialComposite);
		GridDataFactory.swtDefaults().grab(true, true)
		.align(SWT.CENTER, SWT.TOP).applyTo(dialComposite);
		fillDialComposite(dialComposite);
		
		// Second Column: shortcut buttons composite
		//createShortcutButtonsDialpad(tabItemMainComposite);
	}

	private void createTabItemHistory(CTabFolder folder) {
		CTabItem tabItemHistory = new CTabItem(folder, SWT.NONE);
		tabItemHistory.setText(Messages.SoftPhoneViewPart_35);
		Composite tabItemHistoryComposite = new Composite(folder, SWT.NONE);
		tabItemHistory.setControl(tabItemHistoryComposite);
		GridLayoutFactory.swtDefaults().numColumns(1).equalWidth(false).applyTo(tabItemHistoryComposite);
		
		timeComposite = new Composite(tabItemHistoryComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(true).applyTo(timeComposite);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(timeComposite);
		clearHistoryActionContrib.fill(timeComposite);
		showTimeActionContrib.fill(timeComposite);
		filterInternalNrsActionContrib.fill(timeComposite);
		
		callHistoryComposite = new Composite(tabItemHistoryComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(callHistoryComposite);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).span(GRID_COLUMNS, 1).applyTo(
				callHistoryComposite);

		callHistory = new CallHistory(callHistoryComposite, callHistoryMapInit, callerIdText, getInternalNumbers(), columnTypeWidth, columnNameWidth,
				columnNumberWidth, columnDateWidth);	
	}
	
	private CTabFolder shortcutButtonsTabFolder;
	private List<ShortcutButtonTabItem> shortcutButtonTabItemList;

	private void createTabItemShortcutButtons(CTabFolder folder) {
		CTabItem tabItem = new CTabItem(folder, SWT.NONE);
		tabItem.setText(Messages.SoftPhoneViewPart_3);
		
		shortcutButtonsComposite = new Composite(folder, SWT.NONE);
		tabItem.setControl(shortcutButtonsComposite);
		shortcutButtonsComposite.setLayout(new FillLayout());
		createShortcutButtons();
		folder.setSelection(tabItem);
	}

	private void createShortcutButtons() {
		List<ShortcutButtonGroup> list = ShortcutButtonGroupsPreferencePage.loadShortcutButtons(Activator.getDefault().getPreferenceStore());
		
		if(shortcutButtonsTabFolder != null) {
			for(ShortcutButtonTabItem item: shortcutButtonTabItemList) {
				item.dispose();
				item = null;
			}
			shortcutButtonsTabFolder.dispose();
			shortcutButtonsTabFolder = null;
			shortcutButtonTabItemList = null;
		}
		
		shortcutButtonsTabFolder = new CTabFolder(shortcutButtonsComposite, SWT.TOP | SWT.BORDER);
		shortcutButtonTabItemList = new ArrayList<ShortcutButtonTabItem>();
		
		for(ShortcutButtonGroup group: list) {
			// for each button group
			if(group.isActive()) {
				ShortcutButtonTabItem item = new ShortcutButtonTabItem(shortcutButtonsTabFolder, SWT.NONE, group);
				shortcutButtonTabItemList.add(item);
			}
		}
		
		shortcutButtonsTabFolder.setSelection(0);
		
		// redraw window
		shortcutButtonsComposite.layout();
		shortcutButtonsComposite.update();
	}
	
	private void updateShortcutButtons() {
		for(ShortcutButtonTabItem item: shortcutButtonTabItemList) {
			item.update();
		}
	}
	
	private void createShortcutButtonsDialpad(Composite parent) {
//		if(shortcutButtonComposite == null) {
//			if(parent == null) {
//				// throw exception?
//				return;
//			}
//			shortcutButtonComposite = new Composite(parent, SWT.NONE);
//			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(shortcutButtonComposite);
//			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(shortcutButtonComposite);
//		} else {
//			for(ActionContributionItem i: shortcutButtonActionsDialpad) {
//				i.getWidget().dispose();
//			}
//			for(Widget l: shortcutButtonLabelsDialpad) {
//				l.dispose();
//			}
//			if(shortcutButtonCompositeFirstColumn != null) {
//				shortcutButtonCompositeFirstColumn.dispose();
//			}
//			if(shortcutButtonCompositeSecondColumn != null) {
//				shortcutButtonCompositeSecondColumn.dispose();
//			}
//		}
//
//		shortcutButtonActionsDialpad = new ArrayList<ActionContributionItem>(
//				ShortcutButtonsPreferencePage.MAX_SHORTCUT_BUTTONS_DIALPAGE);
//		shortcutButtonLabelsDialpad = new ArrayList<Widget>(ShortcutButtonsPreferencePage.MAX_SHORTCUT_BUTTONS_DIALPAGE);
//		
//		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
//		for (int pos = 0; pos < ShortcutButtonsPreferencePage.MAX_SHORTCUT_BUTTONS
//				&& shortcutButtonActionsDialpad.size() < ShortcutButtonsPreferencePage.MAX_SHORTCUT_BUTTONS_DIALPAGE; pos++) {
//			if(ShortcutButtonsPreferencePage.isActive(store, pos)) {
//				ActionContributionItem scbAction = new ActionContributionItem(new ShortcutButtonAction(
//						ShortcutButtonsPreferencePage.getNumber(store, pos),
//						ShortcutButtonsPreferencePage.getCaption(store, pos),
//						ShortcutButtonsPreferencePage.isForwardCall(store, pos),
//						ShortcutButtonsPreferencePage.isSuffixDialing(store, pos)
//						));
//				shortcutButtonActionsDialpad.add(scbAction);
//			}
//		}
//		
//		Composite c1 = null, c2 = null;
//		int numButtonsDialPage = ShortcutButtonsPreferencePage.MAX_SHORTCUT_BUTTONS_DIALPAGE;
//		int maxButtonsPerCol = numButtonsDialPage / 2;
//		if(shortcutButtonActionsDialpad.size() > maxButtonsPerCol) {
//			c1 = new Composite(shortcutButtonComposite,SWT.NONE);
//			shortcutButtonCompositeFirstColumn = c1;
//			c2 = new Composite(shortcutButtonComposite,SWT.NONE);
//			shortcutButtonCompositeSecondColumn = c2;
//			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(c1);
//			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(c2);
//			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(c1);
//			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(c2);
//		} else {
//			c1 = shortcutButtonComposite;
//		}
//		for(int i=0;i<shortcutButtonActionsDialpad.size() && i < numButtonsDialPage;i++) {
//			Composite c = (i >= maxButtonsPerCol?c2:c1);
//			shortcutButtonActionsDialpad.get(i).fill(c);
//			Label label = new Label(c,SWT.NONE);
//			label.setText(((ShortcutButtonAction)shortcutButtonActionsDialpad.get(i).getAction()).getCaption());
//			shortcutButtonLabelsDialpad.add(label);
//		}
//		
//		shortcutButtonComposite.layout(true);
	}

	private void createTabItemStatistics(CTabFolder folder) {
		CTabItem tabItemStatistics = new CTabItem(folder,SWT.NONE);
		tabItemStatistics.setText("Statistics");
		
		Composite tableComposite = new Composite(folder, SWT.NONE);
		tabItemStatistics.setControl(tableComposite);
		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);
		
		tableViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		Table table = tableViewer.getTable();
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
		
		table.addListener(SWT.EraseItem, new TableSelectionColorChangeListener(table));
		
		TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE, 0);
		column.getColumn().setText("Caption");
		layout.setColumnData(column.getColumn(), new ColumnWeightData(10, 5));
		column.setLabelProvider(new AgentStatisticLabelProvider(null,AgentStatisticLabelProvider.COLUMN_LABEL));

		column = new TableViewerColumn(tableViewer, SWT.NONE, 1);
		column.getColumn().setText("Value");
		layout.setColumnData(column.getColumn(), new ColumnWeightData(10, 5));
		column.setLabelProvider(new AgentStatisticLabelProvider(null, AgentStatisticLabelProvider.COLUMN_VALUE));

		AgentStatisticContentProvider provider = new AgentStatisticContentProvider();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		provider.setDefaultStoreData(store);

		statisticMap = new HashMap<AgentStatisticValues, AgentStatisticRowData>();
		statisticMap.put(AgentStatisticValues.PREF_CNT_CALLS, new AgentStatisticRowData(store,
				AgentStatisticListener.PREF_CNT_CALLS_LABEL,
				AgentStatisticListener.PREF_CNT_CALLS));
		statisticMap.put(AgentStatisticValues.PREF_SUM_CALLTIME, new AgentStatisticRowData(store,
				AgentStatisticListener.PREF_SUM_CALLTIME_LABEL,
				AgentStatisticListener.PREF_SUM_CALLTIME));
		
		tableViewer.setContentProvider(provider);
		tableViewer.setInput(statisticMap);
		
		AgentStatisticTableSorter sorter = new AgentStatisticTableSorter(tableViewer);
		tableViewer.setSorter(sorter);

	}
	
	@Override
	public void setFocus() {
	}

	public Composite createButtons(Composite parent) {
		String labels[] = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(4, false));

		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).applyTo(c);
		int index = 0;
		for (String label : labels) {
			if(index == 3) {
				masterVolumeControlWidget = new Scale(c, SWT.VERTICAL);
				masterVolumeControlWidget.setMinimum(0);
				masterVolumeControlWidget.setMaximum(255);
				masterVolumeControlWidget.addSelectionListener(masterLineListener);
				GridDataFactory.swtDefaults().span(1, 4).align(SWT.CENTER, SWT.FILL).grab(false, true).applyTo(masterVolumeControlWidget);
				
				Thread pollThread = new Thread(new MasterVolumePollRunnable());
				pollThread.start();
			}
			ActionContributionItem contribItem = new ActionContributionItem(new DialAction(label));
			contribItem.fill(c);
			index++;
		}
		return c;
	}

	/** updates all actions
	 * Must be invoked from the GUI thread
	 */
	protected void updateAllActions() {
		((ISelfUpdatingAction) callAnswerActionContrib.getAction()).update();
		((ISelfUpdatingAction) hangupActionContrib.getAction()).update();
		((ISelfUpdatingAction) transferActionContrib.getAction()).update();
		((ISelfUpdatingAction) consultationHoldActionContrib.getAction()).update();
		((ISelfUpdatingAction) pickupActionContrib.getAction()).update();
		((ISelfUpdatingAction) voicemailActionContrib.getAction()).update();
		((ISelfUpdatingAction) conferenceActionContrib.getAction()).update();
		((ISelfUpdatingAction) settingsActionContrib.getAction()).update();
		if(ForwardCallAction.isAvailable(Activator.getDefault().getPreferenceStore())) {
			((ISelfUpdatingAction) forwardCallActionContrib.getAction()).update();
		}
		updateShortcutButtons();
	}
	
	/** Must be invoked from the GUI thread */
	protected void updateStatusIcon() {
		boolean sipConnected = PjsuaClient.getPhoneState() != PhoneState.UNREGISTERED;
		if(sipConnected) {
			registeredLabel.setImage(IconStore.getDefault().getImageRegistry().get(
					IconStore.PHONE_STATUS_REGISTER_SUCCESS));
			registeredLabel.setToolTipText(REGISTER_SUCCESS_TOOLTIP);
		}else {
			registeredLabel.setImage(IconStore.getDefault().getImageRegistry().get(
					IconStore.PHONE_STATUS_REGISTER_FAILED));
			registeredLabel.setToolTipText(REGISTER_FAILED_TOOLTIP);
		}
	}
	
	protected void updateCallerIdText() {
		PhoneState phoneState = PjsuaClient.getPhoneState();
		CallInformation callerInfo = phoneState.getCallerInformation();
		String text = NO_ACTIVE_CALL;
		if(callerInfo != null && callerInfo.getDisplayString() != null) {
			text = callerInfo.getDisplayString();
			switch(callerInfo.getCallDirection()) {
			case INBOUND:
				text = CALL_FROM + text;
				break;
			case OUTBOUND:
				text = CALL_TO + text;
				break;
			default:
				break;
			}
		}
		final String finalText = text;
		callerIdText.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				callerIdText.setText(finalText);
			}
		});
	}
	
	protected void activateShortcutButtonTabForSubject(CallInformation callInformation) {
		if(callInformation != null) {
			String subject = callInformation.getSubject();
			if(subject != null) {
				for(ShortcutButtonTabItem item: shortcutButtonTabItemList) {
					if(item.getGroup().getName().equals(subject)) {
						shortcutButtonsTabFolder.setSelection(item.getTabItem());
						break;
					}
				}
			}
		}
	}
	
	// Do not call this method on every state change as it is quite expensive
	protected void updateRunCommandAction() {
		if(runCommandAction == null) {
			return;
		}
		final String commandAndArgsString = Activator.getDefault().getPreferenceStore().getString(SoftPhonePreferencePage.PREF_RUN_COMMAND_STRING);
		final String commandAndArgsText = Activator.getDefault().getPreferenceStore().getString(SoftPhonePreferencePage.PREF_RUN_COMMAND_TEXT);
		final String commandAndArgsTooltip = Activator.getDefault().getPreferenceStore().getString(SoftPhonePreferencePage.PREF_RUN_COMMAND_TOOLTIP);
		final List<String> commandAndArgs = Filesystem.parseStringToCommandAndArgs(commandAndArgsString);
		
		// We need to run all that in the gui thread, otherwise we can't guarantee that the
		// text, tooltip and image updates have made it to the button before calculating
		// the new size
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if(commandAndArgsText != null && !commandAndArgsText.isEmpty()) {
					runCommandAction.setImageDescriptor(null);
					runCommandAction.setText(commandAndArgsText);
				}else {
					runCommandAction.setText(null);
					runCommandAction.setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_RUN_COMMAND));
				}
				runCommandAction.setToolTipText(commandAndArgsTooltip);
				runCommandAction.setCommandAndArgs(commandAndArgs);
				
				if(runCommandActionContrib != null) {
					final Button button = (Button)runCommandActionContrib.getWidget();
					if(button != null && !button.isDisposed() && bottomButtonsComposite != null) {
						button.setSize(button.computeSize(SWT.DEFAULT, SWT.DEFAULT));
						bottomButtonsComposite.setSize(bottomButtonsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
						parent.layout();
					}
				}
			}
		};
		if(runCommandActionContrib != null) {
			Button button = (Button)runCommandActionContrib.getWidget();
			if(button != null && !button.isDisposed()) {
				button.getDisplay().syncExec(runnable);
				return;
			}
		}
		runnable.run();
	}
	
	public void toggleCallHistory() {
		if (callHistoryComposite.isVisible()) {
			callHistoryComposite.setVisible(false);
			((GridData) callHistoryComposite.getLayoutData()).exclude = true;

			timeComposite.setVisible(false);
			((GridData) timeComposite.getLayoutData()).exclude = true;

			dialComposite.setVisible(true);
			((GridData) dialComposite.getLayoutData()).exclude = false;
			dialComposite.layout();
			dialComposite.getParent().layout();
		} else {
			dialComposite.setVisible(false);
			((GridData) dialComposite.getLayoutData()).exclude = true;

			timeComposite.setVisible(true);
			((GridData) timeComposite.getLayoutData()).exclude = false;
			timeComposite.layout();
			timeComposite.getParent().layout();

			callHistoryComposite.setVisible(true);
			((GridData) callHistoryComposite.getLayoutData()).exclude = false;
			callHistoryComposite.layout();
			callHistoryComposite.getParent().layout();
		}
	}

	public void toggleTime() {
		callHistory.toggleTime();
	}
	
	public void clearHistory() {
		callHistory.clearHistory();
	}
	
	
	public void toggleFilter() {
		callHistory.toggleFilter();
	}
	
	/** Set the master volume to the given volume (between 0, 1)
	 * 
	 * @param volume The volume to set (0 = mute, 1 = full volume)
	 */
	private void setMasterVolume(float volume) throws LineUnavailableException {
		if(volume < 0.0 || volume > 1.0) {
			throw new RuntimeException(Messages.SoftPhoneViewPart_48);
		}
		String mixerControlName = "Master"; //$NON-NLS-1$
		if (System.getProperty("os.name", "").contains("Windows")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			mixerControlName = "Volume"; //$NON-NLS-1$
		}
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info mixerInfo:mixerInfos) {
			Mixer m = AudioSystem.getMixer(mixerInfo);
			Line.Info[] lineInfos = m.getTargetLineInfo();
			for (Line.Info li:lineInfos) {
				if (li instanceof Port.Info){
					Port.Info pi=(Port.Info)li;
					Line portLine=m.getLine(pi);
					portLine.open();
					Control[] pCtrls=portLine.getControls();
					for(Control memberControl : pCtrls){
						if(memberControl.getType().toString().equals(mixerControlName)) {
							FloatControl masterVolumeControl = findFirstFloatControl(memberControl);
							float range = masterVolumeControl.getMaximum() - masterVolumeControl.getMinimum();
							float newValue = volume * range;
							if(newValue < masterVolumeControl.getPrecision()) {
								newValue = masterVolumeControl.getPrecision();
							}
							masterVolumeControl.setValue(newValue);
							BooleanControl masterUnmutedControl = findFirstBoolControl(memberControl);
							masterUnmutedControl.setValue(true);
						}
					}
					portLine.close();
				}
			}
		}
	}
	private Line getCurrentMasterVolumeAndRange(Tuple<Float, Float> volumeAndRange) throws LineUnavailableException {
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info mixerInfo:mixerInfos) {
			Mixer m = AudioSystem.getMixer(mixerInfo);
			Line.Info[] lineInfos = m.getTargetLineInfo();
			for (Line.Info li:lineInfos) {
				if (li instanceof Port.Info){
					Port.Info pi=(Port.Info)li;
					Line portLine=m.getLine(pi);
					portLine.open();
					Control[] pCtrls=portLine.getControls();
					for(Control memberControl : pCtrls){
						if(memberControl.getType().toString().equals("Master")) { //$NON-NLS-1$
							FloatControl masterVolumeControl = findFirstFloatControl(memberControl);
							volumeAndRange.setFirst(masterVolumeControl.getValue());
							volumeAndRange.setSecond(masterVolumeControl.getMaximum() - masterVolumeControl.getMinimum());
							return portLine;
						}
					}
					portLine.close();
				}
			}
		}
		return null;
	}
	
	private FloatControl findFirstFloatControl(Control rootControl) {
		if (rootControl instanceof FloatControl) {
			return (FloatControl)rootControl;
		}else if(rootControl instanceof CompoundControl) {
			CompoundControl cc = (CompoundControl) rootControl;
			for (Control compControl : cc.getMemberControls()) {
				FloatControl child = findFirstFloatControl(compControl);
				if(child != null && child instanceof FloatControl) {
					return child;
				}
			}
		}
		return null;
	}
	
	private BooleanControl findFirstBoolControl(Control rootControl) {
		if (rootControl instanceof FloatControl) {
			return (BooleanControl)rootControl;
		}else if(rootControl instanceof CompoundControl) {
			CompoundControl cc = (CompoundControl) rootControl;
			for (Control compControl : cc.getMemberControls()) {
				BooleanControl child = findFirstBoolControl(compControl);
				if(child != null && child instanceof BooleanControl) {
					return child;
				}
			}
		}
		return null;
	}
	private void fillDialComposite(Composite composite) {

		// dial buttons
		createButtons(composite);
	}
	
	private void createAgentStateButtons(Composite composite) {

		// shortcut buttons
		int colCount = 4;
		Composite shortcutButtons = new Composite(composite, SWT.NONE);
		shortcutButtons.setLayout(new GridLayout(colCount, false));
		GridDataFactory.swtDefaults().span(GRID_COLUMNS, 1)
			.align(SWT.CENTER, SWT.TOP).applyTo(shortcutButtons);

		voicemailActionContrib.fill(shortcutButtons);
		
		if(ForwardCallAction.isAvailable(Activator.getDefault().getPreferenceStore())) {
			((ForwardCallAction)forwardCallActionContrib.getAction()).setStore(Activator.getDefault().getPreferenceStore());
			forwardCallActionContrib.fill(shortcutButtons);
		}
	}
	
	@Override
	public void dispose() {
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(propChangeListener);
		PjsuaClient.getDialDestination().removeListener(dialStringUpdateListener);
		super.dispose();
	}

	public void saveState(IMemento memento) {
		// save state here

		// saving table columns width
		memento.putInteger("ColWidth" + new Integer(CallHistory.COLUMN_TYPE).toString(), callHistory.getCallHistoryTableViewer().getTable().getColumn(CallHistory.COLUMN_TYPE).getWidth()); //$NON-NLS-1$
		memento.putInteger("ColWidth" + new Integer(CallHistory.COLUMN_NAME).toString(), callHistory.getCallHistoryTableViewer().getTable().getColumn(CallHistory.COLUMN_NAME).getWidth()); //$NON-NLS-1$
		memento.putInteger("ColWidth" + new Integer(CallHistory.COLUMN_NUMBER).toString(), callHistory.getCallHistoryTableViewer().getTable().getColumn(CallHistory.COLUMN_NUMBER).getWidth()); //$NON-NLS-1$
		memento.putInteger("ColWidth" + new Integer(CallHistory.COLUMN_DATE).toString(), callHistory.getCallHistoryTableViewer().getTable().getColumn(CallHistory.COLUMN_DATE).getWidth()); //$NON-NLS-1$

		
		// saving call history
		List<CallHistoryEntry> historyEntries = callHistory.getCallHistoryMap();
		
		int historyDays = Activator.getDefault().getPreferenceStore().getInt(SoftPhonePreferencePage.PREF_HISTORY_DAYS);
		
		SortedMap<Date, CallHistoryEntry> sortedMap = new TreeMap<Date, CallHistoryEntry>();
			
		for (CallHistoryEntry entry : historyEntries) {
			sortedMap.put(entry.date, entry);
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -historyDays);
		
		for (CallHistoryEntry entry : sortedMap.tailMap(calendar.getTime()).values()) {
			IMemento entryMemento = memento.createChild("CallHistoryEntry"); //$NON-NLS-1$
			entryMemento.putString("name", entry.name); //$NON-NLS-1$
			entryMemento.putString("number", entry.number); //$NON-NLS-1$
			entryMemento.putString("date", new Long(entry.date.getTime()).toString()); //$NON-NLS-1$
			entryMemento.putString("type", entry.type.toString()); //$NON-NLS-1$
		}
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		// restore from state here
		if (memento == null) {
			return;
		}
		try {
			
			List<CallHistoryEntry> callHistoryMap = new ArrayList<CallHistoryEntry>();

			for (IMemento memEntry : memento.getChildren("CallHistoryEntry")) { //$NON-NLS-1$
				String name = memEntry.getString("name"); //$NON-NLS-1$
				String number = memEntry.getString("number"); //$NON-NLS-1$
				Date date = new Date(new Long(memEntry.getString("date"))); //$NON-NLS-1$
				CallHistoryEntry.Type type = CallHistoryEntry.Type.valueOf(memEntry.getString("type")); //$NON-NLS-1$

				CallHistoryEntry entry = new CallHistoryEntry(date, type, name, number);
				callHistoryMap.add(entry);
				
				columnTypeWidth = memento.getInteger("ColWidth" + new Integer(CallHistory.COLUMN_TYPE).toString()); //$NON-NLS-1$
				columnNameWidth = memento.getInteger("ColWidth" + new Integer(CallHistory.COLUMN_NAME).toString()); //$NON-NLS-1$
				columnNumberWidth = memento.getInteger("ColWidth" + new Integer(CallHistory.COLUMN_NUMBER).toString()); //$NON-NLS-1$
				columnDateWidth = memento.getInteger("ColWidth" + new Integer(CallHistory.COLUMN_DATE).toString()); //$NON-NLS-1$
			}
			
			callHistoryMapInit = callHistoryMap;
			
		} catch (Exception e) {
		}

	}
	
	protected List<String> getInternalNumbers() {
		List<String> list = new ArrayList<String>();
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		list.add(store.getString(AccountPreferencePage.PREF_VOICEMAIL_EXTENSION));
		list.addAll(PjsuaClient.getInternalNumbers());
		return list;
	}
}
