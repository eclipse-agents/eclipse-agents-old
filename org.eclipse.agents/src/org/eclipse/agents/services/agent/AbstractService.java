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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.agents.Activator;
import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.chat.controller.InitializeAgentJob;
import org.eclipse.agents.services.protocol.AcpClient;
import org.eclipse.agents.services.protocol.AcpClientLauncher;
import org.eclipse.agents.services.protocol.AcpClientThread;
import org.eclipse.agents.services.protocol.AcpSchema.AuthenticateResponse;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeResponse;
import org.eclipse.agents.services.protocol.IAcpAgent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

public abstract class AbstractService implements IAgentService {

	public static final String ECLIPSEAGENTS = ".eclipseagents";

	InitializeAgentJob initializeJob = null;
	
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
		
		File agentsHome = new File(System.getProperty("user.home") + File.separator + ECLIPSEAGENTS);

	    if (!agentsHome.exists()) {
	    	if (!agentsHome.mkdirs()) {
	    		throw new RuntimeException("Could not create " + ECLIPSEAGENTS + " in user home directory");
	    	}
	    }
	    
	    File agentsNode= new File(System.getProperty("user.home") + File.separator + ECLIPSEAGENTS + File.separator + getFolderName());

	    if (!agentsNode.exists()) {
	    	if (!agentsNode.mkdirs()) {
	    		throw new RuntimeException("Could not create " + getFolderName() + " in user home directory");
	    	}
	    }
	    return agentsNode;
	}
	
	protected boolean isInstalled() {
		return new File(System.getProperty("user.home") + File.separator + ECLIPSEAGENTS + File.separator + getFolderName()).exists();
	}


	public abstract Process createProcess() throws IOException;
	
	@Override 
	public void schedule() {
		if (!isRunning()) {
			if (initializeJob == null || initializeJob.getResult() != null) {
				initializeJob = new InitializeAgentJob(this);
				initializeJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						if (event.getJob().getResult().isOK()) {
							AgentController.instance().agentStarted(AbstractService.this);
						} else {
							Tracer.trace().trace(Tracer.CHAT, "initialization job has an error");
							Tracer.trace().trace(Tracer.CHAT, event.getJob().getResult().getMessage(), event.getJob().getResult().getException());
							if (event.getJob().getResult().getException() != null) {
								event.getJob().getResult().getException().printStackTrace();
							}
							AgentController.instance().agentFailed(AbstractService.this);
							AbstractService.this.initializeJob = null;
						}
					}
				});
				initializeJob.schedule();
			} else {
				Tracer.trace().trace(Tracer.ACP, "Initialize Job already running");
			}
		} else {
			Tracer.trace().trace(Tracer.ACP, "Agent service already running");
		}
	}

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
					Tracer.trace().trace(Tracer.ACP, line);
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
								Tracer.trace().trace(Tracer.ACP, line);
							}
						} catch (IOException e) {
							Tracer.trace().trace(Tracer.ACP, e.getMessage(), e);
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
					Tracer.trace().trace(Tracer.ACP, getStatus().getMessage(), getStatus().getException());
				}
			};
			thread.start();
			
			agentProcess.onExit().thenRun(new Runnable() {
				@Override
				public void run() {
					int exitValue = agentProcess.exitValue();
					String output = null;
					String errorString = null;

					Tracer.trace().trace(Tracer.ACP, "Gemini Exit:" + exitValue);
				}
			});

		} catch (UnsupportedEncodingException e) {
			Tracer.trace().trace(Tracer.ACP, "Error: ", e);
			e.printStackTrace();
		} catch (IOException e) {
			Tracer.trace().trace(Tracer.ACP, "Error: ", e);
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		if (agentProcess != null) {
			agentProcess.destroy();
		}
		AgentController.instance().agentStopped(AbstractService.this);
	}
	
	@Override
	public void unschedule() {
		if (isScheduled()) {
			initializeJob.cancel();
		}
	}
	
	@Override
	public boolean isRunning() {
		return agentProcess != null && agentProcess.isAlive();
	}
	
	@Override
	public boolean isScheduled() {
		return initializeJob != null && initializeJob.getResult() == null;
	}
	
	@Override
	public IStatus getStatus() {
		if (isRunning() || isScheduled()) {
			return Status.OK_STATUS;
		}
		if (initializeJob == null) {
			return null;
		}
		return initializeJob.getResult();
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
	
	public class ProcessResult {
		int result;
		List<String> errorLines = new ArrayList<String>();
		List<String> inputLines = new ArrayList<String>();
		Throwable ex;
	}
	
	protected ProcessResult runProcess(String[] command) {
	    ProcessBuilder pb = new ProcessBuilder(command);
		return runProcess(pb);
	}

	protected ProcessResult runProcess(ProcessBuilder builder) {

		Tracer.trace().trace(Tracer.ACP, String.join(", ", builder.command()));

		ProcessResult result = new ProcessResult();
		
		try {
			Process process = builder.start();
			result.result = process.waitFor();
			Tracer.trace().trace(Tracer.ACP, "Result:" + result);
		
			inputStream = process.getInputStream();
			errorStream = process.getErrorStream();

			if (!process.isAlive()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
				String line = br.readLine();
				while (line != null) {
					Tracer.trace().trace(Tracer.ACP, line);
					result.errorLines.add(line);
					line = br.readLine();
				}
				br.close();
				
				br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				line = br.readLine();
				while (line != null) {
					Tracer.trace().trace(Tracer.ACP, line);
					result.inputLines.add(line);
					line = br.readLine();
				}
				br.close();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			Tracer.trace().trace(Tracer.ACP, "", e);
			result.ex = e;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Tracer.trace().trace(Tracer.ACP, "", e);
			result.ex = e;
		} catch (IOException e) {
			Tracer.trace().trace(Tracer.ACP, "", e);
			e.printStackTrace();
			result.ex = e;
		}
		return result;
	}

}
