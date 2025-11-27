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
package org.eclipse.agents.preferences;

import org.eclipse.agents.Activator;

public interface IPreferenceConstants {

	public static final String P_MCP_SERVER_ENABLED = Activator.PLUGIN_ID + ".default.mcp.enabled"; //$NON-NLS-1$

	public static final String P_MCP_SERVER_HTTP_PORT = Activator.PLUGIN_ID + ".default.mcp.http.port"; //$NON-NLS-1$
	
	public static final String P_ACP_WORKING_DIR = Activator.PLUGIN_ID + ".default.acp.cwd"; //$NON-NLS-1$
	
	public static final String P_ACP_PROMPT4MCP = Activator.PLUGIN_ID + ".default.acp.prompt4mcp"; //$NON-NLS-1$
	
	public static final String P_ACP_GEMINI_VERSION= Activator.PLUGIN_ID + ".default.acp.gemini.version"; //$NON-NLS-1$

}
