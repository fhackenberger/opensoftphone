package org.acoveo.callcenter.guiLibrary;

import org.eclipse.jface.viewers.StructuredViewer;

public interface IEnumTableColumnComparator {
	public void doSort(StructuredViewer viewer, Enum<?> column);
}
