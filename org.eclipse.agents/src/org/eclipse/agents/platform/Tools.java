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
package org.eclipse.agents.platform;


import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.agents.Activator;
import org.eclipse.agents.MCPException;
import org.eclipse.agents.platform.resource.ConsoleAdapter;
import org.eclipse.agents.platform.resource.EditorAdapter;
import org.eclipse.agents.platform.resource.MarkerAdapter;
import org.eclipse.agents.platform.resource.WorkspaceResourceAdapter;
import org.eclipse.agents.platform.resource.ResourceSchema.Children;
import org.eclipse.agents.platform.resource.ResourceSchema.Consoles;
import org.eclipse.agents.platform.resource.ResourceSchema.DEPTH;
import org.eclipse.agents.platform.resource.ResourceSchema.Editor;
import org.eclipse.agents.platform.resource.ResourceSchema.Editors;
import org.eclipse.agents.platform.resource.ResourceSchema.File;
import org.eclipse.agents.platform.resource.ResourceSchema.Problems;
import org.eclipse.agents.platform.resource.ResourceSchema.Tasks;
import org.eclipse.agents.platform.resource.ResourceSchema.TextEditorSelection;
import org.eclipse.agents.platform.resource.ResourceSchema.TextReplacement;
import org.eclipse.agents.resource.IResourceHierarchy;
import org.eclipse.agents.resource.IResourceTemplate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
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

	@McpTool(name = "listChildResources",
			description = "List child resources of an Eclipse workspace, project or folder URI", 
			annotations = @McpTool.McpAnnotations(
					title = "List Child Resources"))
	public Children<?> listChildResources(
			@McpToolParam(
					description = "URI of an eclipse project or folder") 
					String resourceURI,
			@McpToolParam(
					description = "CHILDREN, GRANDCHILDREN or INFINITE", 
					required = false) 
					DEPTH depth) {

		IResourceTemplate<?, ?> adapter = Activator.getDefault().getServerManager().getResourceTemplate(resourceURI);
		
		if (adapter == null) {
			throw new MCPException("The uri could not be resolved: " + resourceURI);
		} else if (!(adapter instanceof IResourceHierarchy)) {
			throw new MCPException("The uri does not support children: " + resourceURI);
		}
		
		return ((IResourceHierarchy<?, ?>)adapter).getChildren(depth);
	}

	@McpTool(name = "readResource", 
			description = "Returns the contents of an Eclipse workspace file, editor, or console URI", 
			annotations = @McpTool.McpAnnotations(
					title = "Read Resource"))
	public String readResource(
			@McpToolParam(
					description = "URI of an eclipse file, editor or console") 
					String uri) {

		IResourceTemplate<?, ?> adapter = Activator.getDefault().getServerManager().getResourceTemplate(uri);
	
		if (adapter == null) {
			throw new MCPException("The uri could not be resolved");
		}
		
		return adapter.toContent();
	}

	/**
	 * 
	 * @param fileUri
	 * @param selectionOffset
	 * @param selectionLength
	 * @return
	 */
	@McpTool (name = "openEditor", 
			description = "open an Eclipse IDE editor on a file URI and set an initial text selection", 
			annotations = @McpTool.McpAnnotations(
					title = "Open Editor"))
	public Editor openEditor(
			@McpToolParam(
					description = "Eclipse workspace file uri") 
					String fileUri,
			@McpToolParam(
					description = "offset of the text selection", 
					required = false) 
					Integer selectionOffset,
			@McpToolParam(
					description = "length of the text selection", 
					required = false) 
					Integer selectionLength) {

		WorkspaceResourceAdapter adapter = new WorkspaceResourceAdapter(fileUri);
		IResource resource = adapter.getModel();
		final Editor[] result = new Editor[] { null };

		if (resource instanceof IFile) {
			try {
				resource.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						IWorkbenchPart part = page.getActivePart();
						part.dispose();

						IEditorPart editor = IDE.openEditor(page, (IFile) resource, true);
						result[0] = new EditorAdapter().fromEditorName(editor.getTitle()).toJson();

						if (editor instanceof ITextEditor && selectionOffset != null) {
							try {
								ITextEditor textEditor = (ITextEditor) editor;
								IDocument document = textEditor.getDocumentProvider()
										.getDocument(textEditor.getEditorInput());
								if (selectionOffset >= 0 && document.getLength() > selectionOffset) {
									if (selectionLength != null && selectionLength > 0
											&& document.getLength() > selectionOffset + selectionLength) {
										textEditor.selectAndReveal(selectionOffset, selectionLength);
									} else {
										textEditor.selectAndReveal(selectionOffset, 0);
									}
								}
							} catch (Exception e) {
								// swallow selection errors
								e.printStackTrace();
							}
						}
						page.activate(editor);
						page.getActivePart();

					} catch (PartInitException e) {
						throw new MCPException(e);
					}
				}
			});
		} else {
			throw new MCPException("The file URI could not be resolved");
		}

		return result[0];
	}

	@McpTool(name = "closeEditor", 
			description = "close an Eclipse IDE editor", 
			annotations = @McpTool.McpAnnotations(
					title = "Close Editor"))
	public void closeEditor(
			@McpToolParam(
				description = "URI of an open Eclipse editor") 
				String editorUri) {
		EditorAdapter adapter = new EditorAdapter(editorUri);
		final IEditorReference reference = adapter.getModel();

		// TODO close just the editor, not all editors on editor's file
		Activator.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				reference.getPage().closeEditors(new IEditorReference[] { reference }, true);
			}
		});
	}

	@McpTool(name = "saveEditor", 
			description = "save the contents of a dirty Eclipse IDE editor to file", 
			annotations = @McpTool.McpAnnotations(
					title = "Save Editor"))
	public boolean saveEditor(
			@McpToolParam(
			description = "URI of an open Eclipse editor") 
			String editorUri) {
		EditorAdapter adapter = new EditorAdapter(editorUri);
		final IEditorReference reference = adapter.getModel();
		boolean[] result = new boolean[] { false };
		if (reference != null) {
			if (reference.isDirty()) {
				Activator.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						try {
							IEditorInput input = reference.getEditorInput();
							if (input instanceof IFileEditorInput) {
								IFile ifile = ((IFileEditorInput) input).getFile();
								result[0] = IDE.saveAllEditors(new IResource[] { ifile }, true);
							}
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			} else {
				throw new MCPException("Editor does not have unsaved changes");
			}
		} else {
			throw new MCPException("editorURI could not be resolved");
		}
		return result[0];
	}

	@McpTool(name = "changeEditorText", 
			description = "Make one or more changes to an Eclipse text editor", 
			annotations = @McpTool.McpAnnotations(
					title = "Change Editor Text"))
	public boolean changeEditorText(
			@McpToolParam(description = "Open Eclipse editor URI") 
			String editorURI,
			@McpToolParam(description = "One or more text replacements to be applied in order") 
			TextReplacement[] replacements) {

		EditorAdapter adapter = new EditorAdapter(editorURI);
		final IEditorReference reference = adapter.getModel();
		boolean[] result = new boolean[] { false };

		// TODO apply changes in reverse order
		Arrays.sort(replacements, new Comparator<TextReplacement>() {
			@Override
			public int compare(TextReplacement o1, TextReplacement o2) {
				return o2.offset() - o1.offset();
			}

		});

		if (reference != null) {
			IEditorPart part = reference.getEditor(true);
			if (part instanceof ITextEditor) {
				Activator.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						ITextEditor textEditor = (ITextEditor) part;
						if (textEditor.isEditable()) {
							IDocument document = textEditor.getDocumentProvider()
									.getDocument(textEditor.getEditorInput());

							IRewriteTarget rewriteTarget = textEditor.getAdapter(IRewriteTarget.class);
							if (rewriteTarget != null) {
								rewriteTarget.beginCompoundChange();
							}

							try {
								for (TextReplacement replacement : replacements) {
									document.replace(replacement.offset(), replacement.length(), replacement.text());
								}
								result[0] = true;
							} catch (BadLocationException ble) {
								throw new MCPException(ble);
							} finally {
								if (rewriteTarget != null) {
									rewriteTarget.endCompoundChange();
								}
							}
						} else {
							throw new MCPException("The text editor is in read-only mode");
						}
					}
				});
			} else {
				throw new MCPException("Editor is not a text editor");
			}
		} else {
			throw new MCPException("editorURI could not be resolved");
		}
		return result[0];
	}

//  
//     

	@McpTool(name = "listProblems", 
			description = "list Eclipse IDE compilation and configuration problems", 
			annotations = @McpTool.McpAnnotations(
				title = "List Problems"))
	public Problems listProblems(
			@McpToolParam(
				description = "Eclipse workspace file or editor URI")
				String resourceURI,
			@McpToolParam(
				description = "One of ERROR, INFO or WARNING. Default i", 
				required = false) 
				String severity) {

		//TODO severity

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

	@McpTool (name = "listTasks", 
			description = "list codebase locations of tasks including TODO comments", 
			annotations = @McpTool.McpAnnotations(title = "List Tasks"))
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