package org.eclipse.mcp.acp.view;


import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.internal.console.IConsoleHelpContextIds;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;


public class AcpView extends PageBookView implements IConsoleView, KeyListener, IPropertyChangeListener  {

	public static final String ID  = "com.ibm.systemz.wcaz4e.explanation.CodeExplanationView"; //$NON-NLS-1$


	StyledText outputText;
	Text inputText;
	boolean disposed = false;
	
	boolean scrolllock, wordwrap, pinned;
	AcpConsole console;
	PageRec rec = null;
	
	Composite middle;
	Composite topMiddle;

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
		
		inputText = new Text(middle, SWT.MULTI | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.minimumHeight = 60;
		gd.heightHint = 60;
		inputText.setLayoutData(gd);
		inputText.addKeyListener(this);
		inputText.setText("Hello");
		
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
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
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
		console.removePropertyChangeListener(this);
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
}
