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
package org.eclipse.agents.contexts;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.agents.IFactoryProvider;
import org.eclipse.agents.Tracer;
import org.eclipse.agents.contexts.adapters.IResourceTemplate;
import org.eclipse.agents.schema.FormAwareMcpToolProvider;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springaicommunity.mcp.provider.complete.SyncMcpCompleteProvider;
import org.springaicommunity.mcp.provider.prompt.SyncMcpPromptProvider;
import org.springaicommunity.mcp.provider.resource.SyncMcpResourceProvider;
import org.springaicommunity.mcp.provider.tool.SyncMcpToolProvider;


import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import jakarta.servlet.Servlet;

public class MCPServer {

	String name, version;
	int port;
	
	// For dynamically adding/removing tools
	boolean running = false;


	private boolean copyLogsToSysError = true; // Boolean.getBoolean("com.ibm.systemz.db2.mcp.copyLogsToSysError");

	McpSyncServer syncServer;
	QueuedThreadPool threadPool;
	String url;
	IFactoryProvider[] factories;
	
	List<SyncCompletionSpecification> completions;
	List<SyncToolSpecification> tools;
//	SyncMcpLogginProvider loggers;
	List<SyncPromptSpecification> prompts;
	List<SyncResourceSpecification> resources;
//	SyncMcpElicitationProvider elicitors;
//	SyncMcpProgressProvider progressives;
//	SyncMcpSamplingProvider samplers;
	
	Set<SyncToolSpecification> removedTools;
	Set<SyncResourceSpecification> dynamicResources;
	List<IResourceTemplate<?, ?>> resourceTemplates;
	
	StringBuffer description;
	
	
	org.eclipse.jetty.server.Server jettyServer = null;
	
	public MCPServer(String name, String version, int port, IFactoryProvider[] factories) {
		this.name = name;
		this.version = version;
		this.port = port;
		this.factories = factories;
		
		removedTools = new HashSet<SyncToolSpecification>(); 
		dynamicResources = new HashSet<SyncResourceSpecification>();
		resourceTemplates = new ArrayList<IResourceTemplate<?, ?>>();
	}
	
	public void start() {
	
		description = new StringBuffer();

		removedTools.clear();
		dynamicResources.clear();
		resourceTemplates.clear();
		
		List<Object> annotated = new ArrayList<Object>();
		for (IFactoryProvider factory: factories) {
			for (Object o: factory.getAnnotatedObjects()) {
				annotated.add(o);
			}
			resourceTemplates.addAll(Arrays.asList(factory.createResourceTemplates()));
		}
		
		completions = new SyncMcpCompleteProvider(annotated).getCompleteSpecifications();
		// Use FormAwareMcpToolProvider to add form support to tools
		try {
			tools = new FormAwareMcpToolProvider(annotated).getToolSpecifications();
			System.err.println("[MCPServer] Tool scan: found " + (tools != null ? tools.size() : 0)
					+ " tools from " + annotated.size() + " annotated objects");
			if (tools != null) {
				for (SyncToolSpecification t : tools) {
					boolean hasCall = t.call() != null;
					System.err.println("[MCPServer]   tool: " + t.tool().name() + " | call=" + hasCall);
				}
			}
			if (tools == null || tools.isEmpty()) {
				System.err.println("[MCPServer] WARNING: No tools found.");
			} else {
				// Rebuild any spec whose call() is null using direct reflection.
				// This happens when SyncMcpToolMethodCallback fails to initialize
				// due to OSGi classloader split or Jackson version mismatch.
				tools = rebuildNullCallHandlers(tools, annotated);
			}
		} catch (Exception e) {
			System.err.println("[MCPServer] ERROR during tool scan: " + e);
			e.printStackTrace();
			tools = new ArrayList<>();
		}
//		loggers = new SyncMcpLogginProvider(factoryList);
		prompts = new SyncMcpPromptProvider(annotated).getPromptSpecifications();
		resources = new SyncMcpResourceProvider(annotated).getResourceSpecifications();
//		elicitors = new SyncMcpElicitationProvider(factoryList);
//		progressives = new SyncMcpProgressProvider(factoryList);
//		samplers = new SyncMcpSamplingProvider(factoryList);

		this.url = "http://localhost:" + port + "/sse";

		
		// Use legacy SSE transport - compatible with Eclipse's bundled Jetty 12.1.4.
		// VS Code tries Streamable HTTP first (POST /sse -> 404), then automatically
		// falls back to legacy SSE (GET /sse) which works correctly.
		HttpServletSseServerTransportProvider transportProvider =
				CustomMcpTransportProvider.create("/", "/sse");

		ServerCapabilities capabilities = ServerCapabilities.builder().resources(true, true) // Enable resource support
				.tools(true) // Enable tool support
				.prompts(false) // Enable prompt support
				.completions()
				.logging() // Enable logging support
				.build();
		
		
		// Create a server with legacy SSE transport
		this.syncServer = McpServer.sync(transportProvider)
			    .serverInfo(name, version)
			    .capabilities(capabilities)
			    .tools(tools)
	            .resources(resources)
			    .completions(completions)
			    .prompts(prompts)
			    .build();
	        
	        
		log(LoggingLevel.INFO, this, url);
	
		running = true;

		for (IFactoryProvider factory: factories) {
			factory.initialize(new MCPServices(this));
			factory.createResourceTemplates();
		}

		syncServer.notifyResourcesListChanged();
	
		threadPool = new QueuedThreadPool();
		threadPool.setName(name + "-Thread");

		jettyServer = new org.eclipse.jetty.server.Server(threadPool);
	
		ServerConnector connector = new ServerConnector(jettyServer);
		connector.setPort(port);
		// Set a long idle timeout to prevent SSE connections from being dropped.
		// Default Jetty timeout is too short for long-lived SSE streams,
		// causing VS Code "TypeError: fetch failed" errors.
		connector.setIdleTimeout(Long.MAX_VALUE);
		jettyServer.addConnector(connector);

		try {
			ServletContextHandler context = new ServletContextHandler();
			context.setContextPath("/");
			// Register Streamable HTTP bridge at /mcp for Eclipse Copilot
			context.addServlet(new ServletHolder(new StreamableHttpBridgeServlet(this)), "/mcp");
			// Register legacy SSE transport at /* for VS Code and other SSE clients
			context.addServlet(new ServletHolder((Servlet)transportProvider), "/*");
			jettyServer.setHandler(context);
			jettyServer.start();
			jettyServer.setStopAtShutdown(true);
			
			syncServer.notifyToolsListChanged();
	
			// Send logging notifications
			log(LoggingLevel.INFO, this, "Server initialized");

		} catch (Exception e) {
			Tracer.trace().trace(Tracer.MCP, "Failed to initialize jetty server", e);
			e.printStackTrace();
		}
	}
	
	McpSyncServer getSyncServer() {
		return syncServer;
	}

	/**
	 * Rebuilds SyncToolSpecification entries that have a null call() handler.
	 * Uses direct Java reflection to invoke the annotated method, bypassing
	 * SyncMcpToolMethodCallback which can fail due to OSGi classloader issues.
	 */
	@SuppressWarnings("unchecked")
	private List<SyncToolSpecification> rebuildNullCallHandlers(
			List<SyncToolSpecification> specs, List<Object> annotatedObjects) {

		List<SyncToolSpecification> result = new ArrayList<>();
		for (SyncToolSpecification spec : specs) {
			if (spec.call() != null) {
				result.add(spec); // already has a handler
				continue;
			}

			// Find the method on one of the annotated objects whose @McpTool name matches
			String toolName = spec.tool().name();
			SyncToolSpecification rebuilt = null;

			outer:
			for (Object target : annotatedObjects) {
				for (Method method : target.getClass().getMethods()) {
					// Check annotation by class name to avoid classloader identity issues
					for (java.lang.annotation.Annotation ann : method.getAnnotations()) {
						String annType = ann.annotationType().getName();
						if ("org.springaicommunity.mcp.annotation.McpTool".equals(annType)) {
							// Get the name attribute via reflection on the annotation
							String annotatedName = null;
							try {
								annotatedName = (String) ann.annotationType()
										.getMethod("name").invoke(ann);
							} catch (Exception ex) { /* ignore */ }

							if (toolName.equals(annotatedName) ||
									(annotatedName != null && annotatedName.isEmpty()
											&& toolName.equals(method.getName()))) {

								final Object finalTarget = target;
								final Method finalMethod = method;
								finalMethod.setAccessible(true);

								// Build a reflection-based BiFunction call handler
								java.util.function.BiFunction<McpSyncServerExchange, Map<String, Object>,
										McpSchema.CallToolResult> callHandler = (exchange, args) -> {
									try {
										Object returnValue;
										if (finalMethod.getParameterCount() == 0) {
											returnValue = finalMethod.invoke(finalTarget);
										} else {
											// Pass null for each param — most Eclipse tools take no args
											Object[] params = new Object[finalMethod.getParameterCount()];
											returnValue = finalMethod.invoke(finalTarget, params);
										}
										String text = returnValue != null ? returnValue.toString() : "null";
										return McpSchema.CallToolResult.builder()
												.addTextContent(text)
												.build();
									} catch (Exception e) {
										System.err.println("[MCPServer] Reflection call error for "
												+ toolName + ": " + e.getMessage());
										return McpSchema.CallToolResult.builder()
												.addTextContent("Error: " + e.getMessage())
												.isError(true)
												.build();
									}
								};

								rebuilt = new SyncToolSpecification(spec.tool(), callHandler);
								System.err.println("[MCPServer] Rebuilt reflection call handler for: " + toolName);
								break outer;
							}
						}
					}
				}
			}

			result.add(rebuilt != null ? rebuilt : spec);
		}
		return result;
	}

	/**
	 * Calls a tool by name with the given arguments.
	 * Routes through the registered SyncToolSpecification call handlers.
	 */
	public McpSchema.CallToolResult callTool(String toolName, java.util.Map<String, Object> args) {
		// Find the spec in the tools list (same list registered with syncServer)
		for (SyncToolSpecification spec : tools) {
			if (spec.tool().name().equals(toolName) && !removedTools.contains(spec)) {
				if (spec.call() != null) {
					// call() takes (McpSyncServerExchange, Map<String,Object>)
					// Passing null for exchange is safe — tool methods in this project
					// do not use the exchange for logging/sampling callbacks.
					return spec.call().apply(null, args);
				} else {
					throw new RuntimeException("Tool '" + toolName + "' has no call handler (call() is null)."
							+ " This usually means the OSGi classloader split is still present."
							+ " Check MANIFEST.MF Import-Package for org.springaicommunity entries.");
				}
			}
		}
		throw new RuntimeException("Tool not found: " + toolName);
	}

	public void stop() {

		if (syncServer != null) {
			syncServer.closeGracefully();
		}
		
		if (jettyServer != null) {
			try {
				jettyServer.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public IResourceTemplate<?, ?> getResourceTemplate(String uri) {
		for (IResourceTemplate<?, ?> adapter: resourceTemplates) {
			if (adapter.matches(uri)) {
				return adapter.fromUri(uri);
			}
		}
		return null;
	}

	public void log(McpSchema.LoggingLevel level, Object source, String message) {
	
		if (copyLogsToSysError) {
			System.err.println(message);
		}
	
		Class<?> sourceClass;
		if (!(source instanceof Class)) {
			sourceClass = source.getClass();
		} else {
			sourceClass = (Class<?>) source;
		}
	
		syncServer.loggingNotification(LoggingMessageNotification.builder().level(level)
			.logger(sourceClass.getCanonicalName()).data(message).build());
	}
	
	public void log(Throwable throwable) {
		Class<?> c = getClass();
		if (throwable.getStackTrace() != null && throwable.getStackTrace().length > 0) {
			c = throwable.getStackTrace()[0].getClass();
		}
		
		if (throwable instanceof McpError) {
			log(LoggingLevel.ERROR, c, "MCP Implementation Exception");
			int depth = 0;
			while (throwable != null && depth < 5) {
				log(LoggingLevel.ERROR, c, throwable.getMessage());
				throwable = throwable.getCause();
				depth++;
			}
		}
		
	}
	

	public boolean getVisibility(String toolName) {
		for (SyncToolSpecification tool: removedTools) {
			if (tool.tool().name().equals(toolName)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean setVisibility(String toolName, boolean visible) {
		SyncToolSpecification match = null;
		for (SyncToolSpecification tool: removedTools) {
			if (tool.tool().name().equals(toolName)) {
				match = tool;
				break;
			}
		}
		
		if (match != null) {
			if (removedTools.contains(match) && visible) {
				removedTools.remove(match);
				syncServer.addTool(match);
				syncServer.notifyToolsListChanged();
				return true;
			} else if (!removedTools.contains(match) && !visible) {
				removedTools.add(match);
				syncServer.removeTool(toolName);
				syncServer.notifyToolsListChanged();
				return true;
			}
		}
		
		return false;
	}

	public boolean addResource(SyncResourceSpecification spec) {
		for (SyncResourceSpecification existing: dynamicResources) {
			if (existing.resource().uri().equals(spec.resource().uri())) {
				// do nothing
				return false;
			}
		}
		
		dynamicResources.add(spec);
		syncServer.addResource(spec);
		syncServer.notifyResourcesListChanged();
		return true;
	}

	public boolean removeResource(String uri) {
		for (SyncResourceSpecification existing: dynamicResources) {
			if (existing.resource().uri().equals(uri)) {
				dynamicResources.remove(existing);
				syncServer.removeResource(uri);
				syncServer.notifyResourcesListChanged();
				return true;
			}
		}
		
		return false;
	}
	
	public String getContentsDescription() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("MCP Server running on :" + this.url);
		
		buffer.append("\nTools:");
		
		for (SyncToolSpecification tool: tools) {
			if (!removedTools.contains(tool)) {
				buffer.append("\n\t" + tool.tool().name() + ": " + tool.tool().description());
			}
		}
		
		buffer.append("\nResource Templates:");
		for (SyncResourceSpecification resource: resources) {
			if (resource.resource().uri().contains("{")) {
				buffer.append("\n\t" + resource.resource().name() + ": " + resource.resource().description());
				buffer.append("\n\t\t" + resource.resource().uri());
			}
		}

		return buffer.toString();
		
	}
}
