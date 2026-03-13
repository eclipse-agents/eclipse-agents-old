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

import org.eclipse.agents.Activator;
import org.eclipse.agents.Tracer;
import org.eclipse.agents.preferences.IPreferenceConstants;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.protocol.AcpSchema.AuthMethod;
import org.eclipse.agents.services.protocol.AcpSchema.AuthenticateRequest;
import org.eclipse.agents.services.protocol.AcpSchema.AuthenticateResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ClientCapabilities;
import org.eclipse.agents.services.protocol.AcpSchema.FileSystemCapability;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeResponse;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


public class InitializeAgentJob extends Job implements IPreferenceConstants {

	// Inputs
	IAgentService service;
	
	// Outputs
	
	public InitializeAgentJob(IAgentService service) {
		super(service.getName());
		this.service = service;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		try {
			
			monitor.beginTask(service.getName(), 6); // +1 for authenticate step
			monitor.subTask("Stopping Agent");
			service.stop();
			
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			monitor.worked(1);
			monitor.subTask("Checking for updates");
			service.checkForUpdates(monitor);
			
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			
			monitor.worked(1);
			monitor.subTask("Starting Agent");
			service.start();
			
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			
			monitor.worked(1);
			monitor.subTask("Initializing Agent");
			
			// Check if agent service is running
			if (!this.service.isRunning()) {
				return new Status(IStatus.ERROR, org.eclipse.agents.Activator.PLUGIN_ID,
					"Agent service failed to start. Check preferences and trace logs.");
			}
			
			FileSystemCapability fsc = new FileSystemCapability(null, 
					Activator.getDefault().getPreferenceStore().getBoolean(P_ACP_FILE_READ),
					Activator.getDefault().getPreferenceStore().getBoolean(P_ACP_FILE_WRITE));

			ClientCapabilities capabilities = new ClientCapabilities(null, fsc, true);
			InitializeRequest initializeRequest = new InitializeRequest(null, capabilities, 1);
			
			InitializeResponse initializeResponse = this.service.getAgent().initialize(initializeRequest).get();
			this.service.setInitializeRequest(initializeRequest);
			this.service.setInitializeResponse(initializeResponse);

			monitor.worked(1);

			// -----------------------------------------------------------------
			// Authenticate if the agent advertises authentication methods.
			// The @github/copilot ACP server requires this before session/new.
			// -----------------------------------------------------------------
			AuthMethod[] authMethods = initializeResponse.authMethods();
			if (authMethods != null && authMethods.length > 0) {
				String methodId = authMethods[0].id();
				Tracer.trace().trace(Tracer.ACP,
						"Agent requires authentication – method: " + methodId);
				monitor.subTask("Authenticating with " + service.getName());
				try {
					AuthenticateResponse authResponse = this.service.getAgent()
							.authenticate(new AuthenticateRequest(null, methodId)).get();
					this.service.setAuthenticateResponse(authResponse);
					Tracer.trace().trace(Tracer.ACP, "Authentication succeeded.");
				} catch (Exception authEx) {
					String hint = "";
					if (service.getName().toLowerCase().contains("copilot")) {
						hint = "\n\nRun the following command in a terminal and then restart Eclipse:\n"
								+ "  copilot login";
					}
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							"Authentication failed for " + service.getName() + "." + hint,
							authEx);
				}
			} else {
				Tracer.trace().trace(Tracer.ACP, "No authentication methods advertised – skipping authenticate step.");
			}

			monitor.worked(1);

		} catch (Exception e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
		
		return Status.OK_STATUS;
	}
}
