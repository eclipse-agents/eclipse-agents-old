package org.eclipse.agents.chat;

import org.eclipse.agents.Activator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class EnableMCPDialog extends Dialog {

	Button askMeButton;
	
	public EnableMCPDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        
        GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);

		
		Label instructions = new Label(container, SWT.WRAP);
		instructions.setText("The Eclipse IDE's 'Agent Contexts' preferences can expose tools, resources and prompts to coding agents using an MCP server over HTTP.  These services enable an agent to do things read compilation errors, save editors and more.");
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(80);
		gd.horizontalSpan = 4;
		instructions.setLayoutData(gd);

		Link link = new Link(parent, SWT.NONE);
		link.setText("To enable these MCP services, click <a>Agent Contexts</a>.");
		link.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				PreferencesUtil.createPreferenceDialogOn(Activator.getDisplay().getActiveShell(), "org.eclipse.agents.preferences.contexts", //$NON-NLS-1$
						null, null).open();
			}
		});
		
		askMeButton = new Button(parent, SWT.CHECK);
		askMeButton.setText("Do not show this message again");
		
        return container;
	}
	
	protected void addCustomSection(Composite container) {
		
	}
}
