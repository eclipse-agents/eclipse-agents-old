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

import java.util.concurrent.ExecutionException;

import org.eclipse.agents.Activator;
import org.eclipse.agents.Tracer;
import org.eclipse.agents.preferences.IPreferenceConstants;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.protocol.AcpSchema.AuthMethod;
import org.eclipse.agents.services.protocol.AcpSchema.AuthenticateRequest;
import org.eclipse.agents.services.protocol.AcpSchema.AuthenticateResponse;
import org.eclipse.agents.services.protocol.AcpSchema.HttpHeader;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.McpServer;
import org.eclipse.agents.services.protocol.AcpSchema.NewSessionRequest;
import org.eclipse.agents.services.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.agents.services.protocol.AcpSchema.SessionModeState;
import org.eclipse.agents.services.protocol.AcpSchema.SessionModelState;
import org.eclipse.agents.services.protocol.AcpSchema.SseTransport;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;


public class StartSessionJob extends Job {

	// Inputs
	IAgentService service;
	InitializeResponse initializeResponse;
	String oldSessionId;
	
	// Outputs
	String cwd = null;
    McpServer[] mcpServers = null;
    String sessionId = null;
    SessionModeState modes = null;
	SessionModelState models = null;


	public StartSessionJob(IAgentService service, InitializeResponse initializeResponse, String oldSessionId) {
		super("Coding Agent");
		this.service = service;
		this.initializeResponse = initializeResponse; 
		this.oldSessionId = oldSessionId;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		try {
			
			monitor.subTask("Starting session");
			
			boolean supportsSseMcp = initializeResponse.agentCapabilities() != null &&
					initializeResponse.agentCapabilities().mcpCapabilities() != null &&
							initializeResponse.agentCapabilities().mcpCapabilities().sse();
			
			boolean supportsLoadSession = initializeResponse.agentCapabilities() != null &&
					initializeResponse.agentCapabilities().loadSession();
			
			if (oldSessionId != null && supportsLoadSession) {

			} else {

				this.mcpServers = new McpServer[0];
				
				if (supportsSseMcp) {
					Tracer.trace().trace(Tracer.ACP, service.getName() + " supports SSE MCP");
					
					// Use the actual running server state rather than just the preference flag,
					// so the agent receives the MCP URL whenever the server is actually running.
					boolean eclipseMcpRunning = Activator.getDefault().getServerManager() != null
							&& Activator.getDefault().getServerManager().isRunning();

					if (eclipseMcpRunning) {
						String httpPort = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.P_MCP_SERVER_HTTP_PORT);
						Tracer.trace().trace(Tracer.ACP, "Eclipse MCP is running on port " + httpPort);
						
						this.mcpServers = new McpServer[] { new SseTransport(
								new HttpHeader[0],
								"Eclipse MCP",
								"sse",
								"http://localhost:" + httpPort + "/sse")}; 
					} else {
						Tracer.trace().trace(Tracer.ACP, "Eclipse MCP is not running");
					}
				} else {
					Tracer.trace().trace(Tracer.ACP, service.getName() + " does not support SSE MCP");
				}
				
				
				this.cwd = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.P_ACP_WORKING_DIR);
				
				NewSessionRequest newSessionRequest = new NewSessionRequest(
						null,
						this.cwd,
						this.mcpServers);
				
				
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				} 
				
				NewSessionResponse newSessionResponse;
				try {
					newSessionResponse = this.service.getAgent()._new(newSessionRequest).get();
				} catch (ExecutionException sessionEx) {
					// If the agent reports "Authentication required" here it means
					// authenticate() was never called (or the token expired).
					// Try to authenticate now and retry session/new once.
					Throwable cause = sessionEx.getCause();
					boolean authRequired = cause instanceof ResponseErrorException
							&& cause.getMessage() != null
							&& cause.getMessage().toLowerCase().contains("authentication");
					if (authRequired) {
						Tracer.trace().trace(Tracer.ACP,
								"session/new returned 'Authentication required' – attempting authenticate()");
						try {
							InitializeResponse initResp = service.getInitializeResponse();
							AuthMethod[] authMethods = (initResp != null) ? initResp.authMethods() : null;
							String methodId = (authMethods != null && authMethods.length > 0)
									? authMethods[0].id() : null;
							AuthenticateResponse authResp = this.service.getAgent()
									.authenticate(new AuthenticateRequest(null, methodId)).get();
							this.service.setAuthenticateResponse(authResp);
							Tracer.trace().trace(Tracer.ACP, "authenticate() succeeded – retrying session/new");
							// Retry
							newSessionResponse = this.service.getAgent()._new(newSessionRequest).get();
						} catch (Exception retryEx) {
							String hint = service.getName().toLowerCase().contains("copilot")
									? "\n\nPlease run 'copilot login' in a terminal and restart Eclipse."
									: "";
							return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									"Authentication failed for " + service.getName()
									+ ". Cannot start a session." + hint, retryEx);
						}
					} else {
						throw sessionEx;
					}
				}
				this.modes = newSessionResponse.modes();
				this.models = newSessionResponse.models();
				this.sessionId = newSessionResponse.sessionId();
				
				if (AgentController.getSession(this.sessionId) == null) {
					SessionController model = new SessionController(
							service,
							sessionId,
							this.getCwd(),
							this.getMcpServers(),
							this.getModes(),
							this.getModels());
						
					AgentController.putSession(sessionId, model);	
					
						
				} else {
					Tracer.trace().trace(Tracer.CHAT, "prompt: found a pre-existing matching session id");
				}
			}
		} catch (InterruptedException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		} catch (ExecutionException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
		
		return Status.OK_STATUS;
	}

	public String getCwd() {
		return cwd;
	}

	public McpServer[] getMcpServers() {
		return mcpServers;
	}

	public String getSessionId() {
		return sessionId;
	}

	public SessionModeState getModes() {
		return modes;
	}
	
	public SessionModelState getModels() {
		return models;
	}
}
