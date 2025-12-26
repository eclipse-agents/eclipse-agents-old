package org.eclipse.agents.chat;


import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.controller.workspace.WorkspaceChange;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;

public class ChatExpandBar implements ExpandListener {
	
	ExpandBar expandBar = null;
	ChatFileDrawer fileDrawer;
	
	
	public ChatExpandBar(Composite parent) {
		expandBar = new ExpandBar(parent, SWT.V_SCROLL);
		expandBar.addExpandListener(this);
		expandBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fileDrawer = new ChatFileDrawer(expandBar, 0);
		
		expandBar.addListener(SWT.Resize, event -> Activator.getDisplay().asyncExec(() -> {
			resize();
		}));
		
		expandBar.addExpandListener(this);
		expandBar.setSpacing(8);
		updateVisibility();
		
	}
	
//	public ChatExpandBar(Composite parent, int two) {
//		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
//		ScrolledForm form = toolkit.createScrolledForm(parent);
//		form.getBody().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		form.getBody().setLayout(new GridLayout(1, true));
//		
//		Section section = toolkit.createSection(form.getBody(), 
//				  Section.DESCRIPTION|Section.TITLE_BAR|
//				  Section.TWISTIE|Section.EXPANDED);
//		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		section.setLayout(new GridLayout(1, true));
//		section.setFont(null);
//		
//		// Create a button that will live in the TITLE area
//		Button headerButton = toolkit.createButton(section, "Action", SWT.PUSH);
//		headerButton.addSelectionListener(new SelectionAdapter() {
//		    @Override
//		    public void widgetSelected(SelectionEvent e) {
//				IWizard wizard = new GlobalSynchronizeWizard();
//				WizardDialog dialog = new WizardDialog(Activator.getDisplay().getActiveShell(), wizard);
//				dialog.open();
//		    }
//		});
//		headerButton.setLayoutData(new GridData());
//		headerButton.setImage(TeamUIPlugin.getPlugin().getImage(ITeamUIImages.IMG_SYNC_VIEW));
//
//		// Move the button into the title bar
//		section.setTextClient(headerButton);
//		section.addExpansionListener(new ExpansionAdapter() {
//		  public void expansionStateChanged(ExpansionEvent e) {
//		   form.reflow(true);
//		  }
//		 });
//		 section.setText("Section title");
//		 section.setDescription("This is the description that goes "+
//		      "below the title");
//		 Composite sectionClient = toolkit.createComposite(section);
//		 sectionClient.setLayout(new GridLayout());
//		 Button button = toolkit.createButton(sectionClient, "Radio 1", SWT.RADIO);
//		 button = toolkit.createButton(sectionClient, "Radio 2", SWT.RADIO);
//		 section.setClient(sectionClient);
//	}
	
	public ChatFileDrawer getFileDrawer() {
		return fileDrawer;
	}

	@Override
	public void itemCollapsed(ExpandEvent e) {
		resize();
	}

	@Override
	public void itemExpanded(ExpandEvent e) {
		resize();
	}
	
	public void resize() {
		Activator.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				expandBar.getParent().layout(true, true);
			}
		});
	}
	
	public void workspaceChangeAdded(WorkspaceChange change) {
		fileDrawer.workspaceChangeAdded(change);
		updateVisibility();
	}
	
	public void workspaceChangeModified(WorkspaceChange change) {
		fileDrawer.workspaceChangeModified(change);
		updateVisibility();
	}
	
	public void workspaceChangeRemoved(WorkspaceChange change) {
		fileDrawer.workspaceChangeRemoved(change);
		updateVisibility();
	}
	
	public void updateVisibility() {
		expandBar.setVisible(fileDrawer.isVisible());
		((GridData)expandBar.getLayoutData()).exclude = !fileDrawer.isVisible();
	}

	public void dispose() {
		fileDrawer.dispose();
		
	}
}
