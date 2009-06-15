package org.acoveo.callcenter.sipclient.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.acoveo.callcenter.guiLibrary.TableSelectionColorChangeListener;
import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.Activator;
import org.acoveo.callcenter.sipclient.CallInformation;
import org.acoveo.callcenter.sipclient.IPhoneStateListener;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.acoveo.callcenter.sipclient.PjsuaClient.PhoneStateTransition;
import org.acoveo.callcenter.sipclient.actions.CallAnswerAction;
import org.acoveo.callcenter.sipclient.view.CallHistory.CallHistoryEntry.Type;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

public class CallHistory {
	
	// the int values are the order for the columns
	public static final int COLUMN_TYPE = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_NUMBER = 2;
	public static final int COLUMN_DATE = 3;

	int columnTypeWidth = 0;
	int columnNameWidth = 0;
	int columnNumberWidth = 0;
	int columnDateWidth = 0;
	
	boolean showTimeInsteadOfSymbol = false;
	boolean filterInternalNumbers = true;
	
	List<String> internalNumbers = null;
	
	ScrollingLabel callerIdText;
	
	List<CallHistoryEntry> callHistoryMap = null;
	protected TableViewer callHistoryTableViewer;
	private ArrayList<ColumnHeaderObject> columnHeaderObjects;
	
	public CallHistory(Composite parent, List<CallHistoryEntry> callHistoryMap, ScrollingLabel callerIdText, List<String> internalNumbers, int columnTypeWidth, int columnNameWidth,
			int columnNumberWidth, int columnDateWidth) {
		columnHeaderObjects = new ArrayList<ColumnHeaderObject>();

		ColumnHeaderObject colHeadObj = new ColumnHeaderObject(COLUMN_TYPE);
		colHeadObj.columnWeight = 1;
		colHeadObj.title = ""; //$NON-NLS-1$
		colHeadObj.columnStyle = SWT.CENTER;
		columnHeaderObjects.add(colHeadObj);

		colHeadObj = new ColumnHeaderObject(COLUMN_NAME);
		colHeadObj.columnWeight = 10;
		colHeadObj.title = Messages.CallHistory_1;
		colHeadObj.columnStyle = SWT.LEFT;
		columnHeaderObjects.add(colHeadObj);

		colHeadObj = new ColumnHeaderObject(COLUMN_NUMBER);
		colHeadObj.columnWeight = 10;
		colHeadObj.title = Messages.CallHistory_2;
		colHeadObj.columnStyle = SWT.LEFT;
		columnHeaderObjects.add(colHeadObj);

		colHeadObj = new ColumnHeaderObject(COLUMN_DATE);
		colHeadObj.columnWeight = 1;
		colHeadObj.title = ""; //$NON-NLS-1$
		colHeadObj.columnStyle = SWT.LEFT;
		columnHeaderObjects.add(colHeadObj);
		
		this.callHistoryMap = callHistoryMap;
		this.columnTypeWidth = columnTypeWidth;
		this.columnNameWidth = columnNameWidth;
		this.columnNumberWidth = columnNumberWidth;
		this.columnDateWidth = columnDateWidth;
		this.callerIdText = callerIdText;
		this.internalNumbers = internalNumbers;
		
		init(parent);
	}
	
	public void init(Composite composite) {
		Composite tableComposite = new Composite(composite, SWT.NONE);
		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(tableComposite);

		if (callHistoryMap == null) {
			callHistoryMap = new ArrayList<CallHistoryEntry>();
		}

		callHistoryTableViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		Table table = callHistoryTableViewer.getTable();
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
		
		table.addListener(SWT.EraseItem, new TableSelectionColorChangeListener(table));
		
		CallHistory.CallHistoryTableSorter sorter = new CallHistory.CallHistoryTableSorter(callHistoryTableViewer);
		callHistoryTableViewer.setSorter(sorter);
		Collections.sort(columnHeaderObjects);
		for (ColumnHeaderObject colHeaderObj : columnHeaderObjects)
			ColumnHeaderObject.createTableColumn(colHeaderObj, callHistoryTableViewer, layout);

		if(columnTypeWidth > 0 && columnNameWidth > 0 && columnNumberWidth > 0 && columnDateWidth > 0) {
			callHistoryTableViewer.getTable().getColumn(COLUMN_TYPE).setWidth(columnTypeWidth);
			callHistoryTableViewer.getTable().getColumn(COLUMN_NAME).setWidth(columnNameWidth);
			callHistoryTableViewer.getTable().getColumn(COLUMN_NUMBER).setWidth(columnNumberWidth);
			callHistoryTableViewer.getTable().getColumn(COLUMN_DATE).setWidth(columnDateWidth);
		}
		
		CallHistory.CallHistoryContentProvider contentProvider = new CallHistory.CallHistoryContentProvider(tableComposite);
		callHistoryTableViewer.setContentProvider(contentProvider);
		callHistoryTableViewer.setLabelProvider(new CallHistory.CallHistoryLabelProvider());
		callHistoryTableViewer.setInput(callHistoryMap);

		callHistoryTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				PjsuaClient.getDialDestination().replace(0, Integer.MAX_VALUE,
						((CallHistoryEntry) ((StructuredSelection) event.getSelection()).getFirstElement()).number);
				new CallAnswerAction().run();
			}
		});

		sorter.doSort(COLUMN_DATE);
		sorter.doSort(COLUMN_DATE);
		callHistoryTableViewer.refresh();

		PjsuaClient.addPhoneStateListener(contentProvider);

	}
	
	public static class CallHistoryEntry {
		public enum Type {
			IN_MISSED, IN_ANSWERED, OUT_ANSWERED, OUT_REJECTED, OUT_DIALED
		}

		public Date date;
		public Type type;
		public String name, number;

		public CallHistoryEntry(Date date, Type type, String name, String number) {
			this.date = date;
			this.type = type;
			this.name = name;
			this.number = number;
		}
	}
	
	public class CallHistoryContentProvider implements IStructuredContentProvider, IPhoneStateListener {
		final Composite parent;

		Map<Integer, CallHistoryEntry> pendingHistoryEntries = new TreeMap<Integer, CallHistoryEntry>();
		
		public CallHistoryContentProvider(final Composite parent) {
			this.parent = parent;
		}
		
		/** Flush all pending entries which do not match the given call id */
		protected void flushNonMatchingPendingEntries(int callId) {
			Iterator<Entry<Integer, CallHistoryEntry>> iter = pendingHistoryEntries.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<Integer, CallHistoryEntry> entry = iter.next();
				if(!entry.getKey().equals(callId)) {
					callHistoryMap.add(entry.getValue());
					addToViewerFiltered(entry.getValue());
					iter.remove();
				}
			}
		}
		@Override
		public void stateChanged(PjsuaClient.PhoneState oldState, PjsuaClient.PhoneState newState, PjsuaClient.PhoneStateTransition transitionReason) {
			// We have to update the callerid text and store data for the history
			switch(newState) {
			case IDLE: {
				if(!pendingHistoryEntries.isEmpty()) {
					// There are no active calls, put everything into the history
					callHistoryMap.addAll(pendingHistoryEntries.values());
					
					// Add history items to the viewer if the are not filtered
					addToViewerFiltered(pendingHistoryEntries.values());
					pendingHistoryEntries.clear();
				}
				break;
			}
			case INCOMING_CALL: {
				int callId = newState.getRingingCallId();
				if(callId >= 0) {
					CallInformation callInfo = newState.getCallerInformation();
					
					CallHistoryEntry entry = new CallHistoryEntry(new Date(), Type.IN_MISSED, callInfo.getName(), callInfo.getNumber());
					Activator.getLogger().debug("adding pending entry for callid " + callId + " " + entry.type.name() + " " + entry.name + " " + entry.number); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					CallHistoryEntry oldEntry = pendingHistoryEntries.put(callId, entry);
					if(oldEntry != null) {
						Activator.getLogger().debug("replaced pending entry " + oldEntry.type.name() + " " + oldEntry.name + " " + oldEntry.number); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
				break;
			}
			case OUTGOING_CALL: {
				int callId = newState.getRingingCallId();
				if(callId >= 0) {
					CallInformation callInfo = newState.getCallerInformation();
					
					CallHistoryEntry entry = new CallHistoryEntry(new Date(), Type.OUT_DIALED, callInfo.getName(), callInfo.getNumber());
					Activator.getLogger().debug("adding pending entry for callid " + callId + " " + entry.type.name() + " " + entry.name + " " + entry.number); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					CallHistoryEntry oldEntry = pendingHistoryEntries.put(callId, entry);
					if(oldEntry != null) {
						Activator.getLogger().debug("replaced pending entry " + oldEntry.type.name() + " " + oldEntry.name + " " + oldEntry.number); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
				break;
			}
			case CALL_ON_HOLD: {
				int callId = newState.getOnHoldCallId();
				if(callId >= 0) {
					// An outgoing call may have been completed/ cancelled
					// Add all pending entries which do not match the held call to the history
					flushNonMatchingPendingEntries(callId);
				}
				break;
			}
			case ACTIVE_CALL:
			case ACTIVE_CALL_AND_ON_HOLD: {
				int callId = newState.getActiveCallId();
				if(callId >= 0) {
					CallHistoryEntry oldEntry = pendingHistoryEntries.get(callId);
					CallHistoryEntry entry;
					if(oldEntry != null) {
						entry = new CallHistoryEntry(new Date(), oldEntry.type, oldEntry.name, oldEntry.number);
					}else {
						entry = new CallHistoryEntry(new Date(), null, null, null);
					}
					if(transitionReason == PhoneStateTransition.CALL_ANSWERED) {
						// The user answered an incoming call
						entry.type = Type.IN_ANSWERED;
					}else if(transitionReason == PhoneStateTransition.CALL_CONNECTED) {
						// It was an outgoing call and the other party picked up
						entry.type = Type.OUT_ANSWERED;
					}else if(transitionReason == PhoneStateTransition.CALL_UNHOLD) {
						// We simply retrieved a call, no new pending entry
						break;
					}else {
						// No new call established, do not add record to pending entries
						// We had two calls and one of them was dropped
						// Add all pending entries which do not match the active call to the history
						flushNonMatchingPendingEntries(callId);
						break;
					}
					
					// Adding (replacing old entry with equal callid) new pending entry
					Activator.getLogger().debug("adding pending entry for callid " + callId + " " + entry.type.name() + " " + entry.name + " " + entry.number); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					pendingHistoryEntries.put(callId, entry);
					if(oldEntry != null) {
						Activator.getLogger().debug("replaced pending entry " + oldEntry.type.name() + " " + oldEntry.name + " " + oldEntry.number); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
				break;
			}
			}
		}
		
		@Override
		public Object[] getElements(Object inputElement) {
			
			if (callHistoryMap != null && callHistoryMap.size() > 0) {
				List<CallHistoryEntry> tempMap = new ArrayList<CallHistoryEntry>();
				//XXX We should use the filtering framework of the TableViewer
				if(filterInternalNumbers) {
					for(CallHistoryEntry entry : callHistoryMap) {
						if(!internalNumbers.contains(entry.number)) {
							tempMap.add(entry);
						}
					}
					return tempMap.toArray();
				}
				
				return callHistoryMap.toArray();
				
			}

			return new Object[0];
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}
		
		protected void addToViewerFiltered(Collection<CallHistoryEntry> entries) {
			for(CallHistoryEntry entry : entries) {
				addToViewerFiltered(entry);
			}
		}
		
		protected void addToViewerFiltered(CallHistoryEntry entry) {
			if((!filterInternalNumbers && internalNumbers.contains(entry.number)) ||
					!internalNumbers.contains(entry.number)) {
				final CallHistoryEntry finalEntry = entry;
				parent.getDisplay().syncExec(new Runnable() {
					public void run() {
						callHistoryTableViewer.add(finalEntry);
					}
				});
			}
		}
		
		@Override
		public void callerInformationChanged(CallInformation callInformation) {
			// Do nothing
		}
		@Override
		public void conferencingChanged(boolean newEnabled) {
			// Do nothing
		}
	}

	public class CallHistoryLabelProvider implements ITableLabelProvider {

		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy"); //$NON-NLS-1$
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if(element == null) {
				return null;
			}
			CallHistoryEntry entry = (CallHistoryEntry) element;

			switch (columnIndex) {
			case COLUMN_TYPE:
				Image id = null;
				
				if (entry.type.equals(CallHistoryEntry.Type.IN_ANSWERED)) {
					id = IconStore.getDefault().getImageRegistry().get(IconStore.PHONE_CALL_INCOMING);
				} else if (entry.type.equals(CallHistoryEntry.Type.IN_MISSED)) {
					id = IconStore.getDefault().getImageRegistry().get(IconStore.PHONE_CALL_MISSED);
				} else if (entry.type.equals(CallHistoryEntry.Type.OUT_ANSWERED)) {
					id = IconStore.getDefault().getImageRegistry().get(IconStore.PHONE_CALL_OUTGOING);
				} else if (entry.type.equals(CallHistoryEntry.Type.OUT_DIALED)) {
					id = IconStore.getDefault().getImageRegistry().get(IconStore.PHONE_CALL_OUTGOING);
				} else if (entry.type.equals(CallHistoryEntry.Type.OUT_REJECTED)) {
					id = IconStore.getDefault().getImageRegistry().get(IconStore.PHONE_CALL_OUTGOING);
				}
				return id;
			case COLUMN_DATE:
				if (!showTimeInsteadOfSymbol) {
					return IconStore.getDefault().getImageRegistry().get(
							IconStore.PHONE_HISTORY_TIME);
				}
				break;
			}

			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			String returnValue = null;
			CallHistoryEntry entry = (CallHistoryEntry) element;

			switch (columnIndex) {
			case COLUMN_TYPE:
				break;

			case COLUMN_DATE:
				if (showTimeInsteadOfSymbol) {
					Calendar dateDay = Calendar.getInstance();
					dateDay.setTime(entry.date);

					Calendar today = Calendar.getInstance();

					// if date is today, only time will be displayed
					if (dateDay.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
							&& dateDay.get(Calendar.MONTH) == today.get(Calendar.MONTH)
							&& dateDay.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
						returnValue = timeFormat.format(entry.date);
					} else {
						returnValue = dateFormat.format(entry.date);
					}
				}
				break;

			case COLUMN_NAME:
				returnValue = entry.name;
				break;

			case COLUMN_NUMBER:
				returnValue = entry.number;
				break;

			default:
				returnValue = null;
			}
			if (returnValue == null)
				return ""; //$NON-NLS-1$
			return returnValue;

		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

	}

	public class CallHistoryTableSorter extends TableViewerSorter {
		
		public CallHistoryTableSorter(TableViewer viewer) {
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
				viewer.getTable().getColumn(column).setImage(new Image(Display.getCurrent(), 
						IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_HISTORY_SORT_UP).getImageData()));
			} else {
				viewer.getTable().getColumn(column).setImage(new Image(Display.getCurrent(), 
						IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.PHONE_HISTORY_SORT_DOWN).getImageData()));
			}
		}

		/**
		 * Compares the object for sorting
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
			int rc = 0;
			if (e1 != null && e2 != null) {
				CallHistoryLabelProvider labelProvider = (CallHistoryLabelProvider) this.viewer.getLabelProvider();
				// Determine which column and do the appropriate sort
				switch (column) {
				case COLUMN_TYPE:
					rc = ((CallHistoryEntry) e1).type.ordinal() - ((CallHistoryEntry) e2).type.ordinal();
					break;

				case COLUMN_DATE:
					rc = ((CallHistoryEntry) e1).date.compareTo(((CallHistoryEntry) e2).date);
					break;

				case COLUMN_NAME:
				case COLUMN_NUMBER:
					String columnText1 = labelProvider.getColumnText(e1,
							column);
					String columnText2 = labelProvider.getColumnText(e2,
							column);
					rc = columnText2.compareTo(columnText1);
					break;
				}
				if (direction == DESCENDING) {
					rc = -rc;
				}
			}
			return rc;
		}
	}

	public TableViewer getCallHistoryTableViewer() {
		return callHistoryTableViewer;
	}

	public List<CallHistoryEntry> getCallHistoryMap() {
		return callHistoryMap;
	}

	public void setCallHistoryMap(List<CallHistoryEntry> callHistoryMap) {
		this.callHistoryMap = callHistoryMap;
	}

	public void toggleTime() {
		showTimeInsteadOfSymbol = !showTimeInsteadOfSymbol;
		callHistoryTableViewer.refresh();
	}
	
	public void toggleFilter() {
		filterInternalNumbers = !filterInternalNumbers;
		callHistoryTableViewer.refresh();
	}
	
	public void clearHistory() {
		if(callHistoryMap.size() > 0) {
			String title = Messages.CallHistory_28;
			String message = Messages.CallHistory_29;
	        MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, null, message, MessageDialog.QUESTION, new String[] { "Yes", "No"}, 0);
	        int returnCode = dialog.open();
	
	        if(returnCode == MessageDialog.OK) {
				callHistoryMap.clear();
				callHistoryTableViewer.refresh();
	        }
		}
	}
}
