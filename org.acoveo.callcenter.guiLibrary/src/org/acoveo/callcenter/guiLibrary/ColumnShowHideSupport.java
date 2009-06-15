/**
 * 
 */
package org.acoveo.callcenter.guiLibrary;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.acoveo.callcenter.nls.Messages;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

public class ColumnShowHideSupport {
		protected final TableViewer tableViewer;
		protected boolean animationEnabled = true;
		
		public static final String SHOW_INFIX = "show"; //$NON-NLS-1$
		public static final String WIDTH_INFIX = "width"; //$NON-NLS-1$
		public static final String POS_INFIX = "position"; //$NON-NLS-1$
		public static final int DEFAULT_WIDTH = 50;
		
		class ColumnInfo {
			public ColumnInfo(boolean fake, int index) {
				this.fake = fake;
				this.index = index;
			}
			public ColumnInfo(String text, MenuItem menuItem, int index) {
				this.text = text;
				this.menuItem = menuItem;
				this.index = index;
			}
			public boolean fake = false;
			public String text;
			public MenuItem menuItem;
			public int index;
		}
		
		// Maps from columnname to column info
		Map<String, ColumnInfo> columnInfos = new HashMap<String, ColumnInfo>();
		String prefix;
		Preferences pref;
		boolean fullStoreDone = false;
		/**
		 * @param prefix A prefix to use for the column names (must not be null)
		 * @param tableViewer The table viewer to use to set the column widths
		 */
		public ColumnShowHideSupport(TableViewer tableViewer, String prefix, Preferences pref, boolean animationEnabled) {
			this.tableViewer = tableViewer;
			this.prefix = prefix;
			this.pref = pref;
			this.animationEnabled = animationEnabled;
		}

		public void addDefaultColumn(String column, String columnName, int index) {
			columnInfos.put(column, new ColumnInfo(Messages.ColumnShowHideSupport_0 + columnName + Messages.ColumnShowHideSupport_1, null, index));
			registerProperties(column);
		}
		
		public void addDefaultColumn(String column, int index) {
			columnInfos.put(column, new ColumnInfo(Messages.ColumnShowHideSupport_4 + column.toString() + Messages.ColumnShowHideSupport_5, null, index));
			registerProperties(column);
		}
		
		public void addColumn(String column, String menuText, int index) {
			columnInfos.put(column, new ColumnInfo(menuText, null, index));
			registerProperties(column);
		}
		
		/** Add an invisible dummy column
		 * This is useful as a workaround for the table row selection bug on windows
		 * @param column
		 * @param index
		 */
		public void addDummyColumn(String column, int index) {
			columnInfos.put(column, new ColumnInfo(true, index));
			registerProperties(column);
		}
		
		public void removeColumn(String column) {
			columnInfos.remove(column);
		}
		
		/** Creates a drop down menu and menu items for each column
		 * 
		 * The menu could for example be used to create a viewer menu like this:
		 * Menu columnMenu = new Menu(parent.getShell());
		 * MenuItem columnsItem = new MenuItem(columnMenu, parent.getStyle() | SWT.CASCADE);
		 * columnsItem.setText("Show/Hide columns");
		 * Menu columnsSubMenu = columnShowHideSupport.createMenu(parent.getShell());
		 * columnsItem.setMenu(columnsSubMenu);
		 * viewer.getTable().setMenu(columnMenu);
		 * 
		 * @param parent The shell to use as a parent for the menu
		 * @return A {@code Menu} with style {@code SWT#DROP_DOWN}
		 */
		public Menu createMenu(Shell parent, TableViewer viewer) {
			Menu columnsSubMenu = new Menu(parent, parent.getStyle() | SWT.DROP_DOWN);
			List<Entry<String, ColumnInfo>> columnEntries = new LinkedList<Entry<String, ColumnInfo>>(columnInfos.entrySet());
			Collections.sort(columnEntries, new Comparator<Entry<String, ColumnInfo>>() {
				@Override
				public int compare(Entry<String, ColumnInfo> o1, Entry<String, ColumnInfo> o2) {
					if(o1.getValue().text == null) {
						return 0;
					}
					return o1.getValue().text.compareTo(o2.getValue().text);
				}
			});
			for(Entry<String, ColumnInfo> columnEntry : columnEntries) {
				String columnName = columnEntry.getKey();
				ColumnInfo info = columnEntry.getValue();
				if(info.fake) {
					continue;
				}
				MenuItem hideColumnItem = new MenuItem(columnsSubMenu, parent.getStyle() | SWT.CHECK);
				hideColumnItem.setText(info.text);
				info.menuItem = hideColumnItem;
				Boolean show = retrieveShow(columnName);
				if(show != null) {
					hideColumnItem.setSelection(show);
				}else {
					hideColumnItem.setSelection(true);
				}
				hideColumnItem.addSelectionListener(new ShowHideColumnListener(viewer, columnName, info.index, animationEnabled, this));
			}
			return columnsSubMenu;
		}
		
		/** Restores the column width and show/hide from the given store
		 * 
		 * @see #storeColumnInfo(Preferences, String)
		 */
		public void restoreColumnInfo() {
			for(final Entry<String, ColumnInfo> entry : columnInfos.entrySet()) {
				if(entry.getValue().fake) {
					continue;
				}
				final TableColumn tableCol = this.tableViewer.getTable().getColumn(entry.getValue().index);
				if(tableCol != null) {
					tableCol.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							MenuItem menuItem = entry.getValue().menuItem;
							Boolean show = retrieveShow(entry.getKey());
							int width = retrieveWidthToSet(entry.getKey());
							tableCol.setWidth(width);

							// Set the checkbox in the menu to the correct value
							if(menuItem != null) {
								menuItem.setSelection(show == null ? true : show);
							}
						}
					});
				}
			}
			tableViewer.getTable().setColumnOrder(getColumnOrder(tableViewer.getTable().getColumnOrder()));
		}
		
		private String buildPrefName(String column, String infix) {
			return prefix + "_" + column + "_" + infix;
		}
		
		protected void storeWidth(String column, Integer width) {
			if(pref != null) {
				if(!fullStoreDone) {
					fullStoreDone = true; // Set this first to prevent infinite recursion
					storeState();
				}else {
//					System.out.println("storeWidth: " + buildPrefName(column, WIDTH_INFIX) + " " + width); //$NON-NLS-1$ //$NON-NLS-2$
					pref.setValue(buildPrefName(column, WIDTH_INFIX), width);
				}
			}
		}
		protected void storeShow(String column, Boolean show) {
			if(pref != null) {
				if(!fullStoreDone) {
					fullStoreDone = true; // Set this first to prevent infinite recursion
					storeState();
				}else {
					pref.setValue(buildPrefName(column, SHOW_INFIX), show);
				}
			}
		}
		protected void storePosition(String column, int position) {
			if(pref != null) {
				if(!fullStoreDone) {
					fullStoreDone = true; // Set this first to prevent infinite recursion
					storeState();
				}else {
					pref.setValue(buildPrefName(column, POS_INFIX), position);
				}
			}
		}
		protected Integer retrieveWidth(String column) {
			if(pref != null) {
				int width = pref.getInt(buildPrefName(column, WIDTH_INFIX));
				if(width != -1) {
//					System.out.println("retrieveWidth: " + buildPrefName(column, WIDTH_INFIX) + " " + width); //$NON-NLS-1$ //$NON-NLS-2$
					return width;
				}
			}
			return null;
		}
		protected Boolean retrieveShow(String column) {
			if(pref != null) {
				Boolean show = pref.getBoolean(buildPrefName(column, SHOW_INFIX));
//				System.out.println("retrieveShow: " + buildPrefName(column, SHOW_INFIX) + " " + show); //$NON-NLS-1$ //$NON-NLS-2$
				return pref.getBoolean(buildPrefName(column, SHOW_INFIX));
			}
			return null;
		}
		protected Integer retrievePosition(String column) {
			if(pref != null) {
				int pos = pref.getInt(buildPrefName(column, POS_INFIX));
				if(pos != -1) {
					return pos;
				}
			}
			return null;
		}
		protected int retrieveWidthToSet(String column) {
			Boolean show = retrieveShow(column);
			Integer width = retrieveWidth(column);
			if(show != null && !show) {
				// Explicitly hidden
				return 0;
			}else if(width != null) {
				// Not hidden and width stored
				return width;
			}else {
				// Shown, but no width set, apply default
				return DEFAULT_WIDTH;
			}
		}
		protected void registerProperties(String column) {
			if(pref != null) {
				pref.setDefault(buildPrefName(column, SHOW_INFIX), true);
				pref.setDefault(buildPrefName(column, WIDTH_INFIX), -1);
				pref.setDefault(buildPrefName(column, POS_INFIX), -1);
			}
		}
		public void setPrefix(String prefix) {
			storeState();
			this.prefix = prefix;
			fullStoreDone = false;
			for(final Entry<String, ColumnInfo> entry : columnInfos.entrySet()) {
				if(!entry.getValue().fake) {
					registerProperties(entry.getKey());
				}
			}
		}
		public void storeState() {
			int[] columnOrder = tableViewer.getTable().getColumnOrder();
			int[] columnIndexToVisualMap = invertedColumnOrderMap(columnOrder);
			if(this.prefix != null) {
				for(Entry<String, ColumnInfo> entry : columnInfos.entrySet()) {
					if(!entry.getValue().fake) {
						storeState(entry.getKey(), columnIndexToVisualMap[entry.getValue().index]);
					}
				}
			}
		}
		
		protected void storeState(String column, int position) {
			ColumnInfo columnInfo = columnInfos.get(column);
			if(columnInfo != null) {
				int width = tableViewer.getTable().getColumn(columnInfo.index).getWidth();
				storeShow(column, width > 0);
				if(width > 0) storeWidth(column, width);
				storePosition(column, position);
			}
		}
		
		public ColumnLayoutData getColumnLayoutData(String column, int defaultWeight, int defaultMinWidth) {
			ColumnInfo columnInfo = columnInfos.get(column);
			if(columnInfo != null) {
				Boolean show = retrieveShow(column);
				Integer width = retrieveWidth(column);
				if(columnInfo.fake || (show != null && !show)) {
					// Explicitly hidden
					return new ColumnPixelData(0);
				}else if(width != null) {
					// Not hidden and width stored
					return new ColumnPixelData(width);
				}else {
					// Shown, but no width set, apply default
					return new ColumnWeightData(defaultWeight, defaultMinWidth);
				}
			}
			return null;
		}
		
		protected int[] invertedColumnOrderMap(int[] columnOrder) {
			// Build the reverse map
			int[] defaultColumnIndexToVisualMap = new int[columnOrder.length];
			for(int visualIndex = 0; visualIndex < columnOrder.length; visualIndex++) {
				defaultColumnIndexToVisualMap[columnOrder[visualIndex]] = visualIndex;
			}
			return defaultColumnIndexToVisualMap;
		}
		
		public int[] getColumnOrder(int[] currentOrder) {
			int[] result = new int[currentOrder.length];
			Arrays.fill(result, -1);
			// Assign the stored positions, collecting columns which have no stored position
			List<ColumnInfo> remainingColumns = new LinkedList<ColumnInfo>();
			for(Entry<String, ColumnInfo> entry : columnInfos.entrySet()) {
				Integer position = retrievePosition(entry.getKey());
				if(position != null) {
					result[position] = entry.getValue().index; 
				}else {
					remainingColumns.add(entry.getValue());
				}
			}
			// Collect available visual indices
			List<Integer> remainingVisualIndices = new LinkedList<Integer>();
			for(int visualIndex = 0; visualIndex < result.length; visualIndex++) {
				if(result[visualIndex] == -1) {
					remainingVisualIndices.add(visualIndex);
				}
			}
			Collections.sort(remainingColumns, new Comparator<ColumnInfo>() {
				@Override
				public int compare(ColumnInfo o1, ColumnInfo o2) {
					return new Integer(o1.index).compareTo(o2.index);
				}
			});
			assert(remainingVisualIndices.size() == remainingColumns.size());
			// Assign remaining columns to remaining visual positions
			for(ColumnInfo column : remainingColumns) {
				result[remainingVisualIndices.remove(0)] = column.index;
			}
			return result;
		}
	}