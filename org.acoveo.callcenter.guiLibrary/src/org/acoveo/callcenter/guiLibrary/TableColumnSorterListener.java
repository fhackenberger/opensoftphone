package org.acoveo.callcenter.guiLibrary;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class TableColumnSorterListener implements SelectionListener {
	String column;
	StructuredViewer viewer;
	public TableColumnSorterListener(String column, StructuredViewer viewer) {
		this.column = column;
		this.viewer = viewer;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		((ITableColumnComparator)viewer.getComparator()).doSort(viewer, column);
		viewer.refresh();
	}

}
