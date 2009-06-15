package org.acoveo.callcenter.sipclient.actions;

import org.acoveo.callcenter.guiLibrary.TableSelectionColorChangeListener;
import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.sipclient.PjsuaClient;
import org.acoveo.callcenter.sipclient.preferences.forwardcall.ForwardCallData;
import org.acoveo.callcenter.sipclient.preferences.forwardcall.ForwardCallData.ForwardCallEntry;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class ForwardCallAction extends Action implements ISelfUpdatingAction {
	/*
	 * PjsuaClient . connect to sip server
	 * => setzen des zuletzt geladenen Forward status
	 */
	public enum ForwardCallActionTableColumns {
		COLUMN_NUMBER,
		COLUMN_FORWARDTYPE,
		COLUMN_RINGCOUNT;
		public static ForwardCallActionTableColumns fromOrdinal(int ordinal) {
			if (ordinal < 0 || ordinal >= ForwardCallActionTableColumns.values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal"); //$NON-NLS-1$
			}
			return ForwardCallActionTableColumns.values()[ordinal];
		}
		public String getLabelText() {
			switch(this) {
			case COLUMN_NUMBER:
				return "Number";
			case COLUMN_FORWARDTYPE:
				return "Forward";
			case COLUMN_RINGCOUNT:
				return "Ringcount";
			}
			
			return "";
		}
	}
	
	public static class ForwardCallActionLabelProvider extends ColumnLabelProvider {
		private ForwardCallActionTableColumns columnEnum;

		public ForwardCallActionLabelProvider(ForwardCallActionTableColumns columnEnum) {
			this.columnEnum = columnEnum;
		}
		
		@Override
		public String getText(Object element) {
			if(element instanceof ForwardCallEntry) {
				ForwardCallEntry forwardCallEntry = (ForwardCallEntry) element;
				return getText(forwardCallEntry, columnEnum);
			}
			return null;
		}

		public static String getText(ForwardCallEntry forwardCallEntry,
				ForwardCallActionTableColumns columnEnum) {
			switch(columnEnum) {
			case COLUMN_NUMBER:
				return forwardCallEntry.getNumber();
			case COLUMN_FORWARDTYPE:
				if(forwardCallEntry.isIfBusy()) {
					return FORWARD_TYPE_BUSY;
				}
				if(forwardCallEntry.getRingCount() == 0) {
					return FORWARD_TYPE_IMMEDIATELY;
				}
				return FORWARD_TYPE_BY_RINGCOUNT;
			case COLUMN_RINGCOUNT:
				return (forwardCallEntry.getRingCount()==0?"":Integer.toString(forwardCallEntry.getRingCount()));
			}
			return null;
		}
	}

	class ForwardCallActionContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof ForwardCallData) {
				ForwardCallData fcd = (ForwardCallData) inputElement;
				return fcd.getHistoryEntries().toArray();
			}
			return null;
		}

		@Override
		public void dispose() {
			// do nothing
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}
		
	}
	
	public static final String WINDOW_TITLE = "Forwarding calls configuration";
	public static final String TOOLTIP_FORWARDCALLACTION = "Forwarding calls configuration";
	public static final String TOOLTIP_FORWARDCALLACTION_ACTIVE = "Forwarding calls configuration - Forward active";
	public static final String FORWARD_TYPE_IMMEDIATELY = "Immediately";
	public static final String FORWARD_TYPE_BY_RINGCOUNT = "By ringcount";
	public static final String FORWARD_TYPE_BUSY = "If busy";
	public static final String BUTTON_CANCEL = "Cancel";
	public static final String BUTTON_DEACTIVATE_FORWARD = "Deactivate forward";
	
	private Shell shell = null;
	private Text numberText = null;
	private Button forwardImmediatelyButton = null;
	private Button forwardIfBusyButton = null;
	private Button forwardByRingCountButton = null;
	private Text ringCountText = null;
	private Button closeButton = null;
	private Button forwardDeactivateButton = null;
	private TableViewer recentForwardsTableViewer= null;
	private IPreferenceStore store = null;
	private ForwardCallData forwardCallData = null;
	
	public ForwardCallAction() {
		super("", AS_PUSH_BUTTON); //$NON-NLS-1$
		updateImageDescriptor();
		updateToolTipText();
		updateEnabled();
	}
	
	public static boolean isAvailable(IPreferenceStore store) {
		return ForwardCallData.isAvailable(store);
	}
	
	@Override
	public void run() {
		super.run();
		
		createShell();
	}
	
	public void setStore(IPreferenceStore store) {
		this.store = store;
	}
	
	private void createShell() {
		if(store == null) {
			return;
		}
		
		if(shell == null || shell.isDisposed()) {
			shell = new Shell(Display.getCurrent());
			shell.setText(WINDOW_TITLE);
			
			shell.setLayout(new FillLayout(SWT.VERTICAL));
			Composite parent = new Composite(shell, SWT.NONE);
			GridLayoutFactory.swtDefaults().numColumns(1).equalWidth(true).applyTo(parent);
			//parent.setBackground(new Color(shell.getDisplay(), 0, 255, 0));
			
			// -----------------------
			// OPTIONS
			// -----------------------
			Composite optionsComposite = new Composite(parent, SWT.BORDER);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).applyTo(optionsComposite);
			GridLayoutFactory.swtDefaults().equalWidth(false).numColumns(2).applyTo(optionsComposite);
			
			(new Label(optionsComposite, SWT.NONE)).setText("Forward to");
			numberText = new Text(optionsComposite,SWT.BORDER);
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(numberText);
			
			Label foLabel = new Label(optionsComposite,SWT.NONE);
			foLabel.setText("Forward options");
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(foLabel);
			Composite ringCountComposite = new Composite(optionsComposite,SWT.NONE);
			int ringCountColCount = 4;
			GridLayoutFactory.swtDefaults().equalWidth(false).numColumns(ringCountColCount).applyTo(ringCountComposite);
			
			forwardDeactivateButton = new Button(ringCountComposite, SWT.RADIO);
			forwardDeactivateButton.setText(BUTTON_DEACTIVATE_FORWARD);
			GridDataFactory.swtDefaults().span(ringCountColCount,1).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(forwardDeactivateButton);
			
			forwardImmediatelyButton = new Button(ringCountComposite, SWT.RADIO);
			forwardImmediatelyButton.setText(FORWARD_TYPE_IMMEDIATELY);
			GridDataFactory.swtDefaults().span(ringCountColCount,1).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(forwardImmediatelyButton);
			
			forwardIfBusyButton = new Button(ringCountComposite, SWT.RADIO);
			forwardIfBusyButton.setText(FORWARD_TYPE_BUSY);
			GridDataFactory.swtDefaults().span(ringCountColCount,1).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(forwardIfBusyButton);
			
			forwardByRingCountButton = new Button(ringCountComposite, SWT.RADIO);
			forwardByRingCountButton.setText(FORWARD_TYPE_BY_RINGCOUNT);
			
			ringCountText = new Text(ringCountComposite, SWT.BORDER);
			GridDataFactory.swtDefaults().span(ringCountColCount -1,1).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(ringCountText);

			// -----------------------
			// RECENT FORWARDS
			// -----------------------
			Composite savedComposite = new Composite(parent, SWT.BORDER);
			GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(savedComposite);
			GridLayoutFactory.swtDefaults().numColumns(1).applyTo(savedComposite);
			
			Label rfLabel = new Label(savedComposite,SWT.NONE);
			rfLabel.setText("Recent forwards");
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(rfLabel);
			
			Composite recentForwardsTableViewerComposite = new Composite(savedComposite, SWT.NONE);
			GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(recentForwardsTableViewerComposite);
			recentForwardsTableViewerComposite.setLayout(new FillLayout());
			
			recentForwardsTableViewer = new TableViewer(recentForwardsTableViewerComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			
			TableLayout layout = new TableLayout();
			recentForwardsTableViewer.getTable().setLayout(layout);
			final Table table = recentForwardsTableViewer.getTable();
			table.setLinesVisible(false);
			table.setHeaderVisible(true);
			
			table.addListener(SWT.EraseItem, new TableSelectionColorChangeListener(table));

			ForwardCallActionTableColumns columnEnum = ForwardCallActionTableColumns.COLUMN_NUMBER;
			TableViewerColumn column = new TableViewerColumn(recentForwardsTableViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ForwardCallActionLabelProvider(columnEnum));
			layout.addColumnData(new ColumnWeightData(40, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(false);
//			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			
			columnEnum = ForwardCallActionTableColumns.COLUMN_FORWARDTYPE;
			column = new TableViewerColumn(recentForwardsTableViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ForwardCallActionLabelProvider(columnEnum));
			layout.addColumnData(new ColumnWeightData(40, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(false);
//			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			
			columnEnum = ForwardCallActionTableColumns.COLUMN_RINGCOUNT;
			column = new TableViewerColumn(recentForwardsTableViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ForwardCallActionLabelProvider(columnEnum));
			layout.addColumnData(new ColumnWeightData(40, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(false);
//			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));

			recentForwardsTableViewer.setContentProvider(new ForwardCallActionContentProvider());
			//recentForwardsTableViewer.setInput(shortcutButtonGroup);
			
			recentForwardsTableViewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					TableViewer tableViewer = (TableViewer)event.getSource();
					
					StructuredSelection test = (StructuredSelection)event.getSelection();
					//tableViewer.
					
					Object obj = test.getFirstElement();
					if(obj instanceof ForwardCallEntry) {
						ForwardCallEntry entry = (ForwardCallEntry)obj;
						fillForm(entry);
					}
				}
			});
			
			// -----------------------
			// BUTTONS
			// -----------------------
			Composite buttonsComposite = new Composite(parent, SWT.NONE);
			GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(buttonsComposite);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(buttonsComposite);

			Button activateButton = new Button(buttonsComposite, SWT.PUSH);
			activateButton.setText("OK");
			activateButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				@Override
				public void widgetSelected(SelectionEvent e) {
					activateForward();
				}
			});
			
			closeButton = new Button(buttonsComposite, SWT.PUSH);
			closeButton.setText(BUTTON_CANCEL);
			closeButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				@Override
				public void widgetSelected(SelectionEvent e) {
					closeWindow();
				}
			});
			
		}
		
		// Read data from store
		forwardCallData = new ForwardCallData(store);
		recentForwardsTableViewer.setInput(forwardCallData);
		
		// TODO set the location to the shell
		//shell.setSize(400, 500);
		shell.setLocation(500, 200);
		
		ForwardCallEntry entry = PjsuaClient.getForwardCallEntry();
		fillForm(entry);
		
		shell.pack();
		shell.open();
	}
	
	private void fillForm(ForwardCallEntry entry) {
		forwardDeactivateButton.setSelection(false);
		forwardIfBusyButton.setSelection(false);
		forwardImmediatelyButton.setSelection(false);
		forwardByRingCountButton.setSelection(false);
		numberText.setText("");
		ringCountText.setText("");
		
		if(entry != null) {
			numberText.setText(entry.getNumber());
			ringCountText.setText(Integer.toString(entry.getRingCount()));
			
			if(entry.isIfBusy()) {
				forwardIfBusyButton.setSelection(true);
			} else {
				if(entry.getRingCount() > 0) {
					forwardByRingCountButton.setSelection(true);
				} else {
					forwardImmediatelyButton.setSelection(true);
				}
			}
		}
		else {
			forwardDeactivateButton.setSelection(true);
		}
		
	}
	
	private void deactivateForward() {
		PjsuaClient.setForwardCallEntry(null);
		closeWindow();
	}
	private void activateForward() {
		if(forwardDeactivateButton.getSelection()) {
			deactivateForward();
			return;
		}
		
		String number = numberText.getText();
		
		// immediately forward options
		boolean ifBusy = false;
		int ringCount = 0;
		
		if(forwardIfBusyButton.getSelection()) {
			// busy forward
			ifBusy = true;
		}
		else {
			if(forwardByRingCountButton.getSelection()) {
				try {
					ringCount = Integer.parseInt(ringCountText.getText());
				} catch(NumberFormatException nfe) {
					// do nothing
				}
			}
		}
		ForwardCallEntry newForwardCallEntry = forwardCallData.setForward(store, number, ringCount, ifBusy);
		PjsuaClient.setForwardCallEntry(newForwardCallEntry);
		
		closeWindow();
	}

	private void closeWindow() {
		shell.close();
	}

	@Override
	public void update() {
		updateEnabled();
		updateImageDescriptor();
		updateToolTipText();
	}

	@Override
	public boolean shouldBeVisible() {
		return false;
	}
	
	private void updateEnabled() {
		setEnabled(true);
	}
	
	private void updateImageDescriptor() {
		// TODO change image depending on forward state: config / active
		setImageDescriptor(IconStore.getDefault().getImageRegistry().getDescriptor(IconStore.FORWARD_CALL_CONFIG));
	}

	private void updateToolTipText() {
		// TODO update tooltip depending on forward state: config / active
		setToolTipText(TOOLTIP_FORWARDCALLACTION);
	}
	
}
