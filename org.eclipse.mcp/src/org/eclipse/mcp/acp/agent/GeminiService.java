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
package org.eclipse.mcp.acp.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.mcp.internal.Tracer;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class GeminiService extends AbstractService {

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
		return "gemini";
	}

	@Override
	public Process createProcess() throws IOException {


		String startupDefault[] = getDefaultStartupCommand();
		String startup[] = getStartupCommand();

		if (Arrays.equals(startupDefault, startup)) {

			// if user has not customized the gemini cli location, we install and update
			// npm package automatically in private location
			
			File userHome = new File(System.getProperty("user.home"));
			if (!userHome.exists() || !userHome.isDirectory()) {
				throw new RuntimeException("user home not found");
			}
			
			File agentsNodeDir = getAgentsNodeDirectory();

		    ProcessBuilder pb = NodeJSManager.prepareNPMProcessBuilder("i", "@google/gemini-cli");
		    pb.directory(agentsNodeDir);
		    Map<?,?> env = pb.environment();
		    String path = pb.environment().get("PATH");
		    path = NodeJSManager.getNodeJsLocation().getParentFile().getAbsolutePath() + 
		    		System.getProperty("path.separator") +
		    		path;
		    
		    pb.environment().put("PATH", path);
	        
		    Process process = pb.start();
		    
		    try {
				int result = process.waitFor();
				Tracer.trace().trace(Tracer.ACP, "npm i gemini exit:" + result);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    
		    InputStream inputStream = process.getInputStream();
		    InputStream errorStream = process.getErrorStream();
			
			if (!process.isAlive()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
				String line = br.readLine();
				while (line != null) {
					System.err.println(line);
					line = br.readLine();
				}
				br.close();
				
				br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				line = br.readLine();
				while (line != null) {
					System.err.println(line);
					line = br.readLine();
				}
				br.close();
			}
			
		}
		
		
		
	    System.err.println(startup);
	    
	    ProcessBuilder pb = new ProcessBuilder(startup);
	    Process process = pb.start();
	   
	    return process;
		
	}
	
	public String[] getDefaultStartupCommand() {
		
		return new String[] {
				NodeJSManager.getNodeJsLocation().getAbsolutePath(),
				getAgentsNodeDirectory().getAbsolutePath() + 
					File.separator + "node_modules" +
					File.separator + "@google" + 
					File.separator + "gemini-cli" + 
					File.separator + "dist" + 
					File.separator + "index.js",
				"--experimental-acp"};

	}
}
