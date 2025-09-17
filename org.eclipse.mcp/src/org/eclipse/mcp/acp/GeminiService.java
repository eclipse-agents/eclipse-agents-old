package org.eclipse.mcp.acp;

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
import org.eclipse.mcp.acp.AcpSchema.ClientCapabilities;
import org.eclipse.mcp.acp.AcpSchema.FileSystemCapability;
import org.eclipse.mcp.acp.AcpSchema.HttpHeader;
import org.eclipse.mcp.acp.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.AcpSchema.McpServer;
import org.eclipse.mcp.acp.AcpSchema.NewSessionRequest;
import org.eclipse.mcp.acp.AcpSchema.NewSessionResponse;
import org.eclipse.mcp.acp.AcpSchema.SseTransport;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class GeminiService {

	String gemini = "/usr/local/bin/gemini";
	String node = "/usr/local/bin/node";
	
	public GeminiService(String node, String gemini) {
		this.node = node;
		this.gemini = gemini;
	}
	
	public void start() {
		
		try {
			List<String> commandAndArgs = new ArrayList<String>();
//		commandAndArgs.add("gemini");
			commandAndArgs.add(node);
			commandAndArgs.add(gemini);
			commandAndArgs.add("--experimental-acp");
//		commandAndArgs.add("--debug");
			
			ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
			Process agentProcess = pb.start();
			InputStream in = agentProcess.getInputStream();
			OutputStream out = agentProcess.getOutputStream();
			InputStream err = agentProcess.getErrorStream();
			
			if (!agentProcess.isAlive()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(err, "UTF-8"));
				String line = br.readLine();
				while (line != null) {
					System.err.println(line);
					line = br.readLine();
				}
				return;
			}
			
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			IConsoleManager conMan = plugin.getConsoleManager();
			IOConsole console = new IOConsole("Gemini CLI", null, null, false);
			conMan.addConsoles(new IConsole[] { (IConsole) console });
			IOConsoleOutputStream output = console.newOutputStream();
			
			AcpClient acpClient = new AcpClient(console, output);
			AcpClientLauncher launcher = new AcpClientLauncher(acpClient, in, out);
			AcpClientThread thread = new AcpClientThread(launcher) {
				@Override
				public void statusChanged() {
					System.err.println(getStatus());
				}
			};
			thread.start();
			
			new Thread("Acp Initialization") {
				@Override
				public void run() {
					
					try {
						Thread.sleep(1000);

						IAcpAgent agent = thread.getAgent();
						FileSystemCapability fsc = new FileSystemCapability(null, true, true);
						ClientCapabilities capabilities = new ClientCapabilities(null, fsc, true);
						InitializeRequest initialize = new InitializeRequest(null, capabilities, 1);
						InitializeResponse response = agent.initialize(initialize).get();
						
						
						McpServer server = new SseTransport(
								new HttpHeader[0],
								"Eclipse MCP",
								"sse",
								"http://localhost:8683/sse"); 
						
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						NewSessionRequest request = new NewSessionRequest(
								null,
								root.getRawLocationURI().toString(),
								new McpServer[] { server });
						
						
						NewSessionResponse nse = agent._new(request).get();
						try {
							nse.modes();
							output.write("Select a mode:\n");
							for (int i = 1; i < nse.modes().availableModes().length; i++) {
								output.write("\t" + i + nse.modes().availableModes()[i].name());
							}
							int read = console.getInputStream().read();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
			
			
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
}
