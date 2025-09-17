package org.eclipse.mcp.acp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.mcp.acp.AcpSchema.ClientCapabilities;
import org.eclipse.mcp.acp.AcpSchema.FileSystemCapability;
import org.eclipse.mcp.acp.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.AcpSchema.InitializeResponse;

public class GeminiService {

	public void start() {
		
		String gemini = "/usr/local/bin/gemini";
		String node = "/usr/local/bin/node";
		
		
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
			}
			
			
			AcpClient acpClient = new AcpClient();

			
			AcpClientLauncher<IAcpAgent> launcher = new AcpClientLauncher<IAcpAgent>(acpClient, IAcpAgent.class, in, out);
			AcpClientThread thread = new AcpClientThread(launcher) {
				
				@Override
				public void statusChanged() {
					// TODO Auto-generated method stub
					
				}
			};
			thread.start();
			
			Thread.sleep(5000);
//		while (thread.getDssServer() == null) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
			
			IAcpAgent agent = thread.getDssServer();
			FileSystemCapability fsc = new FileSystemCapability(null, true, true);
			ClientCapabilities capabilities = new ClientCapabilities(null, fsc, true);
			InitializeRequest initialize = new InitializeRequest(null, capabilities, 1);
			CompletableFuture<InitializeResponse> response = agent.initialize(initialize);
			
			Thread.sleep(5000);
			
			System.out.println(response);
			//= new AcpClientThread(launcher);
			response.get();
			
			
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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
