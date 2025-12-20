package org.eclipse.agents.chat.controller.workspace;

import org.eclipse.agents.Activator;
import org.eclipse.agents.contexts.Images;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.HistoryItem;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class WorkspaceChange {

	//Differencer
	int type;
	
	Path path;
	IFileState state;;
	String originalContent;
	
	public WorkspaceChange(int type, Path path, IFileState state) {
		this.type = type;
		this.path = path;
		this.state = state;
		this.originalContent = null;
	}
	
	public WorkspaceChange(int type, Path path, String originalContent) {
		this.type = type;
		this.path = path;
		this.state = null;
		this.originalContent = originalContent;
	}
	
	public int getKind() {
		return type;
	}
	
	public Image getTypeImage() {
		if (type == Differencer.ADDITION) {
			return Activator.getDefault().getImageRegistry().get(Images.IMG_MOD_ADD);
		} else if (type == Differencer.DELETION) {
			return Activator.getDefault().getImageRegistry().get(Images.IMG_MOD_DEL);
		} 
		
		return Activator.getDefault().getImageRegistry().get(Images.IMG_MOD_CHG);
	}
	
	public ImageDescriptor getPathImageDescriptor() {
        IFile file = WorkspaceController.findFile(path);
        if (file != null) {
        	IEditorDescriptor editorDescriptor = IDE.getDefaultEditor(file);
        	if (editorDescriptor != null) {
        		return editorDescriptor.getImageDescriptor();
        	}
        }
        return null;
	}
	
	public String getName() {
		return path.toFile().getName();
	}
	
	public void review() {
		IFile file = WorkspaceController.findFile(path);
		ITextEditor editor = WorkspaceController.findFileEditor(path);
		ITypedElement left = null, right = null;

		if (editor != null) {
			left = new LocalResourceTypedElement(file);
		} else if (file != null){
			left = new ResourceNode(file);
		} else {
			//TODO
		}

		if (state != null) {
			right = new HistoryItem(left, state);
		} else if (originalContent != null) {
			right = new StringNode(file, originalContent);
		} else {
			//TODO
		}
		
		if (left !=null && right != null) {
			DiffNode node = new DiffNode(left, right);
			CompareConfiguration configuration = new CompareConfiguration();
			configuration.setLeftLabel("Agent Changes");
			configuration.setRightLabel("Original");
			configuration.setLeftEditable(true);
			configuration.setRightEditable(false);
	
			WorkspaceCompareInput input = new WorkspaceCompareInput(configuration, node);
			CompareUI.openCompareEditor(input);
		} else {
			
		}
	}

	public void accept() {
		
	}
	
	public void revert() {
		
	}
}
