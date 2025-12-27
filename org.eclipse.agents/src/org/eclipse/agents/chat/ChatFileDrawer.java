package org.eclipse.agents.chat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.controller.workspace.WorkspaceChange;
import org.eclipse.agents.chat.controller.workspace.WorkspaceController;
import org.eclipse.agents.contexts.Images;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class ChatFileDrawer {

	Section section;
	ScrolledForm form;
	Composite composite;
	Table table;
	ReviewListener reviewListener;
	ClearListener clearListener;
	RevertListener revertListener;

	Map<ImageDescriptor, Image> images;
	CompareConfiguration cc;

	public ChatFileDrawer(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		composite = form.getBody();
		
		composite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		composite.setLayout(new GridLayout(1, true));

		section = toolkit.createSection(composite,
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED );
		
		section.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		section.setLayout(new GridLayout(1, true));

		Composite buttons = toolkit.createComposite(section, SWT.BORDER_DASH);
		buttons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		buttons.setLayout(new GridLayout(2, false));
		
		Button revertAll = toolkit.createButton(buttons, "Revert All", SWT.PUSH);
		revertAll.addSelectionListener(new RevertAllListener());
		revertAll.setLayoutData(new GridData());
		revertAll.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_UNDO_All));

		Button acceptAll = toolkit.createButton(buttons, "Accept All", SWT.PUSH);
		acceptAll.addSelectionListener(new ClearAllListener());
		acceptAll.setLayoutData(new GridData());
		acceptAll.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_REMOVE_ALL));

		
		// Move the button into the title bar
		section.setTextClient(buttons);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText("File Changes");
		section.setDescription("List of modified files. Does not include added, removed or moved files");
		
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		TableColumnLayout tableLayout =new TableColumnLayout();
		sectionClient.setLayout(tableLayout);
		sectionClient.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		images = new HashMap<ImageDescriptor, Image>();
		cc = new CompareConfiguration();
		reviewListener = new ReviewListener();
		clearListener = new ClearListener();
		revertListener = new RevertListener();

		table = new Table(sectionClient, SWT.BORDER | SWT.SINGLE);
		table.setLinesVisible(false);
		table.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		((GridData)table.getLayoutData()).heightHint = 100;

		for (int i = 0; i < 5; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			if (i == 0) {
				tableLayout.setColumnData(column, new ColumnWeightData(0, 40, false));
			} else if (i == 1) {
				tableLayout.setColumnData(column, new ColumnWeightData(150, 200, true));
			} else {
				tableLayout.setColumnData(column, new ColumnWeightData(0, 20, false));
			}
		}
		
		updatePresentation();

	}

	public void workspaceChangeAdded(WorkspaceChange change) {

		TableItem item = new TableItem(table, SWT.NONE);
		item.setData(change);

		ImageDescriptor id = change.getPathImageDescriptor();
		if (id != null) {
			if (!images.containsKey(id)) {
				images.put(id, id.createImage());
			}
			item.setImage(0, cc.getImage(images.get(id), Differencer.RIGHT + change.getKind()));
		}

		item.setText(1, change.getName());

		TableEditor editor = new TableEditor(table);
		final Button reviewButton = new Button(table, SWT.PUSH);
//		button.setText("Review");
		reviewButton.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_2WAYCOMPARE));
		reviewButton.pack();
		reviewButton.setData(item);
		reviewButton.addSelectionListener(reviewListener);
		editor.minimumWidth = reviewButton.getSize().x;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor(reviewButton, item, 2);
		table.getColumn(2).setWidth(reviewButton.getSize().x + 10);

		editor = new TableEditor(table);
		final Button removeButton = new Button(table, SWT.PUSH);
//		button.setText("Accept");
		removeButton.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_REMOVE));
		removeButton.pack();
		removeButton.setData(item);
		removeButton.addSelectionListener(clearListener);
		editor.minimumWidth = removeButton.getSize().x;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor(removeButton, item, 3);
		table.getColumn(3).setWidth(removeButton.getSize().x + 10);

		editor = new TableEditor(table);
		final Button revertButton = new Button(table, SWT.PUSH);
//		button.setText("Revert");
		revertButton.setImage(Activator.getDefault().getImageRegistry().get(Images.IMG_UNDO));
		revertButton.pack();
		revertButton.setData(item);
		revertButton.addSelectionListener(revertListener);
		editor.minimumWidth = revertButton.getSize().x;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor(revertButton, item, 4);
		table.getColumn(4).setWidth(revertButton.getSize().x + 10);

		item.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				reviewButton.dispose();
				removeButton.dispose();
				revertButton.dispose();
			}

		});
		updatePresentation();
	}

	public void workspaceChangeModified(WorkspaceChange change) {
		// TODO
	}

	public void workspaceChangeRemoved(WorkspaceChange change) {
		for (int i = 0; i < table.getItemCount(); i++) {
			if (change == table.getItem(i).getData()) {
				table.remove(i);
				updatePresentation();
				break;
			}
		}
	}

	class ReviewListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			TableItem item = ((TableItem) e.widget.getData());
			WorkspaceChange change = ((WorkspaceChange) item.getData());
			change.review();
		}
	}

	class ClearListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			TableItem item = ((TableItem) e.widget.getData());
			WorkspaceChange change = ((WorkspaceChange) item.getData());
			change.remove();
		}
	}

	class ClearAllListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			for (TableItem item : table.getItems()) {
				WorkspaceChange change = ((WorkspaceChange) item.getData());
				change.remove();
			}
		}
	}

	class RevertListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			TableItem item = ((TableItem) e.widget.getData());
			WorkspaceChange change = ((WorkspaceChange) item.getData());
			change.revert();
		}
	}

	class RevertAllListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			for (TableItem item : table.getItems()) {
				WorkspaceChange change = ((WorkspaceChange) item.getData());
				change.revert();
			}
		}
	}

	public void dispose() {
		for (Image image : images.values()) {
			image.dispose();
		}
		cc.dispose();
	}

	public Set<IProject> getProjects() {
		Set<IProject> projects = new HashSet<IProject>();
		for (TableItem item : table.getItems()) {
			WorkspaceChange change = (WorkspaceChange) item.getData();
			Path path = change.getPath();
			IFile file = WorkspaceController.findFile(path);
			if (file != null) {
				// TODO what if file was 'deleted'
				projects.add(file.getProject());
			}
		}
		return projects;
	}
	
	public void updatePresentation() {
		int count = table.getItemCount();
		if (count == 1) {
			section.setText(count + " file changed");
		} else {
			section.setText(count + " files changed");
		}
		
		boolean isVisible = table.getItemCount() > 0;
		form.setVisible(isVisible);
		((GridData)form.getLayoutData()).exclude = !isVisible;
		
		table.layout(true);
		form.getParent().layout(true);
	}

}
