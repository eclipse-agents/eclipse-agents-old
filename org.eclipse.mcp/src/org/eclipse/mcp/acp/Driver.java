package org.eclipse.mcp.acp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.mcp.acp.AcpSchema.ClientCapabilities;
import org.eclipse.mcp.acp.AcpSchema.FileSystemCapability;
import org.eclipse.mcp.acp.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.AcpSchema.InitializeResponse;

public class Driver {

	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
	
		String gemini = "/usr/local/bin/gemini";
		String node = "/usr/local/bin/node";
		
		
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
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
//		String line = br.readLine();
//		while (line != null) {
//			System.err.println(line);
//			if (br.)
//		}
		
		OutputStreamWriter writer = new OutputStreamWriter(out);
		writer.write("{\"jsonrpc\": \"2.0\",\"id\": 0,\"method\": \"initialize\",\"params\": {\"protocolVersion\": 1,\"clientCapabilities\": {\"fs\": {\"readTextFile\": true,\"writeTextFile\": true},\"terminal\": true}}}\r\n");
		writer.flush();
		
		String line = br.readLine();
		while (line != null) {
			System.err.println(line);
			line = br.readLine();
		}
		
		ContextStore<IAcpAgent> contextStore = new ContextStore<>();
		AcpClient acpClient = new AcpClient(contextStore);

		
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
	}

}


