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
	
	public static final String P_ACP_FILE_READ = Activator.PLUGIN_ID + ".default.acp.file.read"; //$NON-NLS-1$
	
	public static final String P_ACP_FILE_WRITE = Activator.PLUGIN_ID + ".default.acp.file.write"; //$NON-NLS-1$
	
	public static final String P_ACP_PROMPT4MCP = Activator.PLUGIN_ID + ".default.acp.prompt4mcp"; //$NON-NLS-1$
	
	public static final String P_ACP_GEMINI_VERSION= Activator.PLUGIN_ID + ".default.acp.gemini.version"; //$NON-NLS-1$

	public static final String P_ACP_COPILOT_GH_PATH = Activator.PLUGIN_ID + ".default.acp.copilot.gh.path"; //$NON-NLS-1$

	/**
	 * Absolute path to the gh-copilot extension binary.
	 * When non-blank the plugin calls &lt;code&gt;{binary} acp&lt;/code&gt; directly,
	 * bypassing the GitHub CLI (gh) entirely.
	 */
	public static final String P_ACP_COPILOT_DIRECT_PATH = Activator.PLUGIN_ID + ".default.acp.copilot.direct.path"; //$NON-NLS-1$

	/**
	 * Path to the {@code copilot} binary installed by {@code npm install -g @github/copilot}.
	 * May be a simple command name (e.g. {@code "copilot"}) when the npm global bin
	 * directory is on the PATH, or a full absolute path.
	 * When non-blank the plugin starts the agent as &lt;code&gt;copilot --acp&lt;/code&gt;,
	 * bypassing the GitHub CLI entirely.
	 */
	public static final String P_ACP_COPILOT_NPM_PATH = Activator.PLUGIN_ID + ".default.acp.copilot.npm.path"; //$NON-NLS-1$

}
