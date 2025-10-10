package org.eclipse.mcp.acp.view.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.view.AcpView;

public class ToolbarAgentSelector extends AbstractDynamicToolbarDropdown {

	List<ModelAction> actions;
	
	public ToolbarAgentSelector(AcpView view) {
		super("Coding Agent...", "Select a coding agent", view);
		
		actions = new ArrayList<ModelAction>();
		for (IAgentService agent: AcpService.instance().getAgents()) {
			actions.add(new ModelAction(agent));
		}
	}

	@Override
	protected void fillMenu(MenuManager menuManager) {
		for (ModelAction action: actions) {
			menuManager.add(action);
			action.setChecked(action.getAgent() ==  AcpService.instance().getAgentService());
		}
	}

	class ModelAction extends Action {
		IAgentService agent;
		
		public ModelAction(IAgentService agent) {
			super(agent.getName());
			this.agent = agent;
		}

		@Override
		public void run() {
			AcpService.instance().setAcpService(getView(), agent);
			ToolbarAgentSelector.this.updateText(agent.getName());
		}
		
		public IAgentService getAgent() {
			return agent;
		}
	}
}
