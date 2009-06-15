package org.acoveo.callcenter.guiLibrary;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * A wrapper for the TableColumnSorterListener that takes enums.
 * 
 * @author Thomas Kenner (thomas.kenner@acoveo.com)
 */
public class EnumTableColumnSorterListener implements SelectionListener {
	Enum<?> column;
	StructuredViewer viewer;
	public EnumTableColumnSorterListener(Enum<?> column, StructuredViewer viewer) {
		this.column = column;
		this.viewer = viewer;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		((IEnumTableColumnComparator)viewer.getComparator()).doSort(viewer, column);
		viewer.refresh();
	}
}
