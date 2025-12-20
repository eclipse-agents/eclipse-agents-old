package org.eclipse.agents.chat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.controller.workspace.WorkspaceChange;
import org.eclipse.agents.contexts.Images;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
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
	ClearListener clearListener;
	RevertListener revertListener;
	
	Map<ImageDescriptor, Image> images;
	CompareConfiguration cc;
	
	public ChatFileDrawer(ExpandBar bar, int index) {
		
		images = new HashMap<ImageDescriptor, Image>();
		cc = new CompareConfiguration();
		reviewListener = new ReviewListener();
		clearListener = new ClearListener();
		revertListener = new RevertListener();

		composite = new Composite (bar, SWT.NONE);
		TableColumnLayout tableLayout =new TableColumnLayout();
		composite.setLayout(tableLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		table = new Table (composite, SWT.BORDER | SWT.SINGLE);
		table.setLinesVisible (false);
		table.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		for (int i=0; i < 5; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			if (i == 0) {
				tableLayout.setColumnData(column, 
						new ColumnWeightData(0, 40, false));
			} else if (i == 1) {
				tableLayout.setColumnData(column, 
						new ColumnWeightData(100, 200, true));
			} else {
				tableLayout.setColumnData(column, 
						new ColumnWeightData(0, 20, false));
			}
		}
		
		expand = new ExpandItem(bar, SWT.NONE, index);
		expand.setText("0 files changed");
//		expand.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_PLAY));
		expand.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		expand.setControl(composite);
		
	}
	
	
	public void workspaceChangeAdded(WorkspaceChange change) {
		
		TableItem item = new TableItem (table, SWT.NONE);
		item.setData(change);
		
		ImageDescriptor id = change.getPathImageDescriptor();
		if (id != null) {
			if (!images.containsKey(id)) {
				images.put(id, id.createImage());
			}
			item.setImage(0, cc.getImage(images.get(id), Differencer.RIGHT + change.getKind()));
		}

		item.setText(1, change.getName());
		
		TableEditor editor = new TableEditor (table);
		final Button reviewButton = new Button (table, SWT.PUSH);
//		button.setText("Review");
		reviewButton.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_2WAYCOMPARE));
		reviewButton.pack ();
		reviewButton.setData(item);
		reviewButton.addSelectionListener(reviewListener);
		editor.minimumWidth = reviewButton.getSize ().x;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor (reviewButton, item, 2);
		table.getColumn(2).setWidth(reviewButton.getSize().x + 10);
		
		editor = new TableEditor (table);
		final Button removeButton = new Button (table, SWT.PUSH);
//		button.setText("Accept");
		removeButton.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_REMOVE));
		removeButton.pack ();
		removeButton.setData(item);
		removeButton.addSelectionListener(clearListener);
		editor.minimumWidth = removeButton.getSize ().x;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor (removeButton, item, 3);
		table.getColumn(3).setWidth(removeButton.getSize().x + 10);
		
		editor = new TableEditor (table);
		final Button revertButton= new Button (table, SWT.PUSH);
//		button.setText("Revert");
		revertButton.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_UNDO));
		revertButton.pack ();
		revertButton.setData(item);
		revertButton.addSelectionListener(revertListener);
		editor.minimumWidth = revertButton.getSize ().x;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor (revertButton, item, 4);
		table.getColumn(4).setWidth(revertButton.getSize().x + 10);
		
		item.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				reviewButton.dispose();
				removeButton.dispose();
				revertButton.dispose();
			}
			
		});
		updateExpandBar();
		layout();
	}

	public void workspaceChangeModified(WorkspaceChange change) {
		//TODO
	}
	public void workspaceChangeRemoved(WorkspaceChange change) {
		for (int i = 0; i < table.getItemCount(); i++) {
			if (change == table.getItem(i).getData()) {
				table.remove(i);
				break;
			}
		}
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
	

	class ReviewListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			TableItem item = ((TableItem)e.widget.getData());
			WorkspaceChange change = ((WorkspaceChange)item.getData());
			change.review();
		}
	}
	
	class ClearListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			TableItem item = ((TableItem)e.widget.getData());
			WorkspaceChange change = ((WorkspaceChange)item.getData());
			change.remove();
		}
	}
	
	class RevertListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			TableItem item = ((TableItem)e.widget.getData());
			WorkspaceChange change = ((WorkspaceChange)item.getData());
			change.revert();
		}
	}
	
	public void dispose() {
		for (Image image: images.values()) {
			image.dispose();
		}
		cc.dispose();
	}
	
	public void layout() {
//		for (TableColumn column: table.getColumns()) {
//			column.pack();
//		}
		table.layout(true);
		
//		table.setSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//		System.out.println(composite.getSize().x);
//		table.getColumn(1).setWidth(table.getSize().x 
//				- table.getColumn(0).getWidth()
//				- table.getColumn(2).getWidth()
//				- table.getColumn(3).getWidth()
//				- table.getColumn(4).getWidth());
	}
	
}
