package org.eclipse.mcp.acp.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.mcp.acp.protocol.AcpClient;
import org.eclipse.mcp.acp.protocol.AcpClientLauncher;
import org.eclipse.mcp.acp.protocol.AcpClientThread;
import org.eclipse.mcp.acp.protocol.AcpSchema.AuthenticateResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.protocol.IAcpAgent;

public abstract class AbstractService implements IAgentService {


	AcpClientThread thread;
	Process agentProcess;
	InputStream inputStream;
	OutputStream outputStream;
	InputStream errorStream;
	
	InitializeRequest initializeRequest;
	InitializeResponse initializeResponse;
	AuthenticateResponse authenticateResponse;

	
	public AbstractService() {
		
	}

	public abstract Process createProcess() throws IOException;
	
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
