package org.acoveo.callcenter.sipclient.view.shortcutbutton;

import org.acoveo.callcenter.sipclient.actions.ShortcutButtonAction;
import org.acoveo.callcenter.sipclient.preferences.shortcutbuttons.ShortcutButton;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ShortcutButtonPanel {
	private ShortcutButton button;
	private Composite c;
	private ActionContributionItem scbAction;
	
	public ShortcutButtonPanel(Composite parent, ShortcutButton button) {
		this.button = button;
		
		createContent(parent);
	}

	private void createContent(Composite parent) {
		c = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).indent(0, 0).applyTo(c);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).margins(0, 0).applyTo(c);
		
		scbAction = new ActionContributionItem(
				new ShortcutButtonAction(button.getNumber(), button.getName(),
						button.isForward(), button.isSuffixDialing()));
		scbAction.fill(c);
		Label label = new Label(c, SWT.NONE);
		label.setText(((ShortcutButtonAction)scbAction.getAction()).getCaption());
	}
	
	public void update() {
		scbAction.update();
	}
	
	public void dispose() {
		scbAction.getWidget().dispose();
		c.dispose();
	}

}
