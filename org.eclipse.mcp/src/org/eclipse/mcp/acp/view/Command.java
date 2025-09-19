package org.eclipse.mcp.acp.view;

public enum Command {
	
	MODEL("org.eclipse.mcp.acp.cmd.model", //$NON-NLS-1$
			"com.ibm.systemz.wcaz4e.actions.ExplainCodeAction"), //$NON-NLS-1$
	MODE("org.eclipse.mcp.acp.cmd.mode", //$NON-NLS-1$
			"com.ibm.systemz.wcaz4e.actions.ExplainCodeAction"); //$NON-NLS-1$

	private final String commandId;
	private final String actionDelegateClass;

	Command(String commandId, String actionDelegateClass) {
		this.commandId = commandId;
		this.actionDelegateClass = actionDelegateClass;
	}
	
	public String getCommandId() {
		return commandId;
	}

	public static Command fromCommand(String commandId) throws IllegalArgumentException {
		for (Command command : values()) {
			if (command.commandId.equals(commandId)) {
				return command;
			}
		}
		throw new IllegalArgumentException();
	}

//	IActionDelegate getDelegate() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
//			InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
//		IActionDelegate delegate = (IActionDelegate) Class.forName(actionDelegateClass).getConstructor().newInstance();
//		if (delegate instanceof BaseWcazAction) {
//			((BaseWcazAction) delegate).setCommand(this);
//		}
//		return delegate;
//	}
}