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
package org.eclipse.agents.platform.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.agents.Activator;
import org.eclipse.agents.MCPException;
import org.eclipse.agents.internal.Tracer;
import org.eclipse.agents.platform.resource.ResourceSchema.Editor;
import org.eclipse.agents.platform.resource.ResourceSchema.Editors;
import org.eclipse.agents.platform.resource.ResourceSchema.TextEditorSelection;
import org.eclipse.agents.platform.resource.ResourceSchema.TextSelection;
import org.eclipse.agents.resource.IResourceTemplate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IMarkSelection;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ResourceLink;
import io.modelcontextprotocol.util.DefaultMcpUriTemplateManager;

/**
 * support for resource template: eclipse://editor/{name}
 */
public class EditorAdapter implements IResourceTemplate<IEditorReference, Editor> {

	final String template = "eclipse://editor/{name}";
	final String prefix = template.substring(0, template.indexOf("{"));
	IEditorReference editorReference = null;
	
	public EditorAdapter() {}

	public EditorAdapter(IEditorReference editorReference) {
		this.editorReference = editorReference;
	}
	
	public EditorAdapter(String uri) {
		DefaultMcpUriTemplateManager tm = new DefaultMcpUriTemplateManager(template);
		if (tm.matches(uri)) {
			Map<String, String> variables = tm.extractVariableValues(uri);
			String name = variables.get("name");
			name = URLDecoder.decode(name,StandardCharsets.UTF_8);

			for (IWorkbenchWindow window: PlatformUI.getWorkbench().getWorkbenchWindows()) {
				for (IWorkbenchPage page: window.getPages()) {
					for (IEditorReference reference: page.getEditorReferences()) {
						if (reference.getName().equals(name)) {
							this.editorReference = reference;
						}
					}
				}
			}
		}
		
		if (editorReference == null) {
			throw new MCPException("uri not resolved: " + uri);
		}
	}

	@Override
	public String[] getTemplates() {
		return new String[] { template };
	}
	
	@Override
	public EditorAdapter fromUri(String uri) {
		return new EditorAdapter(uri);
	}

	@Override
	public EditorAdapter fromModel(IEditorReference console) {
		return new EditorAdapter(console);
	}

	@Override
	public IEditorReference getModel() {
		return editorReference;
	}

	@Override
	public Editor toJson() {
		String name = editorReference.getTitle();
		boolean isDirty = editorReference.isDirty();
		String contentDescription = editorReference.getContentDescription();
		String tooltip = editorReference.getTitleToolTip();
		
		Tracer.trace().trace(Tracer.PLATFORM, contentDescription);
		Tracer.trace().trace(Tracer.PLATFORM, tooltip);
		
		
		boolean[] isActive = new boolean[] { false };
		ResourceLink file = null;
		ResourceLink editor = toResourceLink();
		
		IEditorPart part = editorReference.getEditor(false);
		if (part != null) {
			Activator.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbench workbench = PlatformUI.getWorkbench();
					if (workbench != null) {
						IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
						if (window != null) {
							IWorkbenchPage page = window.getActivePage();
							if (page != null) {
								if (part == page.getActiveEditor()) {
									isActive[0] = true;
								}
							}
						}
					}
				}
			});
		
			IEditorInput input = part.getEditorInput();
			if (input instanceof IFileEditorInput) {
				WorkspaceResourceAdapter adapter = new WorkspaceResourceAdapter( ((IFileEditorInput)input).getFile());
				file = adapter.toResourceLink();
			}
		}

		return new Editor(name, editor, file, isActive[0], isDirty);
	}

	@Override
	public ResourceLink toResourceLink() {
		McpSchema.ResourceLink.Builder builder =  McpSchema.ResourceLink.builder();
		
		builder
			.uri(toUri())
			.name(editorReference.getTitle())
			.description("Content of an Eclipse IDE Editor");
		
		Activator.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				IEditorPart part = editorReference.getEditor(false);
				if (part instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor)part;
					IDocument document = textEditor.getDocumentProvider().getDocument(part.getEditorInput());
					
					builder
						.mimeType("text/plain")
						.size((long)document.getLength());
				} 
			}
		});

		return builder.build();
		
	}

	@Override
	public String toUri() {
		return prefix + URLEncoder.encode(editorReference.getTitle(), StandardCharsets.UTF_8);
	}

	@Override
	public String toContent() {
		
		StringBuffer result = new StringBuffer();
		Activator.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				IEditorPart part = editorReference.getEditor(true);
				if (part instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor)part;
					IDocument document = textEditor.getDocumentProvider().getDocument(part.getEditorInput());
					result.append(document.get());
				} else {
					try {
						IEditorInput input = editorReference.getEditorInput();
						if (input instanceof IFileEditorInput) {
							IFile file = ((IFileEditorInput)input).getFile();
							
							try (InputStreamReader reader = new InputStreamReader(
									file.getContents(), file.getCharset())) {
							       
								BufferedReader breader = new BufferedReader(reader);
								result.append(breader.lines().collect(Collectors.joining("\n"))); //$NON-NLS-1$
								
							}catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							} catch (CoreException e) {
								e.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		});
		return result.toString();
	}
	
	// custom
	
	public EditorAdapter fromEditorName(String name) {
		return new EditorAdapter(prefix + URLEncoder.encode(name, StandardCharsets.UTF_8));
	}

	public TextEditorSelection getEditorSelection() {
		
		Editor editor = (Editor)toJson();
		TextSelection selection = getTextSelection();
		return new TextEditorSelection(editor, selection);
	}

	
	public TextSelection getTextSelection() {
		TextSelection[] result = new TextSelection[] { null };
		
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				IEditorPart part = editorReference.getEditor(false);
				ISelection selection = null;
				
				if (part instanceof ITextEditor) {
					selection = ((ITextEditor)part).getSelectionProvider().getSelection();
				} else if (part instanceof ISelectionProvider) {
					selection = ((ISelectionProvider)part).getSelection();
				}

				if (selection instanceof ITextSelection) {
					ITextSelection textSelection = (ITextSelection) selection;
					result[0] = new TextSelection(
							textSelection.getOffset(),
							textSelection.getLength(),
							textSelection.getStartLine(),
							textSelection.getEndLine(),
							textSelection.getText());

				} else if (selection instanceof IMarkSelection) {
					IMarkSelection markSelection = (IMarkSelection) selection;
					int offset = markSelection.getOffset();
					int length = markSelection.getLength();
					try {
						result[0] = new TextSelection(
								offset,
								length,
								markSelection.getDocument().getLineOfOffset(offset),
								markSelection.getDocument().getLineOfOffset(offset + length),
								markSelection.getDocument().get(offset, length));

					} catch (BadLocationException e) {
						e.printStackTrace();
						result[0] = new TextSelection(
								markSelection.getOffset(),
								markSelection.getLength(),
								(Integer)null, (Integer)null, null);
					} 
				}
			}
		});
		return result[0];
	}
	
	public static IEditorPart getActiveEditor() {
		IEditorPart[] activeEditor = new IEditorPart[] { null };
		
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				if (workbench != null) {
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					if (window != null) {
						IWorkbenchPage page = window.getActivePage();
						if (page != null && page.getActiveEditor() != null) {
							activeEditor[0] = page.getActiveEditor();
						}
					}
				}
			}
		});
		return activeEditor[0];
	}
	
	public static Editors getEditors() {
		List<Editor> editors = new ArrayList<Editor>();
		for (IWorkbenchWindow ww : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : ww.getPages()) {
				for (IEditorReference reference : page.getEditorReferences()) {
					EditorAdapter adapter = new EditorAdapter(reference);
					editors.add(adapter.toJson());
				}
			}
		}

		return new Editors(editors.toArray(Editor[]::new));
	}
}
