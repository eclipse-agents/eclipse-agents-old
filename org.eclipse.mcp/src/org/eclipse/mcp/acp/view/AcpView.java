package org.eclipse.mcp.acp.view;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;


public class AcpView extends ViewPart implements KeyListener  {

	public static final String ID  = "com.ibm.systemz.wcaz4e.explanation.CodeExplanationView"; //$NON-NLS-1$


	StyledText outputText;
	Text inputText;
	boolean disposed = false;

	@Override
	public void createPartControl(Composite parent) {
		Composite middle = new Composite(parent, SWT.NONE);
		middle.setLayout(new GridLayout(1, true));
		middle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		outputText = new StyledText(middle, SWT.NONE);
		outputText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH));
		outputText.setEditable(false);;
		
		inputText = new Text(middle, SWT.MULTI);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		//TODO
		gd.heightHint = 60;
		inputText.setLayoutData(gd);
		inputText.addKeyListener(this);
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
}
