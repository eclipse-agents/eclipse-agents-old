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
package org.eclipse.mcp.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.mcp.IFactoryProvider;
import org.eclipse.mcp.resource.IResourceTemplate;
import org.springaicommunity.mcp.provider.complete.SyncMcpCompleteProvider;
import org.springaicommunity.mcp.provider.prompt.SyncMcpPromptProvider;
import org.springaicommunity.mcp.provider.resource.SyncMcpResourceProvider;
import org.springaicommunity.mcp.provider.tool.SyncMcpToolProvider;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
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
		tools = new SyncMcpToolProvider(annotated).getToolSpecifications();
//		loggers = new SyncMcpLogginProvider(factoryList);
		prompts = new SyncMcpPromptProvider(annotated).getPromptSpecifications();
		resources = new SyncMcpResourceProvider(annotated).getResourceSpecifications();
//		elicitors = new SyncMcpElicitationProvider(factoryList);
//		progressives = new SyncMcpProgressProvider(factoryList);
//		samplers = new SyncMcpSamplingProvider(factoryList);

		this.url = "http://localhost:" + port + "/sse";

		
		//HttpServletSseServerTransportProvider transportProvider =
		//	    new HttpServletSseServerTransportProvider(
		//	        new ObjectMapper(), "/", "/sse");
		
		// If JsonMapper not specified, a JacksonJsonMapper will be created from the configured ObjectMapper
		HttpServletSseServerTransportProvider transportProvider = HttpServletSseServerTransportProvider.builder()
				.messageEndpoint("/")
				.sseEndpoint("/sse")
				.build();

		ServerCapabilities capabilities = ServerCapabilities.builder().resources(true, true) // Enable resource support
				.tools(true) // Enable tool support
				.prompts(false) // Enable prompt support
				.completions()
				.logging() // Enable logging support
				.build();
		
		
		// Create a server with custom configuration
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
		jettyServer.addConnector(connector);

		try {
			ServletContextHandler context = new ServletContextHandler();
			context.setContextPath("/");
			context.addServlet(new ServletHolder((Servlet)transportProvider), "/*");
			jettyServer.setHandler(context);
			jettyServer.start();
			jettyServer.setStopAtShutdown(true);
			
			syncServer.notifyToolsListChanged();
	
			// Send logging notifications
			log(LoggingLevel.INFO, this, "Server initialized");

		} catch (Exception e) {
			Tracer.trace().trace(Tracer.DEBUG, "Failed to initialize resource factory: " + e.getLocalizedMessage(), e);
			e.printStackTrace();
		}
	}
	
	McpSyncServer getSyncServer() {
		return syncServer;
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
