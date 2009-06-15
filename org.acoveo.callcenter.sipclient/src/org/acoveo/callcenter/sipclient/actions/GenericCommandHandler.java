package org.acoveo.callcenter.sipclient.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class GenericCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		Command command = arg0.getCommand();
		String commandId = command.getId();
		org.eclipse.jface.action.IAction action = null;
		if(commandId.equals("org.acoveo.callcenter.sipclient.commandClearDestination")) { //$NON-NLS-1$
			action = new ClearDestinationAction();
		}else if(commandId.equals("org.acoveo.callcenter.sipclient.commandBackspaceDestination")) { //$NON-NLS-1$
			action = new BackspaceDestinationAction();
		}else if(commandId.equals("org.acoveo.callcenter.sipclient.commandCallAnswer")) { //$NON-NLS-1$
			action = new CallAnswerAction();
		}else if(commandId.equals("org.acoveo.callcenter.sipclient.commandHangup")) { //$NON-NLS-1$
			action = new HangupAction();
		}else if(commandId.equals("org.acoveo.callcenter.sipclient.commandDial")) { //$NON-NLS-1$
			String dialString = arg0.getParameter("org.acoveo.callcenter.sipclient.commandParameterDialString"); //$NON-NLS-1$
			if(dialString != null) {
				action = new DialAction(dialString);
			}else {
				throw new ExecutionException("dialString parameter missing"); //$NON-NLS-1$
			}
		}
		if(action != null) {
			action.run();
		}else {
			throw new ExecutionException("No matching Action for command " + commandId + " found"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

}
