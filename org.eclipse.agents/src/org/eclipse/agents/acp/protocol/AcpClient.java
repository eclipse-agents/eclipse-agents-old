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
package org.eclipse.agents.acp.protocol;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

import org.eclipse.agents.Activator;
import org.eclipse.agents.acp.AcpService;
import org.eclipse.agents.acp.agent.IAgentService;
import org.eclipse.agents.acp.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.Outcome;
import org.eclipse.agents.acp.protocol.AcpSchema.PermissionOption;
import org.eclipse.agents.acp.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.RequestPermissionOutcome;
import org.eclipse.agents.acp.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionNotification;
import org.eclipse.agents.acp.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.WriteTextFileResponse;
import org.eclipse.agents.internal.Tracer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class AcpClient implements IAcpClient {

	IAgentService service;
	
	public AcpClient(IAgentService service) {
		this.service = service;
	}

	@Override
	public CompletableFuture<RequestPermissionResponse> requestPermission(RequestPermissionRequest request) {
		CompletableFuture<RequestPermissionResponse>  future = new CompletableFuture<RequestPermissionResponse>();
		Activator.getDisplay().syncExec(new Runnable() {
			public void run() {
				SelectionDialog dialog = new SelectionDialog(Activator.getDisplay().getActiveShell()) {

					@Override
					protected Control createDialogArea(Composite parent) {
						Composite top = (Composite) super.createDialogArea(parent);
						top.setLayout(new GridLayout(1, true));
						
						Combo combo = new Combo(top, SWT.READ_ONLY);
						for (PermissionOption po: request.options()) {
							combo.add(po.name());
						}
						combo.addModifyListener(new ModifyListener() {
							@Override
							public void modifyText(ModifyEvent arg0) {
								setSelectionResult(new Object[] { 
										request.options()[combo.getSelectionIndex()]
								});
								getOkButton().setEnabled(true);
							}
						});
						return top;
					}
				};
				
				String message =  
						"Agent would like to call " + request.toolCall().toolCallId() + ": " 
								+ request.toolCall().title() + " TODO";
				
				dialog.setMessage(message);
				if (dialog.open() == Dialog.OK) {
					Object result = dialog.getResult()[0];
					if (result instanceof PermissionOption) {
						PermissionOption option = (PermissionOption)result;
						RequestPermissionOutcome outcome = new RequestPermissionOutcome(Outcome.selected, option.optionId());
						future.complete(new RequestPermissionResponse(null, outcome));
					}
				}
			}
		});
		
		
		return future;
	}

	@Override
	public CompletableFuture<ReadTextFileResponse> readTextFile(ReadTextFileRequest request) {
		Path  absolutePath = new Path(request.path());
		CompletableFuture<ReadTextFileResponse> result = new CompletableFuture<ReadTextFileResponse>();
		Activator.getDisplay().syncExec(new Runnable() {
			public void run() {
				ITextEditor editor = findFileEditor(absolutePath);
				if (editor != null) {
	 				IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
					int offset = 0;
					int length = doc.getLength();
					try {
						if (request.line() != null) {
							int line = request.line();
							offset = doc.getLineOffset(line);
							
							if (request.limit() != null) {
								int endLine = line + request.limit() - 1;
								length = doc.getLineOffset(endLine) + doc.getLineLength(endLine);
							}
						}
						Tracer.trace().trace(Tracer.ACP, "read: " + offset +": "  + length);
						String text = doc.get(offset, length);
						result.complete(new ReadTextFileResponse(null, text));
					} catch (BadLocationException e) {
						e.printStackTrace();
						throw new JsonRpcException(e);
					}
				}
			}			
		});

		if (!result.isDone()) {
			IFile file = findFile(absolutePath);
			if (file != null) {
				StringBuffer buffer = new StringBuffer();
				int firstLine = request.line() == null ? 0 : request.line();
				int lineLimit = request.limit() == null ? -1 : request.limit();
				
				try {
					InputStreamReader reader = new InputStreamReader(((IFile)file).getContents());
					BufferedReader breader = new BufferedReader(reader);
					int i = 0;
					String line = breader.readLine();
					
					while (line != null) {
						if (i >= firstLine) {
							if (lineLimit == -1 || i < firstLine + lineLimit) {
								if (!buffer.isEmpty()) {
									buffer.append("\n");
								}
								buffer.append(line);
							}
						}
						line = breader.readLine();
						i++;
					}
					breader.close();
					result.complete(new ReadTextFileResponse(null, buffer.toString()));
				} catch (CoreException e) {
					e.printStackTrace();
					throw new JsonRpcException(e);
				} catch (IOException e) {
					e.printStackTrace();
					throw new JsonRpcException(e);
				}
			}
		}
		
		return result;
	}

	@Override
	public CompletableFuture<WriteTextFileResponse> writeTextFile(WriteTextFileRequest request) {
		Path  absolutePath = new Path(request.path());
		CompletableFuture<WriteTextFileResponse> result = new CompletableFuture<WriteTextFileResponse>();
		Activator.getDisplay().syncExec(new Runnable() {
			public void run() {
				ITextEditor editor = findFileEditor(absolutePath);
				if (editor != null) {
					IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
					doc.set(request.content());
					result.complete(new WriteTextFileResponse(null));
				}
			}
		});

		if (!result.isDone()) {
			IFile file = findFile(absolutePath);
			if (file != null) {
			    try {
			        byte[] bytes = request.content().getBytes(file.getCharset());
			        ByteArrayInputStream newContentStream = new ByteArrayInputStream(bytes);
			        IProgressMonitor monitor = new NullProgressMonitor(); // Or a real progress monitor
			        file.setContents(newContentStream, IFile.NONE, monitor); // IFile.NONE for no update flags
			        result.complete(new WriteTextFileResponse(null));
			    } catch (CoreException e) {
			    	e.printStackTrace();
			    	throw new JsonRpcException(e);
			    } catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					throw new JsonRpcException(e);
				}
			}
		}

		if (!result.isDone()) {
			throw new JsonRpcException(new Exception("write failed"));
		}
		
		return result;
	}

	@Override
	public CompletableFuture<CreateTerminalResponse> terminalCreate(CreateTerminalRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<TerminalOutputResponse> terminalOutput(TerminalOutputRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<ReleaseTerminalResponse> terminalRelease(WaitForTerminalExitRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<WaitForTerminalExitResponse> terminalWaitForExit(CreateTerminalRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<KillTerminalCommandResponse> terminalKill(KillTerminalCommandRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(SessionNotification notification) {
		AcpService.instance().agentNotifies(notification);
	}
	
	private ITextEditor findFileEditor(Path absolutePath) {
		for (IWorkbenchWindow ww : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : ww.getPages()) {
				for (IEditorReference reference : page.getEditorReferences()) {
					IEditorPart part = reference.getEditor(false);
					if (part != null && part instanceof ITextEditor) {
						IEditorInput input = part.getEditorInput();
						if (input instanceof FileEditorInput) {
							IFile file = ((IFileEditorInput)input).getFile();
							if (file.getRawLocation().equals(absolutePath)) {
								return (ITextEditor)part;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private IFile findFile(Path absolutePath) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(absolutePath);
		if (file != null) {
			if (!file.exists()) {
				try {
					file.refreshLocal(0, new NullProgressMonitor());
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

}
