package org.eclipse.mcp.acp.view;

import org.eclipse.mcp.acp.AcpService;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;

public class AcpChatFactory implements IConsoleFactory {

	@Override
	public void openConsole() {
		AcpConsole console = new AcpConsole();
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
		AcpService.instance().setAcpService(AcpService.instance().getAgents()[0]);
		
	}

}
