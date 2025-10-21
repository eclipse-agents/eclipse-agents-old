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
package org.eclipse.mcp.acp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mcp.acp.agent.GeminiService;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.CancelNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ContentBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.PromptRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.PromptResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReleaseTerminalRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.SetSessionModeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.SetSessionModeResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.StopReason;
import org.eclipse.mcp.acp.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.WriteTextFileResponse;
import org.eclipse.mcp.acp.view.AcpSessionModel;
import org.eclipse.mcp.acp.view.AcpView;

public class AcpService {

	private static AcpService instance;
	
	private IAgentService activeAgent = null;
	private String activeSessionId = null;
	
	private static Map<String, AcpSessionModel> sessions = new HashMap<String, AcpSessionModel>();
	
	private ListenerList<IAcpSessionListener> listenerList;

	private InitializationJob initializationJob;

	static {
		instance = new AcpService();
	}
	
	IAgentService[] agentServices;
	private AcpService() {
		agentServices = new IAgentService[] { 
			new GeminiService()
//			new GooseService()
		};
		listenerList = new  ListenerList<IAcpSessionListener>();
	}
	
	public static AcpService instance() {
		return instance;
	}
	
	public IAgentService[] getAgents() {
		return agentServices;
	}

	public void setAcpService(AcpView view, IAgentService agent) {
		view.agentDisconnected();
		activeSessionId = null;
		this.activeAgent = agent;
		if (!agent.isRunning()) {
			agent.stop();
			
           if (initializationJob != null) {
               initializationJob.cancel();
           }
                       
			initializationJob = new InitializationJob(activeAgent, null);
			initializationJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					if (event.getJob().getResult().isOK()) {
						InitializationJob job = (InitializationJob) event.getJob();

						String sessionId = job.getSessionId();
						if (sessionId != null && !sessions.containsKey(sessionId)) {
							
							activeSessionId = sessionId;

							AcpSessionModel model = new AcpSessionModel(
								agent,
								sessionId,
								job.getCwd(),
								job.getMcpServers(),
								job.getModes());
							
							sessions.put(sessionId, model);
							
							model.setView(view);
							view.agentConnected();
							
							clientRequests(agent.getInitializeRequest());
							agentResponds(agent.getInitializeResponse());
							
							
						} else {
							//TODO
							System.err.println("found a pre-existing matching session id");
						}
					} else {
						System.err.println("initialization job has an error");
						System.err.println(event.getJob().getResult());
					}
				}
			});
			initializationJob.schedule();
		}
	}
	
	public IAgentService getAgentService() {
		return activeAgent;
	}
	
	public String getActiveSessionId() {
		return activeSessionId;
	}
	
	public AcpSessionModel getActiveSession() {
		return sessions.get(activeSessionId);
	}
	
	public void addAcpListener(IAcpSessionListener listener) {
		listenerList.add(listener);
	}
	
	public void removeAcpListener(IAcpSessionListener listener) {
		listenerList.remove(listener);
	}
	
	public void clientRequests(ClientRequest req) {
		for (IAcpSessionListener listener: listenerList) {
//			if (req instanceof InitializeRequest) {
//				listener.accept((InitializeRequest)req);	
//			} else if (req instanceof AuthenticateRequest) {
//				listener.accept((AuthenticateRequest)req);
//			} else if (req instanceof NewSessionRequest) {
//				listener.accept((NewSessionRequest)req);
//			} else if (req instanceof LoadSessionRequest) {
//				listener.accept((LoadSessionRequest)req);
			if (req instanceof SetSessionModeRequest) {
				listener.accept((SetSessionModeRequest)req);
			} else if (req instanceof PromptRequest) {
				listener.accept((PromptRequest)req);
			}
		}
	}
	
	public void clientResponds(ClientResponse resp) {
		for (IAcpSessionListener listener: listenerList) {
			if (resp instanceof WriteTextFileResponse) {
				listener.accept((WriteTextFileResponse)resp);
			} else if (resp instanceof ReadTextFileResponse) {
				listener.accept((ReadTextFileResponse)resp);
			} else if (resp instanceof RequestPermissionResponse) {
				listener.accept((RequestPermissionResponse)resp);
			} else if (resp instanceof CreateTerminalResponse) {
				listener.accept((CreateTerminalResponse)resp);
			} else if (resp instanceof TerminalOutputResponse) {
				listener.accept((TerminalOutputResponse)resp);
			} else if (resp instanceof ReleaseTerminalResponse) {
				listener.accept((ReleaseTerminalResponse)resp);
			} else if (resp instanceof WaitForTerminalExitResponse) {
				listener.accept((WaitForTerminalExitResponse)resp);
			} else if (resp instanceof KillTerminalCommandResponse) {
				listener.accept((KillTerminalCommandResponse)resp);
			}							
		}
	}
	
	public void clientNotifies(ClientNotification notification) {
		for (IAcpSessionListener listener: listenerList) {
			if (notification instanceof CancelNotification) {
				listener.accept((CancelNotification)notification);
			}
		}
	}
	
	public void agentRequests(AgentRequest req) {
		for (IAcpSessionListener listener : listenerList) {
			if (req instanceof ReadTextFileRequest) {
				listener.accept((ReadTextFileRequest)req);
			} else if (req instanceof RequestPermissionRequest) {
				listener.accept((RequestPermissionRequest)req);
			} else if (req instanceof CreateTerminalRequest) {
				listener.accept((CreateTerminalRequest)req);
			} else if (req instanceof TerminalOutputRequest) {
				listener.accept((TerminalOutputRequest)req);
			} else if (req instanceof ReleaseTerminalRequest) {
				listener.accept((ReleaseTerminalRequest)req);
			} else if (req instanceof WaitForTerminalExitRequest) {
				listener.accept((WaitForTerminalExitRequest)req);
			} else if (req instanceof KillTerminalCommandRequest) {
				listener.accept((KillTerminalCommandRequest)req);
			}
		}
	}
	
	public void agentResponds(AgentResponse resp) {
		for (IAcpSessionListener listener: listenerList) {
//			if (resp instanceof AuthenticateResponse) {
//				listener.accept((AuthenticateResponse)resp);
//			} else if (resp instanceof NewSessionResponse) {
//				listener.accept((NewSessionResponse)resp);
//			} else if (resp instanceof LoadSessionResponse) {
//				listener.accept((LoadSessionResponse)resp);
			if (resp instanceof SetSessionModeResponse) {
				listener.accept((SetSessionModeResponse)resp);
			} else if (resp instanceof PromptResponse) {
				listener.accept((PromptResponse)resp);
			}
		}
	}
	
	public void agentNotifies(AgentNotification notification) {
		for (IAcpSessionListener listener: listenerList) {
			if (notification instanceof SessionNotification) {
				listener.accept((SessionNotification)notification);
			}
		}
	}

	public void prompt(String sessionId, ContentBlock[] contentBlocks) {
		PromptRequest request = new PromptRequest(null, contentBlocks, sessionId);
		clientRequests(request);
		getAgentService().getAgent().prompt(request).whenComplete((result, ex) -> {
	        if (ex != null) {
	            ex.printStackTrace();
	            // Gemini CLI: cancel before first thought throws JSONRPC error
	            agentResponds(new PromptResponse(null, StopReason.refusal));
	        } else {
	           agentResponds(result);
	        }
	    });
	}
	
	public void stopPromptTurn(String sessionId) {
		CancelNotification notification = new CancelNotification(null, sessionId);
		clientNotifies(notification);
		try {
			getAgentService().getAgent().cancel(notification);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
