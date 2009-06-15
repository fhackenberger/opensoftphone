package org.acoveo.callcenter.sipclient.view.shortcutbutton;

import java.util.ArrayList;
import java.util.List;

import org.acoveo.callcenter.sipclient.preferences.shortcutbuttons.ShortcutButton;
import org.acoveo.callcenter.sipclient.preferences.shortcutbuttons.ShortcutButtonGroup;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This class creates a Tabitem for a group of buttons and calls for each button the panel class.
 * @author brandner
 *
 */
public class ShortcutButtonTabItem implements Comparable<ShortcutButtonTabItem> {
	private ShortcutButtonGroup group;
	private List<ShortcutButtonPanel> panels;
	private List<Composite> composites;
	private CTabItem tabItem;
	private int columnCount;
	
	public ShortcutButtonTabItem(CTabFolder parent, int style, ShortcutButtonGroup group) {
		tabItem = new CTabItem(parent, style);
		this.group = group;
		this.columnCount = group.getColumnCount();
		
		createContent(parent);
	}

	public ShortcutButtonTabItem(CTabFolder parent, int style, int index, ShortcutButtonGroup group) {
		tabItem = new CTabItem(parent, style, index);
		this.group = group;
		this.columnCount = group.getColumnCount();
		
		createContent(parent);
	}
	
	
	private void createContent(CTabFolder parent) {
		panels = new ArrayList<ShortcutButtonPanel>();

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		
		Composite c = new Composite(sc, SWT.NONE);
		
		boolean activeState = true;
		List<ShortcutButton> list = group.getShortcutButtons(activeState);
		int size = list.size();
		if(size <= 0) {
			return;
		}

		tabItem.setControl(sc);
		tabItem.setText(group.getName());

		GridLayoutFactory.swtDefaults().numColumns(columnCount)
			.equalWidth(true).margins(0, 0).spacing(0, 0).applyTo(c);
		GridDataFactory.swtDefaults().grab(true, true).applyTo(c);

		composites = new ArrayList<Composite>(columnCount);
		for(int i=0; i<columnCount; i++) {
			Composite composite = new Composite(c,SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT .BEGINNING).grab(true, true).applyTo(composite);
			GridLayoutFactory.swtDefaults().margins(1, 0).spacing(0, 0).applyTo(composite);
			composites.add(composite);
		}
		
		for(int i=0; i<size; i++) {
			Composite cToUse = getComposite(size, i);
			if(cToUse != null) {
				ShortcutButtonPanel panel = new ShortcutButtonPanel(cToUse,
						list.get(i));
				panels.add(panel);
			}
		}
		
		Point pt = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		c.setSize(pt);
		sc.setContent(c);
	}
	
	/**
	 * Returns the correct Composite
	 * @param size Size of the Button-List
	 * @param pos Arrayindex from Button-List
	 * @return
	 */
	private Composite getComposite(int size, int pos) {
		int minColSize = (int) size / columnCount;
		int remainder = (int) size % columnCount;
		int start = 0;
		int stop = 0;

		for(int i=0; i<columnCount && i<size; i++) {
			start = stop;
			stop = start + minColSize;
			if(remainder > 0) {
				stop++;
				remainder--;
			}
			if(start <= pos && pos < stop) {
				return composites.get(i);
			}
		}
		return null;
	}
	
	public CTabItem getTabItem() {
		return tabItem;
	}
	
	/**
	 * Call the update on every shortcut button in this group
	 */
	public void update() {
		for(ShortcutButtonPanel scbp: panels) {
			scbp.update();
		}
	}
	
	/**
	 * Dispose the tabitem an the control
	 */
	public void dispose() {
		Control c = tabItem.getControl();
		tabItem.dispose();
		if(c != null) {
			c.dispose();
			c = null;
		}
		tabItem = null;
	}
	
	public ShortcutButtonGroup getGroup() {
		return group;
	}
	
	public String getGroupName() {
		return group.getName();
	}

	/**
	 * Compares the ShortcutButtonGroups of two tab items.
	 */
	@Override
	public int compareTo(ShortcutButtonTabItem o) {
		return getGroup().compareTo(o.getGroup());
	}

}
