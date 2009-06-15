package org.acoveo.callcenter.guiLibrary;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;

/** Draws the background of a selected table row
 * 
 * This class is useful, because on swt win32, selected table rows
 * may have a colour which makes text hard to read
 */
public class TableSelectionColorChangeListener implements org.eclipse.swt.widgets.Listener {
	private Table table;
	private boolean enableGradient = true;
	
	public TableSelectionColorChangeListener(TableViewer tableViewer) {
		this.table = tableViewer.getTable();
	}
	
	public TableSelectionColorChangeListener(Table table) {
		this.table = table;
	}
	
	public void enableGradient(boolean enabled) {
		enableGradient = enabled;
	}
	
	public void handleEvent(Event event) {
		Display display = table.getDisplay();
		event.detail &= ~SWT.HOT;	
		if((event.detail & SWT.SELECTED) != 0) {
			GC gc = event.gc;
			Rectangle area = table.getClientArea();
			/*
			 * If you wish to paint the selection beyond the end of
			 * last column, you must change the clipping region.
			 */
			int columnCount = table.getColumnCount();
			if (event.index == columnCount - 1 || columnCount == 0) {
				int width = area.x + area.width - event.x;
				if (width > 0) {
					Region region = new Region();
					gc.getClipping(region);
					region.add(event.x, event.y, width, event.height); 
					gc.setClipping(region);
					region.dispose();
				}
			}
			gc.setAdvanced(true);
			if (gc.getAdvanced()) gc.setAlpha(127);
			Rectangle rect = event.getBounds();
			Color foreground = gc.getForeground();
			Color background = gc.getBackground();
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
			gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			if(enableGradient) {
				gc.fillGradientRectangle(0, rect.y, 500, rect.height, false);
			}
			else {
				gc.fillRectangle(0,rect.y, rect.width, rect.height);
			}
			// restore colors for subsequent drawing
			gc.setForeground(foreground);
			gc.setBackground(background);
			event.detail &= ~SWT.SELECTED;
		}
	}
}
