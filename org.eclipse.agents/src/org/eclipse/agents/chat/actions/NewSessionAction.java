package org.eclipse.agents.chat.actions;

import org.eclipse.agents.Activator;
import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.ChatView;
import org.eclipse.agents.chat.controller.StartSessionJob;
import org.eclipse.agents.chat.toolbar.ToolbarAgentSelector;
import org.eclipse.agents.chat.toolbar.ToolbarSessionSelector;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;

public class NewSessionAction extends Action {
	
	ChatView view;
	ToolbarSessionSelector selector;
	
	public NewSessionAction(ChatView view, ToolbarSessionSelector selector) {

		super("New Session...");
		this.view = view;
		this.selector = selector;
	}

	@Override
	public void run() {
		IAgentService agent = view.getActiveAgent();
		
		if (agent == null) {
			Tracer.trace().trace(Tracer.CHAT, "New Session: Agent is null");
		} else {
			Tracer.trace().trace(Tracer.CHAT, "New Session: " + agent.getName()); //$NON-NLS-1$
			if (agent.isRunning()) {
			
				final StartSessionJob fJob = new StartSessionJob(agent, agent.getInitializeResponse(), null);
				fJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						if (event.getJob().getResult().isOK()) {
							Activator.getDisplay().asyncExec(new Thread() {
								public void run() {
									view.setActiveSessionId(fJob.getSessionId());
									selector.updateText(fJob.getSessionId());
								}
							});
						} else {
							Tracer.trace().trace(Tracer.CHAT, "StartSessionJob failed", fJob.getResult().getException());
						}
					}
				});
			} else {
				Tracer.trace().trace(Tracer.CHAT, "New Session: Agent is not running");
			}
		}
 	}
}
