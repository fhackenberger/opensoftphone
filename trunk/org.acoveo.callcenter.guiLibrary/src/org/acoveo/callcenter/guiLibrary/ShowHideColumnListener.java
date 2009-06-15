/**
 * 
 */
package org.acoveo.callcenter.guiLibrary;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableColumn;

public class ShowHideColumnListener implements SelectionListener {
	public static final int DEFAULT_COLUMN_WIDTH = 30;

	protected TableViewer viewer;
	protected String column;
	protected int columnIndex;
	protected ColumnShowHideSupport columnSupport;
	protected boolean animationEnabled = true;
	
	protected ShowHideColumnListener(TableViewer viewer, String column, int columnIndex, boolean animationEnabled, ColumnShowHideSupport columnSupport) {
		this.viewer = viewer;
		this.column = column;
		this.columnIndex = columnIndex;
		this.animationEnabled = animationEnabled;
		this.columnSupport = columnSupport;
	}
	
	public boolean isAnimationEnabled() {
		return animationEnabled;
	}

	public void setAnimationEnabled(boolean animationEnabled) {
		this.animationEnabled = animationEnabled;
	}
	
	private class WidthAnimateThread extends Thread {
		private int width = 0;
		private int currentWidth = 0;
		private TableColumn tableColumn;
		private boolean hide = false;

		public WidthAnimateThread(int width, TableColumn tableColumn, boolean hide) {
			super();
			this.width = width;
			this.tableColumn = tableColumn;
			this.hide = hide;
		}

		public void run() {
			tableColumn.getDisplay().syncExec(new Runnable() {
				public void run() {
					currentWidth = tableColumn.getWidth();
					if(hide) {
						if(currentWidth <= 0) {
							currentWidth = DEFAULT_COLUMN_WIDTH;
						}
						columnSupport.storeWidth(column, currentWidth);
						columnSupport.storeShow(column, false);
					}else {
						columnSupport.storeShow(column, true);
					}
				}
			});
			final int targetWidth = hide ? 0 : width;
			float increment = (targetWidth - currentWidth) / 10.0f;
			float current = currentWidth;
			if(animationEnabled) {
				for (int i = 0; i < 9; i++) {
					final int target = (int)(current + increment);
					current = current + increment;
					tableColumn.getDisplay().syncExec(new Runnable() {
						public void run() {
							tableColumn.setWidth(target);
						}
					});
				}
			}
			tableColumn.getDisplay().syncExec(new Runnable() {
				public void run() {
					tableColumn.setWidth(targetWidth);
				}
			});
		}
	};

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		boolean showColumn = ((MenuItem)e.getSource()).getSelection();
		TableColumn tableColumn = viewer.getTable().getColumn(columnIndex);
		if(showColumn) {
			Integer targetWidth = columnSupport.retrieveWidth(column);
			WidthAnimateThread t = new WidthAnimateThread(targetWidth != null ? targetWidth : DEFAULT_COLUMN_WIDTH, tableColumn, false);
			t.start();
		}else {
			WidthAnimateThread t = new WidthAnimateThread(0, tableColumn, true);
			t.start();
		}
	}
}