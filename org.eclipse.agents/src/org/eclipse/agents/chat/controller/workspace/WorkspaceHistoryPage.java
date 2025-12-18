package org.eclipse.agents.chat.controller.workspace;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.internal.core.history.LocalFileRevision;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.actions.CompareRevisionAction;
import org.eclipse.team.internal.ui.history.CompareFileRevisionEditorInput;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class WorkspaceHistoryPage extends HistoryPage {

	DiffNode root;
	Button button;
	
	public WorkspaceHistoryPage(DiffNode root) {
		this.root = root;
	}
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "qwer";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "asdf";
	}

	@Override
	public boolean isValidInput(Object arg0) {
		return true;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean inputSet() {
		return true;
	}

	@Override
	public void createControl(Composite parent) {
		button = new Button(parent, SWT.NONE);
		button.setText(root.getChildren()[0].getName());
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				
//				StructuredSelection sel= new StructuredSelection(new Object[] { getCurrentFileRevision(), structuredSelection.getFirstElement() });
//				compareAction.selectionChanged(sel);
//				compareAction.run();
				
//				CompareRevisionAction action = new CompareRevisionAction("compare", WorkspaceHistoryPage.this);
//				action.setCurrentFileRevision(null);
//				action.selectionChanged(new StructuredSelection(new Object[] {
//					new LocalFileRevision(state)
//				}) );
				
//				OpenRevisionAction;
//				
//				Utils.openEditor(page, new LocalFileRevision(state), new NullProgressMonitor());
				
				DiffNode diffNode = (DiffNode)root.getChildren()[0];
				CompareConfiguration configuration = new CompareConfiguration();
				configuration.setLeftLabel("Agent Changes");
				configuration.setRightLabel("Original");
				configuration.setLeftEditable(true);
				configuration.setRightEditable(false);

				CompareFileRevisionEditorInput input = new CompareFileRevisionEditorInput(diffNode.getLeft(), diffNode.getRight(), page);
				
					
//				WorkspaceCompareInput input = new WorkspaceCompareInput(configuration, (WorkspaceDiffNode)diffNode);
				CompareUI.openCompareEditor(input, true);
			}
		});
	}

	@Override
	public Control getControl() {
		return button;
	}

	@Override
	public void setFocus() {
		
	}

}
