/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.agents.chat.controller.workspace;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.HistoryItem;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
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
	WorkspaceController controller;
	
	public WorkspaceChange(WorkspaceController controller, int type, Path path, IFileState state) {
		this.controller = controller;
		this.type = type;
		this.path = path;
		this.state = state;
		this.originalContent = null;
	}
	
	public WorkspaceChange(WorkspaceController controller, int type, Path path, String originalContent) {
		this.controller = controller;
		this.type = type;
		this.path = path;
		this.state = null;
		this.originalContent = originalContent;
	}
	
	public int getKind() {
		return type;
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
	
	public Path getPath() {
		return path;
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

	public void remove() {
		controller.removeChange(this);
	}
	
	/**
	 * Use the controller to modify the editor or file based on current Workbench state.
	 * This will update the existing WorkspaceChange for the path
	 * After we will remove the WorkspaceChange from the controller
	 */
	public void revert() {
		if (originalContent == null) {
			//TODO
		} else {
			ITextEditor editor = WorkspaceController.findFileEditor(path);
			if (editor != null) {
				controller.writeToEditor(path, editor, originalContent);
			} else {
				controller.writeToFile(path, originalContent);
			}
		}
		controller.removeChange(this);
		
	}
}
