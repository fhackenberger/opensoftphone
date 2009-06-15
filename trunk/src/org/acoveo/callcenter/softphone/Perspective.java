package org.acoveo.callcenter.softphone;

import org.acoveo.callcenter.sipclient.view.SoftPhoneViewPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		layout.addStandaloneView(SoftPhoneViewPart.ID, false, IPageLayout.TOP, 1.0f, editorArea);
	}
}
