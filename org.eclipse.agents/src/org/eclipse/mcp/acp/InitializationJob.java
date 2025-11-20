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
package org.eclipse.mcp.acp;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mcp.Activator;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientCapabilities;
import org.eclipse.mcp.acp.protocol.AcpSchema.FileSystemCapability;
import org.eclipse.mcp.acp.protocol.AcpSchema.HttpHeader;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.McpServer;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionModeState;
import org.eclipse.mcp.acp.protocol.AcpSchema.SseTransport;
import org.eclipse.mcp.internal.Tracer;
import org.eclipse.mcp.internal.preferences.IPreferenceConstants;


public class InitializationJob extends Job {

	// Inputs
	IAgentService service;
	String oldSessionId;
	
	// Outputs
	String cwd = null;
    McpServer[] mcpServers = null;
    String sessionId = null;
    SessionModeState modes = null;


	public InitializationJob(IAgentService service, String oldSessionId) {
		super("Coding Agent");
		this.service = service;
		this.oldSessionId = oldSessionId;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		try {
			
			monitor.beginTask(service.getName(), 5);
			monitor.subTask("Stopping");
			service.stop();
			
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			monitor.worked(1);
			monitor.subTask("Checking for updates");
			service.checkForUpdates();
			
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			
			monitor.worked(1);
			monitor.subTask("Starting");
			service.start();
			
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			
			
			monitor.worked(1);
			monitor.subTask("Connecting");
			
			FileSystemCapability fsc = new FileSystemCapability(null, true, true);
			ClientCapabilities capabilities = new ClientCapabilities(null, fsc, true);
			InitializeRequest initializeRequest = new InitializeRequest(null, capabilities, 1);
			
			InitializeResponse initializeResponse = this.service.getAgent().initialize(initializeRequest).get();
			this.service.setInitializeRequest(initializeRequest);
			this.service.setInitializeResponse(initializeResponse);

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			} 

			monitor.worked(1);
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
					
					boolean eclipseMcpEnabled = Activator.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.P_SERVER_ENABLED);
					
					if (eclipseMcpEnabled) {
						String httpPort = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.P_SERVER_HTTP_PORT);
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
				
				
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				this.cwd = root.getRawLocation().toOSString();
				
				NewSessionRequest newSessionRequest = new NewSessionRequest(
						null,
						this.cwd,
						this.mcpServers);
				
				
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				} 
				
				NewSessionResponse newSessionResponse = this.service.getAgent()._new(newSessionRequest).get();
				this.modes = newSessionResponse.modes();
				this.sessionId = newSessionResponse.sessionId();
			}
		} catch (InterruptedException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		} catch (ExecutionException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		} catch (IOException e) {
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
}
