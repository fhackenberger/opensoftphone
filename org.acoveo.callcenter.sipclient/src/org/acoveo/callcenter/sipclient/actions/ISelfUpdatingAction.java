package org.acoveo.callcenter.sipclient.actions;

import org.eclipse.jface.action.IAction;

public interface ISelfUpdatingAction extends IAction {
    /**
     * Refreshes the action.
     */
    public void update();

    public boolean shouldBeVisible();
}
