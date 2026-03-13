/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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
package org.eclipse.agents.contexts.platform;

import org.eclipse.agents.Activator;
import org.eclipse.agents.MCPException;
import org.eclipse.agents.contexts.adapters.IResourceTemplate;
import org.eclipse.agents.contexts.platform.resource.ConsoleAdapter;
import org.eclipse.agents.contexts.platform.resource.EditorAdapter;
import org.eclipse.agents.contexts.platform.resource.MarkerAdapter;
import org.eclipse.agents.contexts.platform.resource.ResourceSchema.Children;
import org.eclipse.agents.contexts.platform.resource.ResourceSchema.Consoles;
import org.eclipse.agents.contexts.platform.resource.ResourceSchema.DEPTH;
import org.eclipse.agents.contexts.platform.resource.ResourceSchema.Editors;
import org.eclipse.agents.contexts.platform.resource.ResourceSchema.File;
import org.eclipse.agents.contexts.platform.resource.ResourceSchema.Problems;
import org.eclipse.agents.contexts.platform.resource.ResourceSchema.Tasks;
import org.eclipse.agents.contexts.platform.resource.ResourceSchema.TextEditorSelection;
import org.eclipse.agents.contexts.platform.resource.WorkspaceResourceAdapter;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

public class Tools {

	@McpTool(name = "currentSelection",
			description = "Return the active Eclipse IDE text editor and its selected text",
			annotations = @McpTool.McpAnnotations(
					title = "Currrent Selection"))
	public TextEditorSelection currentSelection() {
		IEditorPart activePart = EditorAdapter.getActiveEditor();
		if (activePart != null) {
			EditorAdapter adapter = new EditorAdapter().fromEditorName(activePart.getTitle());
			return adapter.getEditorSelection();
		}
		return null;
	}

	@McpTool(name = "listEditors",
			description = "List open Eclipse IDE text editors",
			annotations = @McpTool.McpAnnotations(
					title = "List Editors"))
	public Editors listEditors() {
		return EditorAdapter.getEditors();
	}

	@McpTool(name = "listConsoles",
			description = "List open Eclipse IDE consoles",
			annotations = @McpTool.McpAnnotations(
					title = "List Consoles"))
	public Consoles listConsoles() {
		return ConsoleAdapter.getConsoles();
	}

	@McpTool(name = "listProjects",
			description = "List open Eclipse IDE projects",
			annotations = @McpTool.McpAnnotations(
					title = "List Projects"))
	public Children<File> listProjects() {
		WorkspaceResourceAdapter adapter = new WorkspaceResourceAdapter(ResourcesPlugin.getWorkspace().getRoot());
		return adapter.getChildren(DEPTH.CHILDREN);
	}

	@McpTool(name = "saveEditor",
			description = "Save an open Eclipse IDE editor",
			annotations = @McpTool.McpAnnotations(
					title = "Save Editor"))
	public boolean saveEditor(
			@McpToolParam(
					description = "URI of an open Eclipse editor")
					String editorUri) {
		EditorAdapter adapter = new EditorAdapter(editorUri);
		final IEditorReference reference = adapter.getModel();
		final boolean[] result = new boolean[] { false };

		Activator.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				IEditorPart part = reference.getEditor(true);
				if (part != null && part.isDirty()) {
					part.doSave(null);
					result[0] = true;
				}
			}
		});

		return result[0];
	}

	@McpTool(name = "listProblems",
			description = "List Eclipse IDE compilation and configuration problems",
			annotations = @McpTool.McpAnnotations(
					title = "List Problems"))
	public Problems listProblems(
			@McpToolParam(description = "Eclipse workspace file or editor URI",
			required = false)
			String resourceURI) {

		if (resourceURI == null || resourceURI.isEmpty()) {
			return MarkerAdapter.getProblems(ResourcesPlugin.getWorkspace().getRoot());
		} else {
			IResourceTemplate<?, ?> adapter = Activator.getDefault().getServerManager().getResourceTemplate(resourceURI);
			if (adapter instanceof WorkspaceResourceAdapter) {
				return MarkerAdapter.getProblems(((WorkspaceResourceAdapter)adapter).getModel());
			} else if (adapter instanceof EditorAdapter) {
				IEditorReference reference = ((EditorAdapter)adapter).getModel();
				IEditorPart part = reference.getEditor(true);
				if (part != null) {
					if (part instanceof ITextEditor) {
						return MarkerAdapter.getProblems((ITextEditor)part);
					} else {
						throw new MCPException("Editor is not a text editor");
					}
				} else {
					throw new MCPException("Unable to initialize editor");
				}
			} else {
				throw new MCPException("The resource URI is not a file or editor");
			}
		}
	}

	@McpTool(name = "listTasks",
			description = "List Eclipse IDE tasks",
			annotations = @McpTool.McpAnnotations(
					title = "List Tasks"))
	public Tasks listTasks(
			@McpToolParam(description = "Eclipse workspace file or editor URI",
			required = false)
			String resourceURI) {

		if (resourceURI == null || resourceURI.isEmpty()) {
			return MarkerAdapter.getTasks(ResourcesPlugin.getWorkspace().getRoot());
		} else {
			IResourceTemplate<?, ?> adapter = Activator.getDefault().getServerManager().getResourceTemplate(resourceURI);
			if (adapter instanceof WorkspaceResourceAdapter) {
				return MarkerAdapter.getTasks(((WorkspaceResourceAdapter)adapter).getModel());
			} else if (adapter instanceof EditorAdapter) {
				IEditorReference reference = ((EditorAdapter)adapter).getModel();
				IEditorPart part = reference.getEditor(true);
				if (part != null) {
					if (part instanceof ITextEditor) {
						return MarkerAdapter.getTasks((ITextEditor)part);
					} else {
						throw new MCPException("Editor is not a text editor");
					}
				} else {
					throw new MCPException("Unable to initialize editor");
				}
			} else {
				throw new MCPException("The resource URI is not a file or editor");
			}
		}
	}
}
