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
package org.eclipse.agents.services.protocol;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.chat.controller.workspace.WorkspaceController;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.agents.services.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.agents.services.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.agents.services.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.agents.services.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.agents.services.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.agents.services.protocol.AcpSchema.SessionNotification;
import org.eclipse.agents.services.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.agents.services.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.agents.services.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileResponse;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.texteditor.ITextEditor;

public class AcpClient implements IAcpClient {

	IAgentService service;
	
	public AcpClient(IAgentService service) {
		this.service = service;
	}

	@Override
	public CompletableFuture<RequestPermissionResponse> requestPermission(RequestPermissionRequest request) {
		CompletableFuture<RequestPermissionResponse> future = new CompletableFuture<RequestPermissionResponse>();
		
		Activator.getDisplay().syncExec(new Runnable() {
			public void run() {
				AgentController.instance().acceptRequestsPermission(request, future);
			}
		});
		
		return future;
	}

	@Override
	public CompletableFuture<ReadTextFileResponse> readTextFile(ReadTextFileRequest request) {
		AgentController.instance().agentRequests(request);
		
		Path  absolutePath = new Path(request.path());
		CompletableFuture<ReadTextFileResponse> result = new CompletableFuture<ReadTextFileResponse>();
		WorkspaceController workspaceController = AgentController.getSession(request.sessionId()).getWorkspaceController();
		ITextEditor editor = WorkspaceController.findFileEditor(absolutePath);
		
		if (editor != null) {
			Activator.getDisplay().asyncExec(new Runnable() {
				public void run() {
					result.complete(new ReadTextFileResponse(null, 
							workspaceController.readFromEditor(editor, request.line(), request.limit())));
					
				}			
			});
		} else {
			new Thread() {
				@Override
				public void run() {
					result.complete(new ReadTextFileResponse(null, 
							workspaceController.readFromFile(absolutePath, request.line(), request.limit())));
				}
				
			}.start();
			
		}
		
		return result;
	}

	@Override
	public CompletableFuture<WriteTextFileResponse> writeTextFile(WriteTextFileRequest request) {
		AgentController.instance().agentRequests(request);
		
		Path absolutePath = new Path(request.path());
		CompletableFuture<WriteTextFileResponse> result = new CompletableFuture<WriteTextFileResponse>();
		WorkspaceController workspaceController = AgentController.getSession(request.sessionId()).getWorkspaceController();

		new Thread() {
			@Override
			public void run() {
				Activator.getDisplay().syncExec(new Runnable() {
					public void run() {
						ITextEditor editor = WorkspaceController.findFileEditor(absolutePath);
						if (editor != null) {
							workspaceController.writeToEditor(absolutePath, editor, request.content());
							result.complete(new WriteTextFileResponse(null));
						}
					}			
				});
				if (!result.isDone()) {
					workspaceController.writeToFile(absolutePath, request.content());
					result.complete(new WriteTextFileResponse(null));
				}
			}
			
		}.start();

		return result;
	}

	@Override
	public CompletableFuture<CreateTerminalResponse> terminalCreate(CreateTerminalRequest request) {
		AgentController.instance().agentRequests(request);
		return null;
	}

	@Override
	public CompletableFuture<TerminalOutputResponse> terminalOutput(TerminalOutputRequest request) {
		AgentController.instance().agentRequests(request);
		return null;
	}

	@Override
	public CompletableFuture<ReleaseTerminalResponse> terminalRelease(WaitForTerminalExitRequest request) {
		AgentController.instance().agentRequests(request);
		return null;
	}

	@Override
	public CompletableFuture<WaitForTerminalExitResponse> terminalWaitForExit(CreateTerminalRequest request) {
		AgentController.instance().agentRequests(request);
		return null;
	}

	@Override
	public CompletableFuture<KillTerminalCommandResponse> terminalKill(KillTerminalCommandRequest request) {
		AgentController.instance().agentRequests(request);
		return null;
	}

	@Override
	public void update(SessionNotification notification) {
		AgentController.instance().agentNotifies(notification);
	}

}
