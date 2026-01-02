/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.agents.chat;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.ContentAssistProvider.ResourceProposal;
import org.eclipse.agents.chat.actions.NewSessionAction;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.chat.controller.IAgentServiceListener;
import org.eclipse.agents.chat.controller.SessionController;
import org.eclipse.agents.chat.controller.StartSessionJob;
import org.eclipse.agents.chat.controller.workspace.WorkspaceChange;
import org.eclipse.agents.chat.toolbar.ToolbarAgentSelector;
import org.eclipse.agents.chat.toolbar.ToolbarModeSelector;
import org.eclipse.agents.chat.toolbar.ToolbarModelSelector;
import org.eclipse.agents.chat.toolbar.ToolbarSessionSelector;
import org.eclipse.agents.chat.toolbar.ToolbarSessionStartStop;
import org.eclipse.agents.contexts.platform.resource.WorkspaceResourceAdapter;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.protocol.AcpSchema.ContentBlock;
import org.eclipse.agents.services.protocol.AcpSchema.TextBlock;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.part.ViewPart;

public class ChatView extends ViewPart implements IAgentServiceListener, TraverseListener, IContentProposalListener, ModifyListener, VerifyListener, Listener  {

	public static final String ID  = "org.eclipse.agents.chat.ChatView"; //$NON-NLS-1$

	Text inputText;
	boolean disposed = false;
	ChatResourceAdditions contexts;
	ChatFileDrawer fileDrawer;
	ChatBrowser browser;

	Composite middle;
	Composite topMiddle;
	boolean listening = true;
	
	ToolbarAgentSelector agentSelector;
    ToolbarModelSelector modelSelector;
    ToolbarModeSelector modeSelector;
    ToolbarSessionSelector sessionSelector;
	ToolbarSessionStartStop startStop;
	
	private IAgentService activeAgent = null;
	private String activeSessionId = null;

	@Override
	public void createPartControl(Composite parent) {
		
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IConsoleHelpContextIds.CONSOLE_VIEW);

		middle = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, true);
		gl.verticalSpacing = 0;
		middle.setLayout(gl);
		middle.setLayoutData(new GridData(GridData.FILL_BOTH));

		browser = new ChatBrowser(middle, SWT.NONE);
		browser.initialize();
		
		contexts = new ChatResourceAdditions(middle, SWT.NONE);
		
		fileDrawer = new ChatFileDrawer(middle);

		inputText = new Text(middle, SWT.MULTI | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.minimumHeight = 60;
		gd.heightHint = 60;
		inputText.setLayoutData(gd);
		inputText.addTraverseListener(this);
		inputText.addModifyListener(this);
		inputText.addVerifyListener(this);
		
		ContentAssistAdapter adapter = new ContentAssistAdapter(inputText);
		adapter.addContentProposalListener(this);
		
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

        // Add your action to the toolbar.
		agentSelector = new ToolbarAgentSelector(this);
	    modelSelector = new ToolbarModelSelector(this);
	    modeSelector = new ToolbarModeSelector(this);
	    sessionSelector = new ToolbarSessionSelector(this);
	    startStop = new ToolbarSessionStartStop(this);
	    
	    toolbarManager.add(agentSelector);
        toolbarManager.add(modelSelector);
        toolbarManager.add(modeSelector);
        toolbarManager.add(sessionSelector);
        toolbarManager.add(startStop);

        // The toolbar will be updated automatically, but you can force an update if needed.
        getViewSite().getActionBars().updateActionBars();
        ChatView acpView = this;
		parent.getDisplay().addFilter(SWT.Traverse, this);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				parent.getDisplay().removeFilter(SWT.Traverse, acpView);
			}
		});
		inputText.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				inputText.removeTraverseListener(acpView);
			}
		});
		
		AgentController.instance().addAgentListener(this);
		SessionController.addChatView(this);
	}

	@Override
	public void setFocus() {
	}
	
	public ChatBrowser getBrowser() {
		return browser;
	}

	@Override
	public void dispose() {
		super.dispose();
		this.disposed = true;
		SessionController.removeChatView(this);
		AgentController.instance().removeAgentListener(this);
		fileDrawer.dispose();
	}

	@Override
	public void keyTraversed(TraverseEvent event) {
		if (event.detail == SWT.TRAVERSE_RETURN && (event.stateMask & SWT.SHIFT) == 0) {
			startPromptTurn();
		}
		
		if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
			event.doit = false;
			browser.setFocus();
		}
	}

	@Override
	public void proposalAccepted(IContentProposal proposal) {
		if (proposal instanceof ResourceProposal) {
			ResourceProposal rp = (ResourceProposal)proposal;
			contexts.addLinkedResourceContext(rp.name, rp.uri);
		}
	}
	
	public void addContext(Object context) {
		if (!(context instanceof IResource) && context instanceof IAdaptable) {
			context = ((IAdaptable)context).getAdapter(IResource.class);
		}

		if (context instanceof IResource) {
			WorkspaceResourceAdapter wra = new WorkspaceResourceAdapter((IResource)context);
			String uri = wra.toUri();
			contexts.addLinkedResourceContext(((IResource)context).getName(), uri);
		}
	}

	public void setActiveAgent(IAgentService agent) {
		if (this.activeAgent != agent) {
			this.activeAgent = agent;
			if (agent.isRunning() && activeSessionId == null) {
				new NewSessionAction(this).run();
			}
			updateEnablement();
		}
	}

	public void agentDisconnected() {
		this.activeSessionId = null;
		updateEnablement();
	}

	@Override
	public void modifyText(ModifyEvent arg0) {
		
	}
	
	public void startPromptTurn() {
		if (this.activeAgent != null) {
			
			String prompt = inputText.getText();
			inputText.setText("");
			inputText.clearSelection();
			
			List<ContentBlock> content = new ArrayList<ContentBlock>();
			content.addAll(contexts.getContextBlocks());
			content.add(new TextBlock(null, null, prompt, "text"));
			
			if (activeSessionId != null) {
				AgentController.getSession(activeSessionId).prompt(content.toArray(ContentBlock[]::new));
			} else {
				StartSessionJob job = new StartSessionJob(
						activeAgent,
						activeAgent.getInitializeResponse(),
						null);
				job.schedule();
				job.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						super.done(event);
						if (event.getResult().isOK()) {
							AgentController.getSession(job.getSessionId()).prompt(content.toArray(ContentBlock[]::new));
						}
					}
					
				});
			}
			
			contexts.clearAcpContexts();
		}
	}
	
	public void stopPromptTurn() {
		if (activeSessionId != null) {
			AgentController.getSession(activeSessionId).stopPromptTurn(activeSessionId);
		}
	}

	public void prompTurnStarted() {
		startStop.prompTurnStarted();
		getViewSite().getActionBars().updateActionBars();
		
		AgentController.getSession(activeSessionId).getWorkspaceController().clearVariants();
	}

	public void prompTurnEnded() {
		startStop.prompTurnEnded();
		getViewSite().getActionBars().updateActionBars();
	}
	
	public void workspaceChangeAdded(WorkspaceChange change) {
		Activator.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				fileDrawer.workspaceChangeAdded(change);
				middle.layout(true);
			}
		});
	}

	public void workspaceChangeModified(WorkspaceChange change) {
		Activator.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				fileDrawer.workspaceChangeModified(change);
				middle.layout(true);
			}
		});
	}
	
	public void workspaceChangeRemoved(WorkspaceChange change) {
		Activator.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				fileDrawer.workspaceChangeRemoved(change);
				middle.layout(true);
			}
		});
	}

	@Override
	public void verifyText(VerifyEvent e) {
	}

	// Handle tabbing from the toolbar. 
	@Override
	public void handleEvent(Event event) {
		if (event.detail == SWT.TRAVERSE_TAB_NEXT) {
			if (event.widget instanceof ToolBar) {
				IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
				if (toolbarManager instanceof ToolBarManager) {
					ToolBar detectedToolbar = (ToolBar) event.widget;
					ToolBarManager manager = (ToolBarManager) toolbarManager;
					ToolBar acpToolbar = manager.getControl();
					if (detectedToolbar.equals(acpToolbar)) {
						event.doit = false;
						browser.setFocus();
					}
				}
			}
		}
	}
	
	public void setActiveSessionId(String sessionId) {
		if (activeSessionId == null || !sessionId.equals(activeSessionId)) {
//			TODO: stopPromptTurn();
			browser.clearContent();
		}

		this.activeSessionId = sessionId;
		updateEnablement();
		
	}
	
	public String getActiveSessionId() {
		return activeSessionId;
	}

	public IAgentService getActiveAgent() {
		return activeAgent;
	}

	@Override
	public void agentStopped(IAgentService service) {
		if (getActiveAgent() == service) {
			this.activeSessionId = null;
			updateEnablement();
		}
	}

	@Override
	public void agentScheduled(IAgentService service) {
		if (activeAgent == service) {
			this.activeSessionId = null;
			updateEnablement();
		}
	}

	@Override
	public void agentStarted(IAgentService service) {
		if (activeAgent == service) {
			this.activeSessionId = null;
			new NewSessionAction(this).run();
			updateEnablement();
		}
	}

	@Override
	public void agentFailed(IAgentService service) {
		if (this.activeAgent == service) {
			this.activeSessionId = null;
			updateEnablement();
		}
	}
	
	private void updateEnablement() {
		Activator.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!disposed) {
					agentSelector.setEnabled(true);
				    sessionSelector.setEnabled(activeAgent != null && activeAgent.isRunning());
					startStop.setEnabled(activeAgent != null && activeAgent.isRunning() && activeSessionId != null);	
					inputText.setEnabled(activeAgent != null && activeAgent.isRunning() && activeSessionId != null);
				}
			}
		});
	}
}
