package org.eclipse.mcp.acp.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.mcp.Activator;
import org.eclipse.mcp.acp.protocol.AcpClient;
import org.eclipse.mcp.acp.protocol.AcpClientLauncher;
import org.eclipse.mcp.acp.protocol.AcpClientThread;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientCapabilities;
import org.eclipse.mcp.acp.protocol.AcpSchema.FileSystemCapability;
import org.eclipse.mcp.acp.protocol.AcpSchema.HttpHeader;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.McpServer;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.SseTransport;
import org.eclipse.mcp.acp.protocol.IAcpAgent;
import org.eclipse.mcp.internal.preferences.IPreferenceConstants;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class GeminiService implements IAgentService {


	AcpClientThread thread;
	Process agentProcess;
	InputStream inputStream;
	OutputStream outputStream;
	InputStream errorStream;
	
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
			});;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		agentProcess.destroy();
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
	
	
}
