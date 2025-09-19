package org.eclipse.mcp.acp;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mcp.acp.agent.GeminiService;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionModeState;

public class AcpService {

	private static AcpService instance;
	
	private IAgentService service = null;
	private InitializeResponse initializeResponse;
	private SessionModeState sessionModeState;
	private String sessionId;
	private InitializationJob initializationJob;
	
	
	static {
		instance = new AcpService();
	}
	
	IAgentService[] agentServices;
	private AcpService() {
		agentServices = new IAgentService[] { new GeminiService() };
	}
	
	public static AcpService instance() {
		return instance;
	}
	
	public IAgentService[] getAgents() {
		return agentServices;
	}

	public void setAcpService(IAgentService service) {
		if (this.service != service) {
			if (this.service != null) {
				this.service.stop();
			}
			
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
					if (event.getJobGroupResult().isOK()) {
						InitializationJob job = (InitializationJob)event.getJob();
						
						if (job.getInitializeResponse() != null) {
							instance.initializeResponse = job.getInitializeResponse();;
							
							if (job.getNewSessionResponse() != null) {
								instance.sessionId = job.getNewSessionResponse().sessionId();
								instance.sessionModeState = job.getNewSessionResponse().modes();
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
			
		}
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
		sessionModeState = newSessionResponse.modes()
		sessionId = newSessionResponse.sessionId();
	}
		
}
