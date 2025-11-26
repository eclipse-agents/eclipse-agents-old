package org.eclipse.agents.chat.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.ChatView;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.chat.controller.InitializeAgentJob;
import org.eclipse.agents.chat.toolbar.ToolbarAgentSelector;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;

public class SelectAgentAction extends Action {
	
	private static List<InitializeAgentJob> initializeJobs = new ArrayList<InitializeAgentJob>();
	
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
		
//		this.activeAgent = agent;
		if (!agent.isRunning()) {
		
			view.agentDisconnected();
			agent.stop();
			
			for (InitializeAgentJob job: initializeJobs) {
				job.cancel();
			}
			initializeJobs.clear();
			
			final InitializeAgentJob fJob = new InitializeAgentJob(agent);
			initializeJobs.add(fJob);
           
			fJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					if (event.getJob().getResult().isOK()) {
						view.agentConnected(agent);
						AgentController.instance().clientRequests(agent.getInitializeRequest());
						AgentController.instance().agentResponds(agent.getInitializeResponse());
							
					} else {
						Tracer.trace().trace(Tracer.CHAT, "initialization job has an error");
						Tracer.trace().trace(Tracer.CHAT, event.getJob().getResult().getMessage(), event.getJob().getResult().getException());
						if (event.getJob().getResult().getException() != null) {
							event.getJob().getResult().getException().printStackTrace();
						}
					}
				}
			});
			fJob.schedule();
		}
	}
	
	public IAgentService getAgent() {
		return agent;
	}
}
