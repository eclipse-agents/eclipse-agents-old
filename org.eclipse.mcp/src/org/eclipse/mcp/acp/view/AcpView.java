package org.eclipse.mcp.acp.view;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.protocol.AcpSchema.ContentBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.TextBlock;
import org.eclipse.mcp.acp.view.ContentAssistProvider.ResourceProposal;
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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;



public class AcpView extends PageBookView implements IConsoleView, IPropertyChangeListener, ModifyListener, TraverseListener, IContentProposalListener  {

	public static final String ID  = "org.eclipse.mcp.acp.view.AcpView"; //$NON-NLS-1$

	Text inputText;
	boolean disposed = false;
	AcpConsole console;
	AcpContexts contexts;
	
	boolean scrolllock, wordwrap, pinned;
	IOConsoleOutputStream outputStream;
	IOConsoleOutputStream traceStream;
	IOConsoleOutputStream errorStream;
	
	PageRec rec = null;
	
	Composite middle;
	Composite topMiddle;
	Combo model, mode;
	boolean listening = true;

	@Override
	public void createPartControl(Composite parent) {
		
		console = new AcpConsole();
		
//		createActions();
		IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		tbm.add(new Separator(IConsoleConstants.LAUNCH_GROUP));
		tbm.add(new Separator(IConsoleConstants.OUTPUT_GROUP));
		getViewSite().getActionBars().updateActionBars();
		
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IConsoleHelpContextIds.CONSOLE_VIEW);

		middle = new Composite(parent, SWT.NONE);
		middle.setLayout(new GridLayout(1, true));
		middle.setLayoutData(new GridData(GridData.FILL_BOTH));

		super.createPartControl(middle);
		
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
		}
		model.addModifyListener(this);
		
		mode = new Combo(bottom, SWT.READ_ONLY);
		mode.addModifyListener(this);
		
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
//		site.getActionBars().getToolBarManager().add(new );
	}

	@Override
	public void dispose() {
		super.dispose();
		this.disposed = true;
//		AcpService.instance().removeAcpListener(this);
	}

	@Override
	public boolean getAutoScrollLock() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAutoScrollLock(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void display(IConsole arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IConsole getConsole() {
		return console;
	}

	@Override
	public boolean getScrollLock() {
		return scrolllock;
	}

	@Override
	public boolean getWordWrap() {
		return wordwrap;
	}

	@Override
	public boolean isPinned() {
		// TODO Auto-generated method stub
		return pinned;
	}

	@Override
	public void pin(IConsole arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	@Override
	public void setScrollLock(boolean scrolllock) {
		this.scrolllock = scrolllock;
	}

	@Override
	public void setWordWrap(boolean wordwrap) {
		this.wordwrap = wordwrap;
	}

	@Override
	public void warnOfContentChange(IConsole arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected IPage createDefaultPage(PageBook pageBook) {
		PageRec rec = doCreatePage(this);
		return rec.page;
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		if (rec == null) {
			final IConsole console = getConsole();
			final IPageBookViewPage page = console.createPage(this);
			initPage(page);
			page.createControl(middle);
			page.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
			console.addPropertyChangeListener(this);
			rec = new PageRec(part, page);
		}
		return rec;
		
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
		IPage page = rec.page;
		page.dispose();
		rec.dispose();
		getConsole().removePropertyChangeListener(this);
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		return this;
	}

	@Override
	protected boolean isImportant(IWorkbenchPart arg0) {
		return true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modifyText(ModifyEvent e) {
		if (!listening) {
			return;
		}
		
		if (e.getSource() == model) {
			AcpService.instance().setAcpService(AcpService.instance().getAgents()[model.getSelectionIndex()]);
		} else if (e.getSource() == mode) {
			
		}
	}

	@Override
	public void keyTraversed(TraverseEvent event) {
		if (event.detail == SWT.TRAVERSE_RETURN && (event.stateMask & SWT.SHIFT) != 0) {
			
			String sessionId = AcpService.instance().getSessionId();
			String prompt = inputText.getText();
			inputText.setText("");
			inputText.clearSelection();
			
			List<ContentBlock> content = new ArrayList<ContentBlock>();
			content.addAll(contexts.getContextBlocks());
			content.add(new TextBlock(null, null, prompt, "text"));
			
			AcpService.instance().prompt(content.toArray(ContentBlock[]::new));
			
			contexts.clearAcpContexts();
		}
	}

	@Override
	public void proposalAccepted(IContentProposal proposal) {
		if (proposal instanceof ResourceProposal) {
			ResourceProposal rp = (ResourceProposal)proposal;
			contexts.addResourceContext(rp.name, rp.uri);
		}
	}
}
