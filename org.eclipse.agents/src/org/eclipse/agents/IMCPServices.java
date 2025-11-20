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
package org.eclipse.agents;

import org.eclipse.agents.internal.Tracer;
import org.eclipse.osgi.service.debug.DebugTrace;

import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;

public interface IMCPServices {

	/**
	 * The option argument extensions must use when using MCP's DebugTrace
	 */
	public final static String EXTENSIONS_TRACE_OPTION = Tracer.THIRDPARTY;
	
	/**
	 * Adds a resource to the server.  
	 * Can be used to dynamically make resources available based on state of IDE.
	 * For example, to dynamically have a resource for each open editor available
	 * @param uri
	 * @param name
	 * @param description
	 * @param mimeType
	 * @return
	 */
	public void addResource(SyncResourceSpecification resource);
	
	/**
	 * Removes a previously added resource from the server
	 * @param uri
	 */
	public void removeResource(String uri);
	
	/**
	 * @param toolName
	 * @return true if toolName is currently available on server
	 */
	public boolean getToolVisibility(String toolName);
	
	/**
	 * Adds / Removes a tool from the server
	 * Can be used to remove declared tools from the server based on user preferences
	 * @param toolName
	 * @param isVisible
	 * @return true only if the tool availability was changed
	 */
	public boolean setToolVisibility(String toolName, boolean isVisible);
	
	/**
	 * Send messages to Eclipse tracer and MCP logger.
	 * Use <code>EXTENSIONS_TRACE_OPTION</code> as the option parameter
	 * @return
	 */
	public DebugTrace getTracer();
}
