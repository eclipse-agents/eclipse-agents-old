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
package org.eclipse.agents.internal;

import org.eclipse.agents.IMCPServices;
import org.eclipse.osgi.service.debug.DebugTrace;

import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;

public class MCPServices implements IMCPServices {

	MCPServer server;
	
	public MCPServices(MCPServer server) {
		this.server = server;
	}

	@Override
	public void addResource(SyncResourceSpecification spec) {
		server.addResource(spec);
	}

	@Override
	public void removeResource(String uri) {
		server.removeResource(uri);
	}

	@Override
	public boolean getToolVisibility(String toolName) {
		return server.getVisibility(toolName);
	}

	@Override
	public boolean setToolVisibility(String toolName, boolean isVisible) {
		return server.setVisibility(toolName, isVisible);
	}

	@Override
	public DebugTrace getTracer() {
		return Tracer.trace();
	}
}
