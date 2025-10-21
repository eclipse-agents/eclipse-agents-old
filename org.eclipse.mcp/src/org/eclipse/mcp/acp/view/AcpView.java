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
package org.eclipse.mcp.acp.view;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.agent.GeminiService;
import org.eclipse.mcp.acp.protocol.AcpSchema.ContentBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.TextBlock;
import org.eclipse.mcp.acp.view.ContentAssistProvider.ResourceProposal;
import org.eclipse.mcp.acp.view.toolbar.ToolbarAgentSelector;
import org.eclipse.mcp.acp.view.toolbar.ToolbarModeSelector;
import org.eclipse.mcp.acp.view.toolbar.ToolbarModelSelector;
import org.eclipse.mcp.acp.view.toolbar.ToolbarSessionSelector;
import org.eclipse.mcp.acp.view.toolbar.ToolbarSessionStartStop;
import org.eclipse.mcp.platform.resource.WorkspaceResourceAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;



public class AcpView extends ViewPart implements TraverseListener, IContentProposalListener, ModifyListener  {

	public static final String ID  = "org.eclipse.mcp.acp.view.AcpView"; //$NON-NLS-1$

	Text inputText;
	boolean disposed = false;
	AcpContexts contexts;
	AcpBrowser browser;
	String activeSessionId;

	Composite middle;
	Composite topMiddle;
	boolean listening = true;
	boolean agentConnected = false;
	
	ToolbarSessionStartStop startStop;

	@Override
	public void createPartControl(Composite parent) {
		
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IConsoleHelpContextIds.CONSOLE_VIEW);

		middle = new Composite(parent, SWT.NONE);
		middle.setLayout(new GridLayout(1, true));
		middle.setLayoutData(new GridData(GridData.FILL_BOTH));

		browser = new AcpBrowser(middle, SWT.NONE);
		browser.initialize();
		
		contexts = new AcpContexts(middle, SWT.NONE);

		inputText = new Text(middle, SWT.MULTI | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.minimumHeight = 60;
		gd.heightHint = 60;
		inputText.setLayoutData(gd);
		inputText.addTraverseListener(this);
		inputText.addModifyListener(this);
		
		ContentAssistAdapter adapter = new ContentAssistAdapter(inputText);
		adapter.addContentProposalListener(this);
		
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

        // Add your action to the toolbar.
        toolbarManager.add(new ToolbarAgentSelector(this));
        toolbarManager.add(new ToolbarModelSelector(this));
        toolbarManager.add(new ToolbarModeSelector(this));
        toolbarManager.add(new ToolbarSessionSelector(this));
        
        startStop = new ToolbarSessionStartStop(this);
        toolbarManager.add(startStop);

        // The toolbar will be updated automatically, but you can force an update if needed.
        getViewSite().getActionBars().updateActionBars();
		
	}

	@Override
	public void setFocus() {

	}
	
	public AcpBrowser getBrowser() {
		return browser;
	}

	@Override
	public void dispose() {
		super.dispose();
		this.disposed = true;
	}

	@Override
	public void keyTraversed(TraverseEvent event) {
		if (event.detail == SWT.TRAVERSE_RETURN && (event.stateMask & SWT.SHIFT) == 0) {
			startPromptTurn();
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

	public void agentConnected() {
		agentConnected = true;
		startStop.setEnabled(true);
	}

	public void agentDisconnected() {
		agentConnected = false;
		startStop.setEnabled(false);
	}

	@Override
	public void modifyText(ModifyEvent arg0) {
		
	}
	
	public void startPromptTurn() {
		activeSessionId = AcpService.instance().getActiveSessionId();
		if (agentConnected && activeSessionId != null) {
			String prompt = inputText.getText();
			inputText.setText("");
			inputText.clearSelection();
			
			List<ContentBlock> content = new ArrayList<ContentBlock>();
			content.addAll(contexts.getContextBlocks());
			content.add(new TextBlock(null, null, prompt, "text"));
			
			AcpService.instance().prompt(activeSessionId, content.toArray(ContentBlock[]::new));
			
			contexts.clearAcpContexts();
		}
	}
	
	public void stopPromptTurn() {
		AcpService.instance().stopPromptTurn(activeSessionId);
	}

	public void prompTurnStarted() {
		startStop.prompTurnStarted();
		getViewSite().getActionBars().updateActionBars();
	}

	public void prompTurnEnded() {
		startStop.prompTurnEnded();
		getViewSite().getActionBars().updateActionBars();
	}
}
