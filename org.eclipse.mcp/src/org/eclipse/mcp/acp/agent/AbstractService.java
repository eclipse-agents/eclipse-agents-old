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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mcp.Activator;
import org.eclipse.mcp.acp.protocol.AcpClient;
import org.eclipse.mcp.acp.protocol.AcpClientLauncher;
import org.eclipse.mcp.acp.protocol.AcpClientThread;
import org.eclipse.mcp.acp.protocol.AcpSchema.AuthenticateResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.protocol.IAcpAgent;

public abstract class AbstractService implements IAgentService {

	public static final String ECLIPSEAGENTS = ".eclipseagents";
	public static final String ECLIPSEAGENTSNODE = "node";

	private AcpClientThread thread;
	private Process agentProcess;
	private InputStream inputStream;
	private OutputStream outputStream;
	private InputStream errorStream;
	
	private InitializeRequest initializeRequest;
	private InitializeResponse initializeResponse;
	private AuthenticateResponse authenticateResponse;

	
	public AbstractService() {
		
	}
	
	public String[] getStartupCommand() {
		return Activator.getDefault().getPreferenceStore().getString(getStartupCommandPreferenceId()).split("\n");
	}
	
	public String getStartupCommandPreferenceId() {
		return Activator.PLUGIN_ID + ".acp.agent.startup." + getId();
	}
	
	public File getAgentsNodeDirectory() {

		File userHome = new File(System.getProperty("user.home"));
		if (!userHome.exists() || !userHome.isDirectory()) {
			throw new RuntimeException("user home not found");
		}
		
		File agentsHome = new File(userHome + File.separator + ECLIPSEAGENTS);

	    if (!agentsHome.exists()) {
	    	if (!agentsHome.mkdirs()) {
	    		throw new RuntimeException("Could not create " + ECLIPSEAGENTS + " in user home directory");
	    	}
	    }
	    
	    File agentsNode= new File(userHome + File.separator + ECLIPSEAGENTS + File.separator + ECLIPSEAGENTSNODE);

	    if (!agentsNode.exists()) {
	    	if (!agentsNode.mkdirs()) {
	    		throw new RuntimeException("Could not create " + ECLIPSEAGENTSNODE + " in user home directory");
	    	}
	    }
	    return agentsNode;
	}


	public abstract Process createProcess() throws IOException;
	
	@Override
	public void start() {
				
		try {
			agentProcess = createProcess();
			inputStream = agentProcess.getInputStream();
			outputStream = agentProcess.getOutputStream();
			errorStream = agentProcess.getErrorStream();
			
			
			if (!agentProcess.isAlive()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
				String line = br.readLine();
				while (line != null) {
					System.err.println(line);
					line = br.readLine();
				}
				return;
			} else {
				final Process _agentProcess = agentProcess; 
				new Thread("ACP Error Thread") {
					public void run() {
						try {
							BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
							while (_agentProcess.isAlive()) {
								String line = br.readLine();
								System.err.println(line);
							}
						} catch (IOException e) {
								e.printStackTrace();
						}							
					}
				}.start();
			}
			
			AcpClient acpClient = new AcpClient(this);
			AcpClientLauncher launcher = new AcpClientLauncher(acpClient, inputStream, outputStream);
			thread = new AcpClientThread(launcher) {
				@Override
				public void statusChanged() {
					System.err.println(getStatus());
				}
			};
			thread.start();
			
			agentProcess.onExit().thenRun(new Runnable() {
				@Override
				public void run() {
					int exitValue = agentProcess.exitValue();
					String output = null;
					String errorString = null;

					System.out.println("Gemini Exit:" + exitValue);
				}
			});

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		if (agentProcess != null) {
			agentProcess.destroy();
		}
	}
	
	@Override
	public boolean isRunning() {
		return agentProcess != null && agentProcess.isAlive();
	}

	@Override
	public IAcpAgent getAgent() {
		return thread.getAgent();
	}

	@Override
	public abstract String getName();

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public InputStream getErrorStream() {
		return errorStream;
	}

	@Override
	public InitializeRequest getInitializeRequest() {
		return initializeRequest;
	}

	@Override
	public void setInitializeRequest(InitializeRequest initializeRequest) {
		this.initializeRequest = initializeRequest;
	}

	@Override
	public InitializeResponse getInitializeResponse() {
		return initializeResponse;
	}

	@Override
	public void setInitializeResponse(InitializeResponse initializeResponse) {
		this.initializeResponse = initializeResponse;
	}

	@Override
	public AuthenticateResponse getAuthenticateResponse() {
		return authenticateResponse;
	}

	@Override
	public void setAuthenticateResponse(AuthenticateResponse authenticateResponse) {
		this.authenticateResponse = authenticateResponse;
	}

}
