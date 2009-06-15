package org.acoveo.callcenter.guiLibrary;

import org.eclipse.jface.viewers.StructuredViewer;

public interface ITableColumnComparator {
	public void doSort(StructuredViewer viewer, String column);
}
