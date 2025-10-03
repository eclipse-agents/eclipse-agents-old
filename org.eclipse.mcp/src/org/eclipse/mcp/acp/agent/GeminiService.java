package org.eclipse.mcp.acp.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mcp.Activator;
import org.eclipse.mcp.acp.InitializationJob;
import org.eclipse.mcp.acp.protocol.AcpClient;
import org.eclipse.mcp.acp.protocol.AcpClientLauncher;
import org.eclipse.mcp.acp.protocol.AcpClientThread;
import org.eclipse.mcp.acp.protocol.AcpSchema.AuthenticateResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionModeState;
import org.eclipse.mcp.acp.view.AcpSessionModel;
import org.eclipse.mcp.acp.protocol.IAcpAgent;
import org.eclipse.mcp.internal.preferences.IPreferenceConstants;

public class GeminiService implements IAgentService {


	AcpClientThread thread;
	Process agentProcess;
	InputStream inputStream;
	OutputStream outputStream;
	InputStream errorStream;
	
	InitializeRequest initializeRequest;
	InitializeResponse initializeResponse;
	AuthenticateResponse authenticateResponse;

	
	public GeminiService() {
		
	}
	
	public void start() {
				
		String node = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.P_ACP_NODE); 
		String gemini = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.P_ACP_GEMINI);

		try {
			List<String> commandAndArgs = new ArrayList<String>();
//		commandAndArgs.add("gemini");
			commandAndArgs.add(node);
			commandAndArgs.add(gemini);
			commandAndArgs.add("--experimental-acp");
//		commandAndArgs.add("--debug");
			
			ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
			agentProcess = pb.start();
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
	public String getName() {
		return "Gemini CLI";
	}

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
