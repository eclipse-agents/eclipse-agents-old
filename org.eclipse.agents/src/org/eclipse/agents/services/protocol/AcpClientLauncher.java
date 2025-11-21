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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageConsumer;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

import com.google.gson.Gson;

public class AcpClientLauncher implements Launcher<IAcpAgent> {

	private final Launcher<IAcpAgent> launcher;
	private boolean traceLsp4jJsonrpc = true; //Boolean.getBoolean("org.eclipse.acp.trace.lsp4j.jsonrpc"); //$NON-NLS-1$
	private Object lock = new Object();
	private Gson gson;
	
	public AcpClientLauncher(IAcpClient acpClient, InputStream is, OutputStream os) {
		
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
//						super.consume(message);
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
		
		AcpSchemaTypeAdapters typeAdapters = new AcpSchemaTypeAdapters();

		try {
			
			PrintWriter tracer = traceLsp4jJsonrpc ? new PrintWriter(System.out) : null;

			this.launcher = builder
					.setLocalService(acpClient)
					.setRemoteInterface(IAcpAgent.class)
					.setInput(is)
					.setOutput(os)
					.traceMessages(tracer)
					.configureGson(gsonBuilder->{
						typeAdapters.registerTypeAdapters(gsonBuilder);
				}).create();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Future<Void> startListening() {
		return CompletableFuture.runAsync(() -> {
			try {
				this.launcher.startListening().get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}, Executors.newSingleThreadExecutor());
	}

	public IAcpAgent getRemoteProxy() {
		return this.launcher.getRemoteProxy();
	}

	@Override
	public RemoteEndpoint getRemoteEndpoint() {
		return null;
	}
	
	
}
