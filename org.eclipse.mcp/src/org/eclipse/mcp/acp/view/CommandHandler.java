package org.eclipse.mcp.acp.view;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.mcp.acp.view.actions.AddToChatAction;

/**
* Handles command invocations and routes to 
*/
public class CommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.err.println(event);
		if (AddToChatAction.ID.equals(event.getCommand().getId())) {
			new AddToChatAction(event).run();
		}
		return null;
	}
}
