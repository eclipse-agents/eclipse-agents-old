package org.eclipse.mcp.acp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.Launcher.Builder;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageConsumer;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.mcp.acp.AcpSchema.ClientCapabilities;
import org.eclipse.mcp.acp.AcpSchema.FileSystemCapability;
import org.eclipse.mcp.acp.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.AcpSchema.InitializeResponse;

import com.google.gson.Gson;

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
		
		Gson gson = new Gson();
		gson.fromJson("{\"protocolVersion\":1,\"authMethods\":[{\"id\":\"oauth-personal\",\"name\":\"Log in with Google\",\"description\":null},{\"id\":\"gemini-api-key\",\"name\":\"Use Gemini API key\",\"description\":\"Requires setting the `GEMINI_API_KEY` environment variable\"},{\"id\":\"vertex-ai\",\"name\":\"Vertex AI\",\"description\":null}],\"agentCapabilities\":{\"loadSession\":false,\"promptCapabilities\":{\"image\":true,\"audio\":true,\"embeddedContext\":true}}}", InitializeResponse.class);
		
		
		AcpClient acpClient = new AcpClient();
		final Object lock = new Object();
		
		Builder<IAcpAgent> builder = new Builder<IAcpAgent>() {

			@Override
			protected RemoteEndpoint createRemoteEndpoint(MessageJsonHandler jsonHandler) {
				MessageConsumer outgoingMessageStream = new StreamMessageConsumer(output, jsonHandler) {
					@Override
					public void consume(Message message) {
						try {
							String content = jsonHandler.serialize(message);
							byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8.name());
							
							synchronized (lock) {
								output.write(contentBytes);
								output.write("\n".getBytes(StandardCharsets.UTF_8.name()));
								output.flush();
							}
						} catch (IOException exception) {
							throw new JsonRpcException(exception);
						}
						super.consume(message);
					}
					
				};
				outgoingMessageStream = wrapMessageConsumer(outgoingMessageStream);
				Endpoint localEndpoint = ServiceEndpoints.toEndpoint(localServices);
				RemoteEndpoint remoteEndpoint;
				if (exceptionHandler == null)
					remoteEndpoint = new RemoteEndpoint(outgoingMessageStream, localEndpoint);
				else
					remoteEndpoint = new RemoteEndpoint(outgoingMessageStream, localEndpoint, exceptionHandler);
				jsonHandler.setMethodProvider(remoteEndpoint);
				remoteEndpoint.setJsonHandler(jsonHandler);
				return remoteEndpoint;
			}
			
			public Launcher<IAcpAgent> create() {
				// Validate input
				if (input == null)
					throw new IllegalStateException("Input stream must be configured.");
				if (output == null)
					throw new IllegalStateException("Output stream must be configured.");
				if (localServices == null)
					throw new IllegalStateException("Local service must be configured.");
				if (remoteInterfaces == null)
					throw new IllegalStateException("Remote interface must be configured.");

				// Create the JSON handler, remote endpoint and remote proxy
				MessageJsonHandler jsonHandler = createJsonHandler();
				if (messageTracer != null) {
					messageTracer.setJsonHandler(jsonHandler);
				}
				RemoteEndpoint remoteEndpoint = createRemoteEndpoint(jsonHandler);
				IAcpAgent remoteProxy = createProxy(remoteEndpoint);

				// Create the message processor
				final var reader = new StdinoutMessageProducer(input, jsonHandler, remoteEndpoint);
				MessageConsumer messageConsumer = wrapMessageConsumer(remoteEndpoint);
				ConcurrentMessageProcessor msgProcessor = createMessageProcessor(reader, messageConsumer, remoteProxy);
				ExecutorService execService = executorService != null ? executorService : Executors.newCachedThreadPool();
				return createLauncher(execService, remoteProxy, remoteEndpoint, msgProcessor);
			}

			
			
		};
		
		PrintWriter tracer = new PrintWriter(System.out);
		
		Launcher<IAcpAgent> launcher = builder
			.setLocalService(acpClient)
			.setRemoteInterface(IAcpAgent.class)
			.setInput(in)
			.setOutput(out)
			.traceMessages(tracer)
			.create();
		
		launcher.startListening();
		
		

		RemoteEndpoint re = launcher.getRemoteEndpoint();
		
		IAcpAgent agent = launcher.getRemoteProxy();
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


