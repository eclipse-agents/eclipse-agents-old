package org.eclipse.agents.chat.actions;

import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.ChatView;
import org.eclipse.agents.chat.toolbar.ToolbarAgentSelector;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.jface.action.Action;

public class SelectAgentAction extends Action {
	
	ChatView view;
	IAgentService agent;
	ToolbarAgentSelector selector;
	
	public SelectAgentAction(ChatView view, IAgentService agent, 
			ToolbarAgentSelector selector) {

		super(agent.getName());
		this.view = view;
		this.agent = agent;
		this.selector = selector;
	}

	@Override
	public void run() {
		Tracer.trace().trace(Tracer.CHAT, "agent selected: " + agent.getName()); //$NON-NLS-1$
		
		selector.updateText(agent.getName());
		view.setActiveAgent(agent);
		this.agent.schedule();
	}
	
	public IAgentService getAgent() {
		return agent;
	}
}
