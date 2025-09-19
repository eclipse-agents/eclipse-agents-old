package org.eclipse.mcp.acp;

import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mcp.Activator;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientCapabilities;
import org.eclipse.mcp.acp.protocol.AcpSchema.FileSystemCapability;
import org.eclipse.mcp.acp.protocol.AcpSchema.HttpHeader;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.McpServer;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.SseTransport;
import org.eclipse.mcp.internal.preferences.IPreferenceConstants;


public class InitializationJob extends Job {

	// Inputs
	IAgentService service;
	String oldSessionId;
	
	// Outputs
	NewSessionResponse newSessionResponse = null;
	InitializeResponse initializeResponse = null;

	public InitializationJob(IAgentService service, String oldSessionId) {
		super("Initializing " + service.getName());
		this.service = service;
		this.oldSessionId = oldSessionId;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Initializing", 2);
		monitor.subTask("Starting CLI process");
		
		service.stop();
		service.start();
		
		monitor.subTask("Initializing CLI");
		
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		
		try {
			FileSystemCapability fsc = new FileSystemCapability(null, true, true);
			ClientCapabilities capabilities = new ClientCapabilities(null, fsc, true);
			InitializeRequest initialize = new InitializeRequest(null, capabilities, 1);
			
			initializeResponse = this.service.getAgent().initialize(initialize).get();
			
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			} 
			
			monitor.worked(1);
			monitor.subTask("Loading Session");
			
			boolean supportsSseMcp = initializeResponse.agentCapabilities() != null &&
					initializeResponse.agentCapabilities().mcpCapabilities() != null &&
							initializeResponse.agentCapabilities().mcpCapabilities().sse();
			
			boolean supportsLoadSession = initializeResponse.agentCapabilities() != null &&
					initializeResponse.agentCapabilities().loadSession();
			
			if (oldSessionId != null && supportsLoadSession) {

			} else {

				McpServer[] servers = null;
				
				if (supportsSseMcp) {
					System.err.println(service.getName() + " supports SSE MCP");
					
					boolean eclipseMcpEnabled = Activator.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.P_SERVER_ENABLED);
					
					if (eclipseMcpEnabled) {
						String httpPort = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.P_SERVER_HTTP_PORT);
						System.err.println("Eclipse MCP is running on port " + httpPort);
						
						servers = new McpServer[] { new SseTransport(
								new HttpHeader[0],
								"Eclipse MCP",
								"sse",
								"http://localhost:" + httpPort + "/sse")}; 
					} else {
						System.err.println("Eclipse MCP is not running");
					}
				} else {
					System.err.println(service.getName() + " does not support SSE MCP");
				}
				
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				NewSessionRequest request = new NewSessionRequest(
						null,
						root.getRawLocationURI().toString(),
						servers);
				
				
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				} 
				newSessionResponse = this.service.getAgent()._new(request).get();
				
			}
		} catch (InterruptedException e) {
			new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		} catch (ExecutionException e) {
			new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
		
		return Status.OK_STATUS;
	}

	public NewSessionResponse getNewSessionResponse() {
		return newSessionResponse;
	}

	public InitializeResponse getInitializeResponse() {
		return initializeResponse;
	}
}
