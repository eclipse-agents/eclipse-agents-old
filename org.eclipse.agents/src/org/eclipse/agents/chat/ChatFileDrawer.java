package org.eclipse.agents.chat;

import org.eclipse.agents.chat.controller.workspace.WorkspaceChange;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ChatFileDrawer {

	Composite composite;
	ExpandItem expand; 
	Table table;
	ReviewListener reviewListener;
	
	public ChatFileDrawer(ExpandBar bar, int index) {
		
		reviewListener = new ReviewListener();

		composite = new Composite (bar, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		table = new Table (composite, SWT.BORDER | SWT.SINGLE);
		table.setLinesVisible (true);
		
		for (int i=0; i < 6; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth (100);
		}
		
		expand = new ExpandItem(bar, SWT.NONE, index);
		expand.setText("0 files changed");
//		expand.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_PLAY));
		expand.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		expand.setControl(composite);
		
	}
	
	
	public void workspaceChangeAdded(WorkspaceChange change) {
		
		TableItem item = new TableItem (table, SWT.NONE);
		
		item.setImage(0, change.getTypeImage());
		item.setImage(1, change.getTypeImage());
		item.setText(2, change.getName());
		
		TableEditor editor = new TableEditor (table);
		Button button = new Button (table, SWT.PUSH);
		button.setText("Review");
		button.pack ();
		button.setData(change);
		button.addSelectionListener(reviewListener);
		editor.minimumWidth = button.getSize ().x;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor (button, item, 3);
		table.getColumn(3).setWidth(button.getSize().x + 10);
		
		editor = new TableEditor (table);
		button = new Button (table, SWT.PUSH);
		button.setText("Accept");
		button.pack ();
		editor.minimumWidth = button.getSize ().x;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor (button, item, 4);
		table.getColumn(4).setWidth(button.getSize().x + 10);
		
		editor = new TableEditor (table);
		button = new Button (table, SWT.PUSH);
		button.setText("Revert");
		button.pack ();
		editor.minimumWidth = button.getSize ().x;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor (button, item, 5);
		table.getColumn(5).setWidth(button.getSize().x + 10);
		
		updateExpandBar();
	}

	public void workspaceChangeModified(WorkspaceChange change) {
	
	}
	public void workspaceChangeRemoved(WorkspaceChange change) {
	
	}
	
	public void updateExpandBar() {
		int count = table.getItemCount();
		if (count == 1) {
			expand.setText(count + " file changed");
		} else {
			expand.setText(count + " files changed");
		}
	}
	
	public boolean isVisible() {
		return table.getItemCount() > 0;
	}
	

	class ReviewListener implements SelectionListener {

		@Override
		public void widgetSelected(SelectionEvent e) {
			WorkspaceChange change = ((WorkspaceChange)e.widget.getData());
			change.review();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			
		}
		
	}
	
	
}
