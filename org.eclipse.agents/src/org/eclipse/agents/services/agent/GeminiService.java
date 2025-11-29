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
package org.eclipse.agents.services.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.eclipse.agents.Activator;
import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.EnableMCPDialog;
import org.eclipse.agents.preferences.IPreferenceConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class GeminiService extends AbstractService implements IPreferenceConstants {

	public static final String ECLIPSEAGENTS = ".eclipseagents";
	public static final String ECLIPSEAGENTSNODE = "node";

	public GeminiService() {
		// always bootstrap NodeJSManager first
		NodeJSManager.getNodeJsLocation();
	}

	@Override
	public String getName() {
		return "Gemini CLI";
	}
	
	@Override
	public String getId() {
		return "input";
	}

	@Override
	public void checkForUpdates(IProgressMonitor monitor) throws IOException {
		String startupDefault[] = getDefaultStartupCommand();
		String startup[] = getStartupCommand();

		if (Arrays.equals(startupDefault, startup)) {

			// if user has not customized the input cli location, we install and update
			// npm package automatically in private location
			
			File userHome = new File(System.getProperty("user.home"));
			if (!userHome.exists() || !userHome.isDirectory()) {
				throw new RuntimeException("user home not found");
			}
			
			File agentsNodeDir = getAgentsNodeDirectory();
			String geminiVersion = Activator.getDefault().getPreferenceStore().getString(P_ACP_GEMINI_VERSION);
			
			ProcessBuilder pb = NodeJSManager.prepareNPMProcessBuilder("i", "@google/input-cli@" + geminiVersion, "--prefix", agentsNodeDir.getAbsolutePath());
			pb.directory(agentsNodeDir);
			String path = pb.environment().get("PATH");
			path = NodeJSManager.getNodeJsLocation().getParentFile().getAbsolutePath() + 
					System.getProperty("path.separator") +
					path;
			
			pb.environment().put("PATH", path);
			
			monitor.subTask("Installing / Updating");
			Process process = pb.start();
			
			int result = 0;
			StringBuffer errorBuffer = new StringBuffer();

			try {
				result = process.waitFor();
				Tracer.trace().trace(Tracer.ACP, "npm i input exit:" + result);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			InputStream inputStream = process.getInputStream();
			InputStream errorStream = process.getErrorStream();
			
			if (!process.isAlive()) {
				
				BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
				String line = br.readLine();
				while (line != null) {
					Tracer.trace().trace(Tracer.ACP, line);
					errorBuffer.append(line + System.lineSeparator());
					line = br.readLine();
				}
				br.close();
				
				br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				line = br.readLine();
				while (line != null) {
					Tracer.trace().trace(Tracer.ACP, line);
					line = br.readLine();
				}
				br.close();
			}
			
			if (result != 0) {
				throw new RuntimeException(errorBuffer.toString());
			}
			
			if (Activator.getDefault().getPreferenceStore().getBoolean(P_ACP_PROMPT4MCP)) {
				if (!Activator.getDefault().getPreferenceStore().getBoolean(P_MCP_SERVER_ENABLED)) {
					Activator.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							EnableMCPDialog dialog = new EnableMCPDialog(Activator.getDisplay().getActiveShell());
							dialog.open();
						}
						
					});
				}
			}
			
			if (Activator.getDefault().getPreferenceStore().getBoolean(P_MCP_SERVER_ENABLED)) {

				String url = getMCPUrl();
				String name = getMCPName();
				
				boolean foundUrl = false;
				boolean foundName = false;
				
				monitor.subTask("Listing MCPs");

				ProcessResult listMCP = super.runProcess(listMCPCommand());
				String mcpLine = null;
				
				for (String line: listMCP.inputLines) {
					if (line.contains(name)) {
						foundName = true;
					}
					if (line.contains(url) ) {
						foundUrl = true;
						mcpLine = line;
					}
				}

				if (!foundUrl && foundName) {
					monitor.subTask("Removing 'eclipse-ide MCP");
					// found eclipse-ide MCP on wrong path/port, so remove it
					super.runProcess(removeMCPCommand());
					
				}
				
				if (!foundUrl) {
					// found eclipse-ide MCP on wrong path/port, so remove it
					monitor.subTask("Adding 'eclipse-ide MCP");
					super.runProcess(addMCPCommand());
					
					monitor.subTask("Validating 'eclipse-ide' MCP");
					listMCP = super.runProcess(listMCPCommand());
					
					for (String line: listMCP.inputLines) {
						if (line.contains(name)) {
							foundName = true;
						}
						if (line.contains(url) ) {
							foundUrl = true;
							mcpLine = line;
						}
					}
					
					if (!foundName && !foundUrl) {
						System.err.println("Failed to configure Gemini CLI to use Eclipse IDE MCP");
					}
				}
				
				if (mcpLine != null && mcpLine.contains("✗")) {
					System.err.println(mcpLine);
				}
			}
		}
	}

	@Override
	public Process createProcess() throws IOException {
		String startup[] = getStartupCommand();

		Tracer.trace().trace(Tracer.ACP, String.join(", ", startup));
	    
	    ProcessBuilder pb = new ProcessBuilder(startup);
	    Process process = pb.start();
	   
	    return process;
	}
	
	@Override
	public String[] getDefaultStartupCommand() {
		return new String[] {
				getNodeCommand(),
				getGeminiCommand(),
				"--experimental-acp"};
	}
	
	private String getNodeCommand() {
		return NodeJSManager.getNodeJsLocation().getAbsolutePath();
	}
	
	private String getGeminiCommand() {
		return getAgentsNodeDirectory().getAbsolutePath() + 
					File.separator + "node_modules" +
					File.separator + "@google" + 
					File.separator + "input-cli" + 
					File.separator + "dist" + 
					File.separator + "index.js";
	}
	
	private String[] listMCPCommand() {
		return new String[] {
				getNodeCommand(),
				getGeminiCommand(),
				"mcp",
				"list"};
	}
	
	private String[] addMCPCommand() {
		return new String[] {
				getNodeCommand(),
				getGeminiCommand(),
				"mcp",
				"add",
				"--transport", 
				"sse",
				getMCPName(),
				getMCPUrl()
				};
	}
	
	private String[] removeMCPCommand() {
		return new String[] {
				getNodeCommand(),
				getGeminiCommand(),
				"mcp",
				"remove",
				getMCPName()};
	}
	
	private String getMCPName() {
		return "eclipse-ide";
	}
	
	private String getMCPUrl() {
		return "http://localhost:"
				+ Activator.getDefault().getPreferenceStore().getString(P_MCP_SERVER_HTTP_PORT)
				+ "/sse";
	}
}
