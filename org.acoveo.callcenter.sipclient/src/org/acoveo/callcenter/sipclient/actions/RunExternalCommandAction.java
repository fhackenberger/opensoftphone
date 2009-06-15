package org.acoveo.callcenter.sipclient.actions;

import java.io.IOException;
import java.util.List;

import org.acoveo.callcenter.sipclient.Activator;
import org.eclipse.jface.action.Action;

public class RunExternalCommandAction extends Action {
	protected String tooltip = "Run external command";
	protected String text = "run";
	protected ProcessBuilder processBuilder;
	
	public RunExternalCommandAction(String tooltip, String buttonText, List<String> commandAndArgs ) {
		super(buttonText, AS_PUSH_BUTTON);
		this.text = buttonText;
		this.tooltip = tooltip;
		if(commandAndArgs != null && !commandAndArgs.isEmpty()) {
			this.processBuilder = new ProcessBuilder(commandAndArgs);
		}
		setToolTipText(tooltip);
		setEnabled(false);
	}
	
	@Override
	public boolean isEnabled() {
		if(processBuilder != null) {
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		super.run();
		
		if(!isEnabled()) {
			notifyResult(false);
			return;
		}
		try {
			processBuilder.start();
			notifyResult(true);
		} catch (IOException e) {
			Activator.getLogger().error("Executing \"" + processBuilder.command() + "\" failed.", e);
			notifyResult(false);
		}
	}
	
	public void setCommandAndArgs(List<String> commandAndArgs) {
		if(commandAndArgs != null && !commandAndArgs.isEmpty()) {
			this.processBuilder = new ProcessBuilder(commandAndArgs);
			setEnabled(isEnabled());
		}
	}
}

