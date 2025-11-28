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
package org.eclipse.agents.services.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.agents.services.protocol.AcpSchema.ClientCapabilities;
import org.eclipse.agents.services.protocol.AcpSchema.FileSystemCapability;
import org.eclipse.agents.services.protocol.AcpSchema.HttpHeader;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.McpServer;
import org.eclipse.agents.services.protocol.AcpSchema.NewSessionRequest;
import org.eclipse.agents.services.protocol.AcpSchema.SseTransport;

import com.google.gson.Gson;

import io.modelcontextprotocol.spec.McpSchema.JSONRPCRequest;

public class RawDriver {

	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
	
		String gemini = "/Users/jflicke/.eclipseagents/node/node_modules/@google/input-cli/dist/index.js";
		String node = "/Applications/2025-09J2EE.app/Contents/Eclipse/.node/node-v22.13.1-darwin-x64/bin/node";
	
		// {"jsonrpc":"2.0","method":"initialize","id":"0","params":{"clientCapabilities":{"fs":{"readTextFile":true,"writeTextFile":true},"terminal":true},"protocolVersion":1}}
		
		List<String> commandAndArgs = new ArrayList<String>();
//		commandAndArgs.add("input");
		commandAndArgs.add(node);
		commandAndArgs.add("--inspect-brk");
		commandAndArgs.add(gemini);
		commandAndArgs.add("--experimental-acp");
//		commandAndArgs.add("--debug");
		
		ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
//		pb.environment().put("GEMINI_CLI_NO_RELAUNCH", "true");
//		pb.directory(new File("/Users/jflicke/.eclipseagents/node"));
	
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
		
		agentProcess.onExit().thenRun(new Runnable() {
			@Override
			public void run() {
				int exitValue = agentProcess.exitValue();
				String output = null;
				String errorString = null;

				System.out.println("Gemini Exit:" + exitValue);
			}
		});;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
//		String line = br.readLine();
//		System.err.println(line);

//		String line = br.readLine();
//		while (line != null) {
//			System.err.println(line);
//			if (br.)
//		}
		
//		{
//			  "jsonrpc": "2.0",
//			  "id": "1",
//			  "method": "initialize",
//			  "params": {
//			    "clientCapabilities": {
//			      "fs": {
//			        "readTextFile": true,
//			        "writeTextFile": true
//			      },
//			      "terminal": true
//			    },
//			    "protocolVersion": 1
//			  }
//			}
		
		FileSystemCapability fsc = new FileSystemCapability(null, true, true);
		ClientCapabilities capabilities = new ClientCapabilities(null, fsc, true);
		InitializeRequest initialize = new InitializeRequest(null, capabilities, 1);
		JSONRPCRequest request = new JSONRPCRequest("2.0", "initialize", "0", initialize);
		Gson gson = new Gson();
		
		System.err.println(gson.toJson(request));
		OutputStreamWriter writer = new OutputStreamWriter(out);
		writer.write(gson.toJson(request));
		writer.write("\n");
		writer.flush();
		
		String line = br.readLine();
		System.err.println(line);
			
		
		McpServer server = new SseTransport(
				new HttpHeader[0],
				"Eclipse MCP",
				"sse",
				"http://localhost:8683/sse"); 
		
		NewSessionRequest session = new NewSessionRequest(
				null,
				"file:/Users/jflicke/git/eclipse-mcp",
//				new McpServer[] { server });
				new McpServer[0]);
		
//		{
//			  "jsonrpc": "2.0",
//			  "id": "2",
//			  "method": "session/new",
//			  "params": {
//			    "cwd": "file:/Users/jflicke/runtime-IDz17cleanNew",
//			    "mcpServers": []
//			  }
//			}
		request = new JSONRPCRequest("2.0", "session/new", "1", session);
		System.err.println(gson.toJson(request));
		writer.write(gson.toJson(request));
		writer.write("\n");
		writer.flush();
		
		line = br.readLine();
		System.err.println(line);

	}

}


