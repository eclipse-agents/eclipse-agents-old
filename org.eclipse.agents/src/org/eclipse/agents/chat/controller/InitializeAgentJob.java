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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.eclipse.agents.Activator;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.protocol.AcpSchema.ClientCapabilities;
import org.eclipse.agents.services.protocol.AcpSchema.FileSystemCapability;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeResponse;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


public class InitializeAgentJob extends Job {

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
			
			monitor.beginTask(service.getName(), 5);
			monitor.subTask("Stopping Agent");
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
			monitor.subTask("Starting Agent");
			service.start();
			
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			
			monitor.worked(1);
			monitor.subTask("Initializing Agent");
			
			FileSystemCapability fsc = new FileSystemCapability(null, true, true);
			ClientCapabilities capabilities = new ClientCapabilities(null, fsc, true);
			InitializeRequest initializeRequest = new InitializeRequest(null, capabilities, 1);
			
			InitializeResponse initializeResponse = this.service.getAgent().initialize(initializeRequest).get();
			this.service.setInitializeRequest(initializeRequest);
			this.service.setInitializeResponse(initializeResponse);

		} catch (InterruptedException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		} catch (ExecutionException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		} catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
		
		return Status.OK_STATUS;
	}
}
