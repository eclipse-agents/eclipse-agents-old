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
package org.eclipse.agents.chat.controller;

import org.eclipse.agents.services.protocol.AcpSchema.CancelNotification;
import org.eclipse.agents.services.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.agents.services.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.agents.services.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.agents.services.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.agents.services.protocol.AcpSchema.PromptRequest;
import org.eclipse.agents.services.protocol.AcpSchema.PromptResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ReleaseTerminalRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.agents.services.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.agents.services.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.agents.services.protocol.AcpSchema.SessionNotification;
import org.eclipse.agents.services.protocol.AcpSchema.SetSessionModeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.SetSessionModeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.agents.services.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.agents.services.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileResponse;

public interface ISessionListener {

	public String getSessionId();

	//AgentNotification
	public void accept(SessionNotification notification);
	
	//AgentRequest
	public void accept(WriteTextFileRequest request);
	public void accept(ReadTextFileRequest request);
	public void accept(RequestPermissionRequest request);
	public void accept(CreateTerminalRequest request);
	public void accept(TerminalOutputRequest request);
	public void accept(ReleaseTerminalRequest request);
	public void accept(WaitForTerminalExitRequest request);
	public void accept(KillTerminalCommandRequest request);
	
	//AgentResponse
//	public void accept(InitializeResponse response);
//	public void accept(AuthenticateResponse response);
//	public void accept(NewSessionResponse response);
//	public void accept(LoadSessionResponse response);
	public void accept(SetSessionModeResponse response);
	public void accept(PromptResponse response);
	
	//ClientNotification
	public void accept(CancelNotification notification);
	
	//ClientRequest
//	public void accept(InitializeRequest request);
//	public void accept(AuthenticateRequest request);
//	public void accept(NewSessionRequest request);
//	public void accept(LoadSessionRequest request);
	public void accept(SetSessionModeRequest request);
	public void accept(PromptRequest request);
	
	//ClientResponse
	public void accept(WriteTextFileResponse response);
	public void accept(ReadTextFileResponse response);
	public void accept(RequestPermissionResponse response);
	public void accept(CreateTerminalResponse response);
	public void accept(TerminalOutputResponse response);
	public void accept(ReleaseTerminalResponse response);
	public void accept(WaitForTerminalExitResponse response);
	public void accept(KillTerminalCommandResponse response);
}
