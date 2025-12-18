package org.eclipse.agents.chat.controller.workspace;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.Path;

public class WorkspaceDiffNode extends DiffNode {

	String originalContent;
	Path absolutePath;

	public WorkspaceDiffNode(int kind) {
		super(kind);
	}
	
	public WorkspaceDiffNode(int kind, ITypedElement left, ITypedElement right) {
		super(kind, null, left, right);
	}

	public WorkspaceDiffNode(int kind, Path absolutePath, String originalContent) {
		super(kind);
		this.absolutePath = absolutePath;
		this.originalContent = originalContent;
	}
	
	
}
