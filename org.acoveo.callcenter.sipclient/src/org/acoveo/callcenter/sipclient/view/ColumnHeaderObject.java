package org.acoveo.callcenter.sipclient.view;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 
 * @author Thomas Kenner (thomas.kenner@acoveo.com)
 *
 */

public class ColumnHeaderObject implements Comparable<ColumnHeaderObject> {
	/** An index for sorting ColumnHeaderObject in lists
	 * Does NOT specify the order within the table!
	 */
	public int index;
	public String title;
	public String tooltipText;
	public Image icon;
	public int columnWeight;
	public int columnStyle;
	
	public static int COLUMN_MINIMUM_WIDTH = 10;
	
	public ColumnHeaderObject(int index) {
		this.index = index;
		this.columnStyle = SWT.CENTER;
	}
	
	@Override
	public int compareTo(ColumnHeaderObject o) {
		return this.index - o.index;
	}
	
	public static TableViewerColumn createTableColumn(final ColumnHeaderObject colHeaderObj, final TableViewer viewer, TableLayout layout, ColumnLabelProvider labelProvider, SelectionListener listener, ColumnLayoutData layoutData) {
		TableViewerColumn column = new TableViewerColumn(viewer, colHeaderObj.columnStyle, viewer.getTable().getColumnCount());
		column.getColumn().setMoveable(true);

		column.setLabelProvider(labelProvider);
		column.getColumn().addSelectionListener(listener);
		
		if (colHeaderObj.title != null) {
			column.getColumn().setText(colHeaderObj.title);
		}
		
		if (colHeaderObj.tooltipText != null) {
			column.getColumn().setToolTipText(colHeaderObj.tooltipText);
		}
		
		if (colHeaderObj.icon != null) {
			column.getColumn().setImage(colHeaderObj.icon);
		}
		
		// Add listener to column so tasks are sorted by description when clicked 
		column.getColumn().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				((TableViewerSorter) viewer.getSorter()).doSort(colHeaderObj.index);
				viewer.refresh();
			}
		});

		if(layoutData != null) {
			layout.addColumnData(layoutData);
		}
		return column;
	}
	
	public static TableViewerColumn createTableColumn(final ColumnHeaderObject colHeaderObj, final TableViewer viewer, TableLayout layout, ColumnLabelProvider labelProvider, SelectionListener listener) {
		return createTableColumn(colHeaderObj, viewer, layout, labelProvider, listener, new ColumnWeightData(colHeaderObj.columnWeight, COLUMN_MINIMUM_WIDTH));
	}
	
	public static TableColumn createTableColumn(final ColumnHeaderObject colHeaderObj, final TableViewer viewer, TableColumnLayout layout) {
		TableColumn column = new TableColumn(viewer.getTable(), colHeaderObj.columnStyle, viewer.getTable().getColumnCount());
		column.setMoveable(true);

		if (colHeaderObj.title != null)
			column.setText(colHeaderObj.title);
		
		if (colHeaderObj.tooltipText != null)
			column.setToolTipText(colHeaderObj.tooltipText);
		
		if (colHeaderObj.icon != null) {
			//XXX We loose memory here!
			column.setImage(new Image(Display.getCurrent(), colHeaderObj.icon.getImageData()));
		}
		
		// Add listener to column so tasks are sorted by description when clicked 
	  	column.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent event) {
	        	((TableViewerSorter) viewer.getSorter()).doSort(colHeaderObj.index);
	        	viewer.refresh();
	        }
	      });
	  	
	  	column.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(ControlEvent e) {}

			@Override
			public void controlResized(ControlEvent e) {
				TableColumn col = (TableColumn)e.getSource();
				if(col.getWidth() < COLUMN_MINIMUM_WIDTH) {
					col.setWidth(COLUMN_MINIMUM_WIDTH);
				}
				
			}
	  		
	  	});

		layout.setColumnData(column, new ColumnWeightData(colHeaderObj.columnWeight, COLUMN_MINIMUM_WIDTH));
		return column;
	}
}