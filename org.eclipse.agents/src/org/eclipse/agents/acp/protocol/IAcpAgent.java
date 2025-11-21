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

import java.util.concurrent.CompletableFuture;

import org.eclipse.agents.acp.protocol.AcpSchema.AuthenticateRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.AuthenticateResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.CancelNotification;
import org.eclipse.agents.acp.protocol.AcpSchema.InitializeRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.LoadSessionRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.LoadSessionResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.NewSessionRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.PromptRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.PromptResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.SetSessionModeRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.SetSessionModeResponse;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface IAcpAgent {

	@JsonRequest
	CompletableFuture<InitializeResponse> initialize(InitializeRequest request);
	
	@JsonRequest
	CompletableFuture<AuthenticateResponse> authenticate(AuthenticateRequest request);
	
	@JsonRequest(value = "session/new")
	CompletableFuture<NewSessionResponse> _new(NewSessionRequest request);
	
	@JsonRequest(value = "session/load")
	CompletableFuture<LoadSessionResponse> load(LoadSessionRequest Response);
	
	@JsonRequest(value = "session/set_mode")
	CompletableFuture<SetSessionModeResponse> set_mode(SetSessionModeRequest request);
	
	@JsonRequest(value = "session/prompt")
	CompletableFuture<PromptResponse> prompt(PromptRequest request);
	
	@JsonNotification(value = "session/cancel")
	void cancel(CancelNotification request);
	
}

