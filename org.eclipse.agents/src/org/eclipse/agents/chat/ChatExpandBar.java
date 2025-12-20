package org.eclipse.agents.chat;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.controller.workspace.WorkspaceChange;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.graphics.Image;
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
		
	}
	public void workspaceChangeRemoved(WorkspaceChange change) {
		
	}
	
	public void updateVisibility() {
		expandBar.setVisible(fileDrawer.isVisible());
		((GridData)expandBar.getLayoutData()).exclude = !fileDrawer.isVisible();
	}

	public void dispose() {
		fileDrawer.dispose();
		
	}
}
