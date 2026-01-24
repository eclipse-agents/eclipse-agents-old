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
import org.eclipse.agents.LogHelper;
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
	public String getFolderName() {
		return "gemini";
	}
	
	@Override
	public String getId() {
		return "gemini";
	}

	/**
	 * Installs Gemini CLI at the specified version.
	 * 
	 * @param version the version to install (e.g., "0.9.0")
	 * @param monitor progress monitor for reporting progress
	 * @throws IOException if installation fails
	 */
	public void installGemini(String version, IProgressMonitor monitor) throws IOException {
		File agentsNodeDir = getAgentsNodeDirectory();
		
		LogHelper.logInfo("Installing Gemini CLI v" + version + " to " + agentsNodeDir.getAbsolutePath());
		LogHelper.logInfo("Starting Gemini CLI installation: version=" + version + ", directory=" + agentsNodeDir);
		
		// Use npm install without --prefix for proper dependency resolution
		// cd to directory and run npm install there
		ProcessBuilder pb = NodeJSManager.prepareNPMProcessBuilder("install", "@google/gemini-cli@" + version);
		
		pb.directory(agentsNodeDir);
		String path = pb.environment().get("PATH");
		path = NodeJSManager.getNodeJsLocation().getParentFile().getAbsolutePath() + 
				System.getProperty("path.separator") +
				path;
		pb.environment().put("PATH", path);
		
		LogHelper.logInfo("Running npm install command in directory: " + agentsNodeDir);
		monitor.subTask("Running npm install command in directory " + agentsNodeDir.getAbsolutePath());
		
		ProcessResult result = runProcess(pb);
		
		if (result.result != 0) {
			String errorMessage = String.join("\n", result.errorLines);
			if (errorMessage.isEmpty()) {
				errorMessage = "npm install failed with exit code " + result.result;
			}
			LogHelper.logError("Failed to install Gemini CLI v" + version + ": " + errorMessage);
			throw new IOException(errorMessage);
		}
		
		LogHelper.logInfo("Successfully installed Gemini CLI v" + version);
		monitor.subTask("Installation complete");
	}
	
	/**
	 * Uninstalls Gemini CLI by removing the entire gemini folder from .eclipseagents directory.
	 * 
	 * @param monitor progress monitor for reporting progress
	 * @throws IOException if uninstallation fails
	 */
	public void uninstallGemini(IProgressMonitor monitor) throws IOException {
		// Get the gemini directory path without creating it
		File geminiDir = new File(System.getProperty("user.home") + File.separator + ECLIPSEAGENTS + File.separator + getFolderName());
		
		LogHelper.logInfo("Uninstalling Gemini CLI from " + geminiDir.getAbsolutePath());
		LogHelper.logInfo("Starting Gemini CLI uninstallation from: " + geminiDir);
		
		// Remove the entire gemini directory
		if (geminiDir.exists()) {
			LogHelper.logInfo("Deleting gemini directory and all its contents");
			monitor.subTask("Deleting directory " + geminiDir.getAbsolutePath());
			deleteDirectory(geminiDir);
		}
		
		LogHelper.logInfo("Successfully uninstalled Gemini CLI");
		monitor.subTask("Uninstallation complete");
	}
	
	/**
	 * Recursively deletes a directory and all its contents.
	 */
	private void deleteDirectory(File directory) throws IOException {
		LogHelper.logInfo("Deleting directory: " + directory.getAbsolutePath());
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					if (!file.delete()) {
						String errorMsg = "Failed to delete file: " + file.getAbsolutePath();
						LogHelper.logError(errorMsg);
						throw new IOException(errorMsg);
					}
				}
			}
		}
		if (!directory.delete()) {
			String errorMsg = "Failed to delete directory: " + directory.getAbsolutePath();
			LogHelper.logError(errorMsg);
			throw new IOException(errorMsg);
		}
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
			
			monitor.subTask("Checking Version");
			
			ProcessBuilder pb =  NodeJSManager.prepareNPMProcessBuilder("i", "@google/gemini-cli@" + geminiVersion, "--prefix", agentsNodeDir.getAbsolutePath());
			
			pb.directory(agentsNodeDir);
			String path = pb.environment().get("PATH");
			path = NodeJSManager.getNodeJsLocation().getParentFile().getAbsolutePath() + 
					System.getProperty("path.separator") +
					path;
			pb.environment().put("PATH", path);
			
			monitor.subTask("Installing / Updating");
			ProcessResult result = runProcess(pb);
			
			if (result.result != 0) {
				throw new RuntimeException(String.join("\n", result.errorLines));
			}
			
			if (Activator.getDefault().getPreferenceStore().getBoolean(P_ACP_PROMPT4MCP)) {
				if (!Activator.getDefault().getPreferenceStore().getBoolean(P_MCP_SERVER_ENABLED)) {
					Activator.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
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
				System.out.println("Failed to configure Gemini CLI to use Eclipse IDE MCP");
			}
			}
			
			if (mcpLine != null && mcpLine.contains("✗")) {
				System.out.println("MCP configuration issue: " + mcpLine);
			}
			}
		}
	}

	@Override
	public Process createProcess() throws IOException {
		String startup[] = getStartupCommand();

		System.out.println(String.join(", ", startup));
	    
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
					File.separator + "gemini-cli" + 
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
				"--scope", 
				"user",
				"--transport", 
				"sse",
				getMCPName(),
				getMCPUrl()
				};
	}
	
	public String getVersion() {
		System.out.println("Checking Gemini CLI version");
		System.out.println("  isInstalled(): " + isInstalled());
		System.out.println("  Install directory: " + getAgentsNodeDirectory().getAbsolutePath());
		System.out.println("  Install directory exists: " + getAgentsNodeDirectory().exists());
		
		File geminiIndexFile = new File(getGeminiCommand());
		System.out.println("  Gemini index.js path: " + geminiIndexFile.getAbsolutePath());
		System.out.println("  Gemini index.js exists: " + geminiIndexFile.exists());
		
		if (isInstalled()) {
			String[] command = new String[] {
				getNodeCommand(),
				getGeminiCommand(),
				"--version"};
			
			System.out.println("  Running command: " + String.join(" ", command));
			
			ProcessResult result = super.runProcess(command);
			
			System.out.println("  Process exit code: " + result.result);
			System.out.println("  Output lines: " + result.inputLines.size());
			for (String line: result.inputLines) {
				System.out.println("  OUTPUT: " + line);
			}
			System.out.println("  Error lines: " + result.errorLines.size());
			for (String line: result.errorLines) {
				System.out.println("  ERROR: " + line);
			}
			
			if (result.result == 0 && !result.inputLines.isEmpty()) {
				String version = result.inputLines.get(0);
				System.out.println("  Returning version: " + version);
				return version;
			}
		}
		
		System.out.println("  Gemini CLI not found");
		return "Not found";

	}	
	
	private String[] removeMCPCommand() {
		return new String[] {
				getNodeCommand(),
				getGeminiCommand(),
				"mcp",
				"remove",
				"--scope",
				"user",
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
