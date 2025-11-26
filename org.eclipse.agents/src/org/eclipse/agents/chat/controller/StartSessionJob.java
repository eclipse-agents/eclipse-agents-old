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
					
					boolean eclipseMcpEnabled = Activator.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.P_MCP_SERVER_ENABLED);
					
					if (eclipseMcpEnabled) {
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
				
				NewSessionResponse newSessionResponse = this.service.getAgent()._new(newSessionRequest).get();
				this.modes = newSessionResponse.modes();
				this.models = newSessionResponse.models();
				this.sessionId = newSessionResponse.sessionId();
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
