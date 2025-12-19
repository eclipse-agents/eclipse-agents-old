package org.eclipse.agents.chat;


import org.eclipse.agents.Activator;
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

//		todoExpand.setExpanded(true);
		
		expandBar.setSpacing(8);
		
		
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
}
