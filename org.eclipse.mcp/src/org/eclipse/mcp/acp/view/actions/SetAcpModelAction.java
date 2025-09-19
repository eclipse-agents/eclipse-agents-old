package org.eclipse.mcp.acp.view.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.agent.IAgentService;

public class SetAcpModelAction extends Action {

	private IAgentService service;
	
	public SetAcpModelAction(IAgentService service) {
		super(service.getName());
		this.service = service;
	}

	@Override
	public void run() {
		AcpService.instance().setAcpService(service);
	}
	
	

}
