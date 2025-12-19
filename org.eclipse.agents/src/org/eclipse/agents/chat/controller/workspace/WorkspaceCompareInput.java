package org.eclipse.agents.chat.controller.workspace;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.IProgressMonitor;

public class WorkspaceCompareInput extends CompareEditorInput {

	DiffNode root;
	public WorkspaceCompareInput(CompareConfiguration configuration, DiffNode root) {
		super(configuration);
		this.root = root;
	}

	@Override
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		return root;
	}

}
