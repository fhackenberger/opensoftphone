package org.acoveo.callcenter.sipclient.preferences.shortcutbuttons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.acoveo.callcenter.guiLibrary.TableSelectionColorChangeListener;
import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.sipclient.Activator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ShortcutButtonGroupsPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {
	
	public static final int MAX_SHORTCUT_GROUPS = 20;
	public static final int MAX_SHORTCUT_BUTTONS = 100;
	public static final int MAX_SHORTCUT_BUTTONS_DIALPAD = 10;
	
	public static final String PREF_SHORTCUT_GROUP_PREFIX = "Shortcut.Group";
	public static final String PREF_SHORTCUT_BUTTON_PREFIX = "Shortcut.Button";
	
	private final int NUM_COLUMNS = 2;
	
	private List<ShortcutButtonGroup> shortcutButtonGroupList;
	private Text addGroupTenantText, addGroupNameText;
	private CTabFolder groupTabFolder;
	private List<GroupTabItem> groupTabItemList = new ArrayList<GroupTabItem>();
	private Composite preferencePageParent;
	private Composite mainComposite;
	
	public enum ShortcutButtonTableColumns {
		//COLUMN_ROW_NUMBER,
		COLUMN_NAME,
		COLUMN_NUMBER,
		COLUMN_ACTIVE,
		COLUMN_FORWARD,
		COLUMN_DIALPAD,
		COLUMN_SUFFIX_DIALING,
		COLUMN_POSITION,
		COLUMN_DELETE;
		public static ShortcutButtonTableColumns fromOrdinal(int ordinal) {
			if (ordinal < 0 || ordinal >= ShortcutButtonTableColumns.values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal"); //$NON-NLS-1$
			}
			return ShortcutButtonTableColumns.values()[ordinal];
		}

		public String getLabelText() {
			switch(this) {
			case COLUMN_NAME:
				return "Name";
			case COLUMN_NUMBER:
				return "Number";
			case COLUMN_ACTIVE:
				return "Active";
			case COLUMN_FORWARD:
				return "Forward";
			case COLUMN_DIALPAD:
				return "Dialpad";
			case COLUMN_SUFFIX_DIALING:
				return "Suffix Dial";
			case COLUMN_POSITION:
				return "Position";
			case COLUMN_DELETE:
				return "Delete";
			}
			return "";
		}
	}
	

	class ShortcutButtonContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof ShortcutButtonGroup) {
				ShortcutButton[] buttons = null;
				ShortcutButtonGroup grp = (ShortcutButtonGroup)inputElement;
				List<ShortcutButton> list = grp.getShortcutButtons();
				buttons = new ShortcutButton[list.size()];
				buttons = list.toArray(buttons);
				return buttons;
			}
			return null;
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Do nothing
		}
		
	}
	
	static public class ShortcutButtonLabelProvider extends ColumnLabelProvider implements IColorProvider {
		private ShortcutButtonTableColumns column;
		private ShortcutButtonGroup shortcutButtonGroup;
		private static final Color colorSwitch = Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION);
		
		public ShortcutButtonLabelProvider(ShortcutButtonTableColumns column, ShortcutButtonGroup shortcutButtonGroup) {
			this.column = column;
			this.shortcutButtonGroup= shortcutButtonGroup;
		}

		@Override
		public Color getBackground(Object element) {
//			int pos = shortcutButtonGroup.getShortcutButtons().indexOf(element);
//			if(pos % 2 == 0) {
//				return colorSwitch;
//			}
			return super.getBackground(element);
		}
		
		@Override
		public Image getImage(Object element) {
			switch(column) {
			case COLUMN_DELETE:
				return IconStore.getDefault().getImageRegistry().get(IconStore.REMOVE_PROPERTY_ROW_CONSTANT);
			default: 
				return super.getImage(element);
			}
		}
		
		public static String getText(ShortcutButton button, ShortcutButtonTableColumns column) {
			String res = null;
			switch(column) {
			case COLUMN_NAME:
				res = button.getName();
				break;
			case COLUMN_NUMBER:
				res = button.getNumber();
				break;
			case COLUMN_ACTIVE:
				res = button.isActive()?"Yes":"No";
				break;
			case COLUMN_FORWARD:
				res = button.isForward()?"Yes":"No";
				break;
			case COLUMN_DIALPAD:
				res = button.isDialpad()?"Yes":"No";
				break;
			case COLUMN_SUFFIX_DIALING:
				res = button.isSuffixDialing()?"Yes":"No";
				break;
			case COLUMN_POSITION:
				res = Integer.toString(button.getPosition());
				break;
			case COLUMN_DELETE:
				res = "";
				break;
			}
			return res;
		}
		
		@Override
		public String getText(Object element) {
			if(element instanceof ShortcutButton) {
				ShortcutButton button = (ShortcutButton) element;
				return getText(button, column);
			}
			return null;
		}

		/**
		 * Returns the value as object depending on the given column.
		 * Types are Boolean or String.
		 * @param button
		 * @param column
		 * @return
		 */
		public static Object getValueAsObject(ShortcutButton button,
				ShortcutButtonTableColumns column) {
			Object res = null;
			switch(column) {
			case COLUMN_NAME:
				res = button.getName();
				break;
			case COLUMN_NUMBER:
				res = button.getNumber();
				break;
			case COLUMN_ACTIVE:
				res = new Boolean(button.isActive());
				break;
			case COLUMN_FORWARD:
				res = new Boolean(button.isForward());
				break;
			case COLUMN_DIALPAD:
				res = new Boolean(button.isDialpad());
				break;
			case COLUMN_SUFFIX_DIALING:
				res = new Boolean(button.isSuffixDialing());
				break;
			case COLUMN_POSITION:
				res = Integer.toString(button.getPosition());
				break;
			}
			return res;
		}

		@Override
		public void dispose() {
			super.dispose();
		}
	}
	
	
	/**
	 * This represents a button group as tab item for the preference page like ShortcutButtonTabItem for the softphone. 
	 * @author brandner
	 *
	 */
	private class GroupTabItem implements Comparable<GroupTabItem> {
		class ShortcutButtonEditingSupport extends EditingSupport {
			private ShortcutButtonTableColumns column;
			
			public ShortcutButtonEditingSupport(ColumnViewer viewer, ShortcutButtonTableColumns column) {
				super(viewer);
				this.column = column;
			}
			
			private void confirmRemoveButton(Object element) {
				if(element instanceof ShortcutButton) {
					ShortcutButton button = (ShortcutButton) element;
					boolean confirmed = MessageDialog.openConfirm(tabItem.getParent().getShell(), String.format("Delete button '%s'", button.getName()), "Do you want to delete this button?");
					if(confirmed) {
						shortcutButtonViewer.remove(button);
						removeButton(button);
					}
				}
			}
			
			@Override
			protected boolean canEdit(Object element) {
				switch(column) {
				case COLUMN_NAME:
				case COLUMN_NUMBER:
				case COLUMN_ACTIVE:
				case COLUMN_FORWARD:
				case COLUMN_DIALPAD:
				case COLUMN_SUFFIX_DIALING:
				case COLUMN_POSITION:
					return true;
				case COLUMN_DELETE:
					// no break
					// call from canEdit() because we don't need an editor
					confirmRemoveButton(element);
				default:
					return false;
				}
			}
			
			@Override
			protected CellEditor getCellEditor(Object element) {
				CellEditor editor = null;
				if(element instanceof ShortcutButton) {
					try {
						switch(column) {
						case COLUMN_NAME:
						case COLUMN_NUMBER:
						case COLUMN_POSITION:
							// text field
							editor = new TextCellEditor();
							editor.create(shortcutButtonViewer.getTable());
							break;
						case COLUMN_ACTIVE:
						case COLUMN_FORWARD:
						case COLUMN_DIALPAD:
						case COLUMN_SUFFIX_DIALING:
							editor = new CheckboxCellEditor();
							editor.create(shortcutButtonViewer.getTable());
							break;
						}
					} catch( Exception e ) {
						editor = null;
					}
				}
				return editor;
			}
			
			@Override
			protected Object getValue(Object element) {
				ShortcutButton button = (ShortcutButton)element;
				return ShortcutButtonLabelProvider.getValueAsObject(button,column);
			}
			
			@Override
			protected void setValue(Object element, Object value) {
				final ShortcutButton button = (ShortcutButton) element;
				switch(column) {
				case COLUMN_NAME:
					button.setName((String)value);
					break;
				case COLUMN_NUMBER:
					button.setNumber((String)value);
					break;
				case COLUMN_POSITION:
					button.setPosition(Integer.parseInt((String)value));
					break;
				case COLUMN_ACTIVE:
					button.setActive(((Boolean)value).booleanValue());
					break;
				case COLUMN_FORWARD:
					button.setForward(((Boolean)value).booleanValue());
					break;
				case COLUMN_DIALPAD:
					button.setDialpad(((Boolean)value).booleanValue());
					break;
				case COLUMN_SUFFIX_DIALING:
					button.setSuffixDialing(((Boolean)value).booleanValue());
					break;
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						shortcutButtonViewer.refresh(button);
					}
				});
			}
		}
		
		private TableViewer shortcutButtonViewer;
		private CTabItem tabItem;
		private Composite gtiComposite;
		private ShortcutButtonGroup shortcutButtonGroup;
		private Button active;
		private Text position;
		private Text columnCount;
//		private Composite buttonComposite;
//		private List<ShortcutButtonPanel> shortcutButtonPanelList;
		
		public GroupTabItem(CTabFolder folder, ShortcutButtonGroup group) {
			this(folder,group,false);
		}
		
		public GroupTabItem(CTabFolder folder, ShortcutButtonGroup group, boolean setActiveTab) {
			shortcutButtonGroup = group;
			//shortcutButtonPanelList = new ArrayList<ShortcutButtonPanel>();
			createTabItem(folder);
			if(setActiveTab) {
				folder.setSelection(tabItem);
			}
		}
		
		private void createTabItem(CTabFolder folder) {
			tabItem = new CTabItem(folder, SWT.NONE);
			gtiComposite = new Composite(folder, SWT.NONE);
			tabItem.setControl(gtiComposite);
			
			int numColumns = 5, numRows = 3;
			
			GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(gtiComposite);

			// short name as tab label
			tabItem.setText(shortcutButtonGroup.getName());
			
			// long tenant name as label
			Label tenantLabel = new Label(gtiComposite, SWT.NONE);
			tenantLabel.setText("Group: "+shortcutButtonGroup.getTenant());
			GridDataFactory.swtDefaults().span(1, numRows).grab(true, false).align(SWT.FILL, SWT.BEGINNING).applyTo(tenantLabel);
			
			Label positionLabel = new Label(gtiComposite, gtiComposite.getStyle());
			positionLabel.setText("Position");
			// FIXME number validating textfield
			position = new Text(gtiComposite, SWT.BORDER);
			position.setText(Integer.toString(shortcutButtonGroup.getPosition()));
			position.setTextLimit(5);
			position.setSize(position.computeSize(50, SWT.DEFAULT));
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(position);
			/*
			position.addVerifyListener(new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent e) {
					switch (e.keyCode) {
					case SWT.DEL:
					case SWT.BS:
						return;
					}
					String regexp = "[0-9]";
					char c = e.character;
					if (!("" + c).matches(regexp)) {
						e.doit = false;
					}
				}
			});
			*/
			
			active = new Button(gtiComposite, SWT.CHECK);
			active.setSelection(shortcutButtonGroup.isActive());
			Label activeLabel = new Label(gtiComposite, SWT.NONE);
			activeLabel.setText("Active");
			
			// row 2
			Label columnCountLabel = new Label(gtiComposite, gtiComposite.getStyle());
			columnCountLabel.setText("Column count");
			columnCount = new Text(gtiComposite, SWT.BORDER);
			columnCount.setText(Integer.toString(shortcutButtonGroup.getColumnCount()));
			columnCount.setTextLimit(2);
			columnCount.setSize(columnCount.computeSize(50, SWT.DEFAULT));
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(columnCount);

			Label emptyLabel = new Label(gtiComposite, gtiComposite.getStyle());
			GridDataFactory.swtDefaults().span(2,1).applyTo(emptyLabel);
			
			Button addShortcutButton = new Button(gtiComposite, SWT.PUSH);
			addShortcutButton.setText("Add shortcut button");
			GridDataFactory.swtDefaults().span(2,1).align(SWT.END, SWT.BEGINNING).applyTo(addShortcutButton);
			
			addShortcutButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				@Override
				public void widgetSelected(SelectionEvent e) {
					addShortcutButton();
				}
			});
			
			
			Button deleteButton = new Button(gtiComposite, SWT.PUSH);
			deleteButton.setText("Delete group");
			GridDataFactory.swtDefaults().span(2,1).align(SWT.END, SWT.BEGINNING).applyTo(deleteButton);
			
			final Shell shell = folder.getShell();
			deleteButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean confirmed = MessageDialog.openConfirm(shell, "Delete tenant", "Do you want to delete this tenant?");
					if(confirmed) {
						remove();
					}
				}
			});
			createButtonTable();
		}
		
		protected void addShortcutButton() {
			ShortcutButton button = shortcutButtonGroup.addButton();
			button.setName((new StringBuffer("Shortcut button ")).append(shortcutButtonGroup.size()).toString());
			shortcutButtonViewer.add(button);
		}

		private void createButtonTable() {
			final Composite composite = new Composite(gtiComposite,SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
			.span(((GridLayout)gtiComposite.getLayout()).numColumns, 1).applyTo(composite);
			
			composite.setLayout(new FillLayout());
			
			shortcutButtonViewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL
					| SWT.V_SCROLL | SWT.BORDER);
			TableLayout layout = new TableLayout();
			shortcutButtonViewer.getTable().setLayout(layout);
			final Table table = shortcutButtonViewer.getTable();
			table.setLinesVisible(false);
			table.setHeaderVisible(true);
			
			table.addListener(SWT.EraseItem, new TableSelectionColorChangeListener(table));				
			/*
			AgentGroupViewColumns columnEnum = AgentGroupViewColumns.COLUMN_AGENT_NAME;
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(Messages.AgentGroupView_28);
			column.setLabelProvider(new AgentGroupViewLabelProvider(parent.getDisplay(), columnEnum, this));
			column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			layout.addColumnData(new ColumnWeightData(30, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			columnShowHideSupport.addDefaultColumn(columnEnum);
			 */
			
			ShortcutButtonTableColumns columnEnum = ShortcutButtonTableColumns.COLUMN_NAME;
			TableViewerColumn column = new TableViewerColumn(shortcutButtonViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ShortcutButtonLabelProvider(columnEnum, shortcutButtonGroup));
			column.setEditingSupport(new ShortcutButtonEditingSupport(shortcutButtonViewer,columnEnum));
			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			layout.addColumnData(new ColumnWeightData(40, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			//columnShowHideSupport.addDefaultColumn(columnEnum);
		
			columnEnum = ShortcutButtonTableColumns.COLUMN_NUMBER;
			column = new TableViewerColumn(shortcutButtonViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ShortcutButtonLabelProvider(columnEnum, shortcutButtonGroup));
			column.setEditingSupport(new ShortcutButtonEditingSupport(shortcutButtonViewer,columnEnum));
			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			layout.addColumnData(new ColumnWeightData(40, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			//columnShowHideSupport.addDefaultColumn(columnEnum);
		
			columnEnum = ShortcutButtonTableColumns.COLUMN_ACTIVE;
			column = new TableViewerColumn(shortcutButtonViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ShortcutButtonLabelProvider(columnEnum, shortcutButtonGroup));
			column.setEditingSupport(new ShortcutButtonEditingSupport(shortcutButtonViewer,columnEnum));
			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			layout.addColumnData(new ColumnWeightData(10, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			//columnShowHideSupport.addDefaultColumn(columnEnum);
		
			columnEnum = ShortcutButtonTableColumns.COLUMN_FORWARD;
			column = new TableViewerColumn(shortcutButtonViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ShortcutButtonLabelProvider(columnEnum, shortcutButtonGroup));
			column.setEditingSupport(new ShortcutButtonEditingSupport(shortcutButtonViewer,columnEnum));
			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			layout.addColumnData(new ColumnWeightData(10, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			//columnShowHideSupport.addDefaultColumn(columnEnum);
		
			columnEnum = ShortcutButtonTableColumns.COLUMN_DIALPAD;
			column = new TableViewerColumn(shortcutButtonViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ShortcutButtonLabelProvider(columnEnum, shortcutButtonGroup));
			column.setEditingSupport(new ShortcutButtonEditingSupport(shortcutButtonViewer,columnEnum));
			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			layout.addColumnData(new ColumnWeightData(10, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			//columnShowHideSupport.addDefaultColumn(columnEnum);
		
			columnEnum = ShortcutButtonTableColumns.COLUMN_SUFFIX_DIALING;
			column = new TableViewerColumn(shortcutButtonViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ShortcutButtonLabelProvider(columnEnum, shortcutButtonGroup));
			column.setEditingSupport(new ShortcutButtonEditingSupport(shortcutButtonViewer,columnEnum));
			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			layout.addColumnData(new ColumnWeightData(10, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			//columnShowHideSupport.addDefaultColumn(columnEnum);
		
			columnEnum = ShortcutButtonTableColumns.COLUMN_POSITION;
			column = new TableViewerColumn(shortcutButtonViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ShortcutButtonLabelProvider(columnEnum, shortcutButtonGroup));
			column.setEditingSupport(new ShortcutButtonEditingSupport(shortcutButtonViewer,columnEnum));
			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			layout.addColumnData(new ColumnWeightData(20, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			//columnShowHideSupport.addDefaultColumn(columnEnum);
		
			columnEnum = ShortcutButtonTableColumns.COLUMN_DELETE;
			column = new TableViewerColumn(shortcutButtonViewer, SWT.NONE, columnEnum.ordinal());
			column.getColumn().setText(columnEnum.getLabelText());
			column.setLabelProvider(new ShortcutButtonLabelProvider(columnEnum, shortcutButtonGroup));
			column.setEditingSupport(new ShortcutButtonEditingSupport(shortcutButtonViewer,columnEnum));
			//column.getColumn().addSelectionListener(new TableColumnSorterListener(columnEnum, viewer));
			layout.addColumnData(new ColumnWeightData(20, 10));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			//columnShowHideSupport.addDefaultColumn(columnEnum);
		
			//viewer.setComparator(new AgentGroupViewTableSorter(this));
			shortcutButtonViewer.setContentProvider(new ShortcutButtonContentProvider());
			shortcutButtonViewer.setInput(shortcutButtonGroup);
		}
		
		public boolean remove() {
			// remove tab item from the config page
			groupTabItemList.remove(this);
			// remove group from the list
			shortcutButtonGroupList.remove(shortcutButtonGroup);
			// remove data from store
			shortcutButtonGroup.remove();
			Composite c = (Composite) tabItem.getControl();
			tabItem.dispose();
			if(c != null) {
				c.dispose();
			}
			return true;
		}
		
		public boolean removeButton(ShortcutButton button) {
			return shortcutButtonGroup.removeButton(button);
		}
		
		public boolean store() {
			return shortcutButtonGroup.store();
		}
		
		public void readForm() {
			shortcutButtonGroup.setActive(active.getSelection());
			int pos = 0;
			int columnCount = 1;
			try {
				pos = Integer.parseInt(position.getText());
			} catch (NumberFormatException nfe) {
				// do nothing
			}
			try {
				columnCount = Integer.parseInt(this.columnCount.getText());
			} catch (NumberFormatException nfe) {
				// do nothing
			}
			shortcutButtonGroup.setPosition(pos);
			shortcutButtonGroup.setColumnCount(columnCount);
		}
		
		public CTabItem getTabItem() {
			return tabItem;
		}
		
		public Composite getComposite() {
			return gtiComposite;
		}
		
		public ShortcutButtonGroup getShortcutButtonGroup() {
			return shortcutButtonGroup;
		}

		@Override
		public int compareTo(GroupTabItem o) {
			return getShortcutButtonGroup().compareTo(o.getShortcutButtonGroup());
		}
	}// class GroupTabItem
	

	public ShortcutButtonGroupsPreferencePage() {
		super();
	}

	public ShortcutButtonGroupsPreferencePage(String title) {
		super(title);
	}

	public ShortcutButtonGroupsPreferencePage(String title,
			ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		preferencePageParent = parent;
		// disable apply and defaults buttons
		noDefaultAndApplyButton();
		
		// read data from store
		loadDataFromStore();
		
		// create widgets
		mainComposite = new Composite(parent, parent.getStyle());
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(mainComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true,true).applyTo(mainComposite);
		//mainComposite.setLayout(new FillLayout(SWT.VERTICAL));
		
		//Label infoLabel = new Label(mainComposite, parent.getStyle());
		//infoLabel.setText("");
		
		// Grid
		Composite addGroupComposite = new Composite(mainComposite, parent.getStyle());
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(addGroupComposite);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).applyTo(addGroupComposite);
		
		Label addGroupTenantLabel = new Label(addGroupComposite, parent.getStyle());
		addGroupTenantLabel.setText("Group");
		addGroupTenantText = new Text(addGroupComposite, parent.getStyle() | SWT.BORDER | SWT.SINGLE);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).applyTo(addGroupTenantText);

		String groupNameDescription = "Use an unique identifier which only consists off alphanumeric signs!";
		Label addGroupNameLabel = new Label(addGroupComposite, parent.getStyle());
		addGroupNameLabel.setText("Group abbreviation");
		addGroupNameLabel.setToolTipText(groupNameDescription);
		addGroupNameText = new Text(addGroupComposite, parent.getStyle() | SWT.BORDER | SWT.SINGLE);
		addGroupNameText.setToolTipText(groupNameDescription);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).applyTo(addGroupNameText);
		
		Button addGroupButton = new Button(addGroupComposite, parent.getStyle() | SWT.PUSH);
		addGroupButton.setText("Add group");
		GridDataFactory.swtDefaults().span(NUM_COLUMNS, 1).align(SWT.END, SWT.BEGINNING);
		addGroupButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				addGroupTab(null,false);
			}
			
		});
		// /Grid
		
		groupTabFolder = new CTabFolder(mainComposite, parent.getStyle());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true,true).applyTo(groupTabFolder);
		
		createTabs();
		return null;
	}
	
	/**
	 * Create all tabs for tenants
	 */
	private void createTabs() {
		for(ShortcutButtonGroup grp: shortcutButtonGroupList) {
			addGroupTab(grp,false);
		}
		groupTabFolder.setSelection(0);
	}
	
	public ShortcutButtonGroup getShortcutButtonGroupByName(String name) {
		for(ShortcutButtonGroup grp: shortcutButtonGroupList) {
			if(grp.getName() != null && grp.getName().equalsIgnoreCase(name)) {
				return grp;
			}
		}
		return null;
	}
	
	private void addGroupTab(ShortcutButtonGroup group, boolean selectIt) {
		if(group == null) {
			String tenantName = addGroupTenantText.getText();
			String tenantShortName = addGroupNameText.getText();
			boolean error = false;
			StringBuffer message = null;
			if(tenantName != null && tenantShortName != null && !tenantName.isEmpty() && !tenantShortName.isEmpty()) {
				// check name
				if(getShortcutButtonGroupByName(tenantShortName) == null) {
					group = addGroup(tenantShortName, tenantName);
				} else {
					error = true;
					message = new StringBuffer(String.format("The group abbreviation '%s' already exists!", tenantShortName));
				}
			} else {
				error = true;
				message = new StringBuffer("Enter following data:\n");
				if(tenantName == null || tenantName.length() == 0) {
					message.append("* "); //$NON-NLS-1$
					message.append("Group name");
					message.append("\n"); //$NON-NLS-1$
				}
				if(tenantShortName == null || tenantShortName.length() == 0) {
					message.append("* "); //$NON-NLS-1$
					message.append("Group abbreviation");
				}
			}
			if(error) {
				MessageDialog.openError(preferencePageParent.getShell(), "Error while adding a new group", message.toString());
				return;
			}
		}
		GroupTabItem gti = new GroupTabItem(groupTabFolder, group);
		groupTabItemList.add(gti);
		if(selectIt) {
			//groupTabFolder.setSelection(gti.getTabItem());
			int itemCount = groupTabFolder.getItemCount();
			groupTabFolder.setSelection(itemCount - 1);
		}
		addGroupTenantText.setText("");
		addGroupNameText.setText("");
	}

	private ShortcutButtonGroup addGroup(String name, String tenant) {
		String prefix = PREF_SHORTCUT_GROUP_PREFIX + "." + Integer.toString(shortcutButtonGroupList.size());
		ShortcutButtonGroup grp = new ShortcutButtonGroup(getPreferenceStore(),prefix);
		grp.setTenant(tenant);
		grp.setName(name);
		shortcutButtonGroupList.add(grp);
		return grp;
	}

	private void loadDataFromStore() {
		IPreferenceStore store = getPreferenceStore();
		if(store == null) {
			return;
		}
		shortcutButtonGroupList = loadShortcutButtons(store);
	}
	
	public static List<ShortcutButtonGroup> loadShortcutButtons(IPreferenceStore store) {
		List<ShortcutButtonGroup> list = new ArrayList<ShortcutButtonGroup>();
		
		int i = 0, failedCnt = 0;
		ShortcutButtonGroup grp = null;
		StringBuffer prefix;
		while(i < MAX_SHORTCUT_GROUPS && failedCnt < 3) {
			prefix = new StringBuffer(PREF_SHORTCUT_GROUP_PREFIX);
			prefix.append(".").append(i).append(".");
			grp = ShortcutButtonGroup.load(store, prefix.toString());
			if(grp != null) {
				list.add(grp);
			} else {
				failedCnt++;
			}
			i++;
		}
		return list;
	}

	@Override
	public void init(IWorkbench workbench) {
		
	}
	
	@Override
	public void performDefaults() {
		// no nothing?
	}

	/*
	 * must be in the right order!!!
	 * remove, read form, sort, store
	 */
	private enum Actions {
		ACTION_REMOVE_DATA_FROM_STORE,
		ACTION_READ_FORM,
		ACTION_SORT_OBJECTS,
		ACTION_STORE_DATA
	}
	
	@Override
	public boolean performOk() {
		boolean result = true;
		StringBuffer prefix;
		for(Actions action: Actions.values()) {
			if(action == Actions.ACTION_SORT_OBJECTS) {
				Collections.sort(shortcutButtonGroupList);
				Collections.sort(groupTabItemList);
			}
			for(int i=0; i < groupTabItemList.size(); i++) {
				GroupTabItem gti = groupTabItemList.get(i);
				boolean res = true;
				switch(action) {
				case ACTION_REMOVE_DATA_FROM_STORE:
					// first remove data from store, may be wrong after sorting
					res = gti.getShortcutButtonGroup().removeIfChanged();
					break;
				case ACTION_READ_FORM: 
					// first read form
					gti.readForm();
					break;
				case ACTION_SORT_OBJECTS:
					if(gti.getShortcutButtonGroup().getPosition() > 0) {
						// only if pos > 0, 0 means at the end of the list
						gti.getShortcutButtonGroup().setPosition( (i+1) * 10);
					}
					prefix = new StringBuffer(PREF_SHORTCUT_GROUP_PREFIX);
					prefix.append(".").append(i).append(".");
					gti.getShortcutButtonGroup().setPreferencePrefix(prefix.toString());
					
					gti.getShortcutButtonGroup().sortButtons();
					break;
				case ACTION_STORE_DATA: 				
					res = gti.store();
					break;
				}
				if(!res) {
					// error
					result = false;
				}
			}
		}
		return result;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

}
