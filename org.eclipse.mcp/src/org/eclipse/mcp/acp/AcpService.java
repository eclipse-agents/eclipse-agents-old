package org.eclipse.mcp.acp;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mcp.acp.agent.GeminiService;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ContentBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.PromptRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionModeState;

public class AcpService {

	private static AcpService instance;
	
	private IAgentService service = null;
	private InitializeResponse initializeResponse;
	private SessionModeState sessionModeState;
	private String sessionId;
	private InitializationJob initializationJob;
	private ListenerList<IAcpListener> listenerList;
	
	static {
		instance = new AcpService();
	}
	
	IAgentService[] agentServices;
	private AcpService() {
		agentServices = new IAgentService[] { new GeminiService() };
		listenerList = new  ListenerList<IAcpListener>();
	}
	
	public static AcpService instance() {
		return instance;
	}
	
	public IAgentService[] getAgents() {
		return agentServices;
	}

	public void setAcpService(IAgentService service) {
//		if (this.service != service) {
			if (this.service != null) {
				this.service.stop();
			}
//			if (console != null) {
//				console.destroy();
//			}
//			console = null;
			
			this.service = service;
			initializeResponse = null;
			sessionModeState = null;
			sessionId = null;
			
			if (initializationJob != null) {
				initializationJob.cancel();
			}
			
			initializationJob = new InitializationJob(this.service, null);
			initializationJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					if (event.getJob().getResult().isOK()) {
						InitializationJob job = (InitializationJob)event.getJob();
						
					
						if (job.getInitializeResponse() != null) {
							instance.initializeResponse = job.getInitializeResponse();;
							
							if (job.getNewSessionResponse() != null) {
								instance.sessionId = job.getNewSessionResponse().sessionId();
								instance.sessionModeState = job.getNewSessionResponse().modes();
								
								clientRequests(job.getInitializeRequest());
								agentResponds(job.getInitializeResponse());
								clientRequests(job.getNewSessionRequest());
								agentResponds(job.getNewSessionResponse());
								
								
							} else {
								System.err.println("initialization job missing InitializeResponse");
							}
						} else {
							System.err.println("initialization job missing NewSessionResponse");
						}
					}
				}
			});
			initializationJob.schedule();
			
//		}
	}
	
	/**
	 * stores initialization response
	 * @param initializeResponse
	 * @return value of session id to restore, or null to start new session
	 */
	protected String setInitializeResponse(InitializeResponse initializeResponse) {
		this.initializeResponse = initializeResponse;
		return null;
	}
		
	public void sessionCreated(NewSessionResponse newSessionResponse) {
		sessionModeState = newSessionResponse.modes();
		sessionId = newSessionResponse.sessionId();
	}

	public InitializeResponse getInitializeResponse() {
		return initializeResponse;
	}

	public SessionModeState getSessionModeState() {
		return sessionModeState;
	}

	public String getSessionId() {
		return sessionId;
	}
	
	public IAgentService getAgentService() {
		return service;
	}
	
	public void addAcpListener(IAcpListener listener) {
		listenerList.add(listener);
	}
	
	public void removeAcpListener(IAcpListener listener) {
		listenerList.remove(listener);
	}
	
	public void clientRequests(ClientRequest req) {
		for (IAcpListener listener: listenerList) {
			listener.clientRequests(req);
		}
	}
	
	public void clientResponds(ClientResponse resp) {
		for (IAcpListener listener: listenerList) {
			listener.clientResponds(resp);
		}
	}
	
	public void clientNotifies(ClientNotification notification) {
		for (IAcpListener listener: listenerList) {
			listener.clientNotifies(notification);
		}
	}
	
	public void agentRequests(AgentRequest req) {
		for (IAcpListener listener: listenerList) {
			listener.agentRequests(req);
		}
	}
	
	public void agentResponds(AgentResponse resp) {
		for (IAcpListener listener: listenerList) {
			listener.agentResponds(resp);
		}
	}
	
	public void agentNotifies(AgentNotification notification) {
		for (IAcpListener listener: listenerList) {
			listener.agentNotifies(notification);
		}
	}

	public void prompt(ContentBlock[] contentBlocks) {
		PromptRequest request = new PromptRequest(null, contentBlocks, sessionId);
		clientRequests(request);
		getAgentService().getAgent().prompt(request).whenComplete((result, ex) -> {
	        if (ex != null) {
	            ex.printStackTrace();
	        } else {
	           agentResponds(result);
	        }
	    });
	}
		
}
