package org.eclipse.mcp.acp.view;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.protocol.AcpSchema.ContentBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.TextBlock;
import org.eclipse.mcp.acp.view.ContentAssistProvider.ResourceProposal;
import org.eclipse.mcp.acp.view.toolbar.ToolbarAgentSelector;
import org.eclipse.mcp.acp.view.toolbar.ToolbarModeSelector;
import org.eclipse.mcp.acp.view.toolbar.ToolbarModelSelector;
import org.eclipse.mcp.acp.view.toolbar.ToolbarSessionSelector;
import org.eclipse.mcp.platform.resource.WorkspaceResourceAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;



public class AcpView extends ViewPart implements ModifyListener, TraverseListener, IContentProposalListener  {

	public static final String ID  = "org.eclipse.mcp.acp.view.AcpView"; //$NON-NLS-1$

	Text inputText;
	boolean disposed = false;
	AcpContexts contexts;
	AcpBrowser browser;
	String activeSessionId;

	Composite middle;
	Composite topMiddle;
	Combo model, mode;
	boolean listening = true;

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
		
		ContentAssistAdapter adapter = new ContentAssistAdapter(inputText);
		adapter.addContentProposalListener(this);
		
		Composite bottom = new Composite(middle, SWT.NONE);
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		bottom.setLayout(new GridLayout(2, false));
		
		model = new Combo(bottom, SWT.READ_ONLY);
		for (IAgentService service: AcpService.instance().getAgents()) {
			model.add(service.getName());
			if (service == AcpService.instance().getAgentService()) {
				model.select(model.getItemCount() - 1);
				
				if (AcpService.instance().getActiveSessionId() != null) {
					AcpSessionModel sessionModel = AcpService.instance().getActiveSession();
					sessionModel.setBrowser(browser);
				}
			}
		}
		model.addModifyListener(this);
		
		mode = new Combo(bottom, SWT.READ_ONLY);
		mode.addModifyListener(this);
		
		getViewSite().getActionBars().getToolBarManager();
		
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

        // Add your action to the toolbar.
        toolbarManager.add(new ToolbarAgentSelector(this));
        toolbarManager.add(new ToolbarModelSelector(this));
        toolbarManager.add(new ToolbarModeSelector(this));
        toolbarManager.add(new ToolbarSessionSelector(this));

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
//		AcpService.instance().removeAcpListener(this);
	}


	@Override
	public void modifyText(ModifyEvent e) {
		if (!listening) {
			return;
		}
		
		if (e.getSource() == model) {
			AcpService.instance().setAcpService(this, AcpService.instance().getAgents()[model.getSelectionIndex()]);
		} else if (e.getSource() == mode) {
			
		}
	}

	@Override
	public void keyTraversed(TraverseEvent event) {
		if (event.detail == SWT.TRAVERSE_RETURN && (event.stateMask & SWT.SHIFT) != 0) {
			
			String sessionId = AcpService.instance().getActiveSessionId();
			if (sessionId != null) {
				String prompt = inputText.getText();
				inputText.setText("");
				inputText.clearSelection();
				
				List<ContentBlock> content = new ArrayList<ContentBlock>();
				content.addAll(contexts.getContextBlocks());
				content.add(new TextBlock(null, null, prompt, "text"));
				
				AcpService.instance().prompt(sessionId, content.toArray(ContentBlock[]::new));
				
				contexts.clearAcpContexts();
			}
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
}
