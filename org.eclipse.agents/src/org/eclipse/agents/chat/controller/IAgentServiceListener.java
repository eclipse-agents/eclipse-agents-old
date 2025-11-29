package org.eclipse.agents.chat.controller;

import org.eclipse.agents.services.agent.IAgentService;

public interface IAgentServiceListener {

	public void agentStopped(IAgentService service);
	public void agentScheduled(IAgentService service);
	public void agentStarted(IAgentService service);
	public void agentFailed(IAgentService service);
}
