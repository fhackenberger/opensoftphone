package org.acoveo.callcenter.guiLibrary;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.TableViewer;

/**
 * A wrapper for the ColumnShowHideSupport that takes enums.
 * 
 * @author Thomas Kenner (thomas.kenner@acoveo.com)
 */
public class EnumColumnShowHideSupport extends ColumnShowHideSupport {

	public EnumColumnShowHideSupport(TableViewer tableViewer, String prefix, Preferences pref, boolean animationEnabled) {
		super(tableViewer, prefix, pref, animationEnabled);
	}

	public void addDefaultColumn(Enum<?> column, String columnName) {
		super.addDefaultColumn(column.toString(), columnName, column.ordinal());
	}

	public void addDefaultColumn(Enum<?> column) {
		super.addDefaultColumn(column.toString(), column.ordinal());
	}
	
	public void addColumn(Enum<?> column, String menuText) {
		super.addColumn(column.toString(), menuText, column.ordinal());
	}
	
	public void addDummyColumn(Enum<?> column) {
		super.addDummyColumn(column.toString(), column.ordinal());
	}
	
	public ColumnLayoutData getColumnLayoutData(Enum<?> column, int defaultWeight, int defaultMinWidth) {
		return super.getColumnLayoutData(column.toString(), defaultWeight, defaultMinWidth);
	}
}
