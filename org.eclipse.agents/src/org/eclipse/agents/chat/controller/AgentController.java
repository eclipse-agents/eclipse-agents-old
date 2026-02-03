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
package org.eclipse.agents.chat.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.agents.Activator;
import org.eclipse.agents.preferences.IPreferenceConstants;
import org.eclipse.agents.services.agent.CustomAgentService;
import org.eclipse.agents.services.agent.GeminiService;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.protocol.AcpSchema.AgentNotification;
import org.eclipse.agents.services.protocol.AcpSchema.AgentRequest;
import org.eclipse.agents.services.protocol.AcpSchema.AgentResponse;
import org.eclipse.agents.services.protocol.AcpSchema.CancelNotification;
import org.eclipse.agents.services.protocol.AcpSchema.ClientNotification;
import org.eclipse.agents.services.protocol.AcpSchema.ClientRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ClientResponse;
import org.eclipse.agents.services.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.agents.services.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.agents.services.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.agents.services.protocol.AcpSchema.NewSessionRequest;
import org.eclipse.agents.services.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.agents.services.protocol.AcpSchema.PromptRequest;
import org.eclipse.agents.services.protocol.AcpSchema.PromptResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ReleaseTerminalRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.agents.services.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.agents.services.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.agents.services.protocol.AcpSchema.SessionNotification;
import org.eclipse.agents.services.protocol.AcpSchema.SetSessionModeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.SetSessionModeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.agents.services.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.agents.services.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileResponse;
import org.eclipse.core.runtime.ListenerList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AgentController {

	private static AgentController instance;
	
	private static Map<String, SessionController> sessions = new HashMap<String, SessionController>();
	
	private ListenerList<IAgentServiceListener> agentListeners;
	private ListenerList<ISessionListener> sesionListeners;

	static {
		instance = new AgentController();
	}
	
	List<IAgentService> agentServices;
	private AgentController() {
		agentServices = new ArrayList<IAgentService>();
		agentServices.add(new GeminiService());
		agentListeners = new ListenerList<IAgentServiceListener>();
		sesionListeners = new  ListenerList<ISessionListener>();
		loadCustomAgents();
	}

	public static AgentController instance() {
		return instance;
	}

	public IAgentService[] getAgents() {
		return agentServices.toArray(new IAgentService[agentServices.size()]);
	}

	public void addAgent(IAgentService agent) {
		agentServices.add(agent);
	}

	public void removeAgent(IAgentService agent) {
		if (agent.isRunning()) {
			agent.stop();
		}
		if (agent.isScheduled()) {
			agent.unschedule();
		}
		agentServices.remove(agent);
	}

	public void refreshCustomAgents() {
		// Stop and remove existing custom agents
		List<IAgentService> toRemove = new ArrayList<IAgentService>();
		for (IAgentService agent : agentServices) {
			if (agent instanceof CustomAgentService) {
				if (agent.isRunning()) {
					agent.stop();
				}
				if (agent.isScheduled()) {
					agent.unschedule();
				}
				toRemove.add(agent);
			}
		}
		agentServices.removeAll(toRemove);
		// Reload from preferences
		loadCustomAgents();
	}

	private void loadCustomAgents() {
		try {
			String json = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.P_ACP_CUSTOM_AGENTS);
			if (json == null || json.isEmpty()) {
				return;
			}
			Gson gson = new Gson();
			JsonArray array = gson.fromJson(json, JsonArray.class);
			if (array == null) {
				return;
			}
			for (JsonElement element : array) {
				JsonObject obj = element.getAsJsonObject();
				String id = obj.get("id").getAsString();
				String name = obj.get("name").getAsString();
				JsonArray cmdArray = obj.getAsJsonArray("command");
				String[] command = new String[cmdArray.size()];
				for (int i = 0; i < cmdArray.size(); i++) {
					command[i] = cmdArray.get(i).getAsString();
				}
				String workingDirectory = null;
				if (obj.has("workingDirectory") && !obj.get("workingDirectory").isJsonNull()) {
					workingDirectory = obj.get("workingDirectory").getAsString();
				}
				agentServices.add(new CustomAgentService(id, name, command, workingDirectory));
			}
		} catch (Exception e) {
			// If preferences are corrupt, just skip loading custom agents
		}
	}
	
	public static void putSession(String sessionId, SessionController controller) {
		sessions.put(sessionId, controller);
	}
	
	public static SessionController getSession(String sessionId) {
		return sessions.get(sessionId);
	}
	
	public static int getSessionCount() {
		return sessions.size();
	}
	
	public void addSessionListener(ISessionListener listener) {
		sesionListeners.add(listener);
	}
	
	public void removeSessionListener(ISessionListener listener) {
		sesionListeners.remove(listener);
	}
	
	public void addAgentListener(IAgentServiceListener listener) {
		agentListeners.add(listener);
	}
	
	public void removeAgentListener(IAgentServiceListener listener) {
		agentListeners.remove(listener);
	}
	
	public void clientRequests(ClientRequest req) {
		for (ISessionListener listener: sesionListeners) {
			if (req instanceof InitializeRequest) {
				listener.accept((InitializeRequest)req);	
//			} else if (req instanceof AuthenticateRequest) {
//				listener.accept((AuthenticateRequest)req);
			} else if (req instanceof NewSessionRequest) {
				listener.accept((NewSessionRequest)req);
//			} else if (req instanceof LoadSessionRequest) {
//				listener.accept((LoadSessionRequest)req);
			} else if (req instanceof SetSessionModeRequest) {
				listener.accept((SetSessionModeRequest)req);
			} else if (req instanceof PromptRequest) {
				listener.accept((PromptRequest)req);
			}
		}
	}
	
	public void clientResponds(ClientResponse resp) {
		for (ISessionListener listener: sesionListeners) {
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
		for (ISessionListener listener: sesionListeners) {
			if (notification instanceof CancelNotification) {
				listener.accept((CancelNotification)notification);
			}
		}
	}
	
	public void agentRequests(AgentRequest req) {
		for (ISessionListener listener : sesionListeners) {
			if (req instanceof ReadTextFileRequest) {
				listener.accept((ReadTextFileRequest)req);
			} else if (req instanceof WriteTextFileRequest) {
				listener.accept((WriteTextFileRequest)req);
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
		for (ISessionListener listener: sesionListeners) {
			if (resp instanceof InitializeResponse) {
				listener.accept((InitializeResponse)resp);
//			if (resp instanceof AuthenticateResponse) {
//				listener.accept((AuthenticateResponse)resp);
			} else if (resp instanceof NewSessionResponse) {
				listener.accept((NewSessionResponse)resp);
//			} else if (resp instanceof LoadSessionResponse) {
//				listener.accept((LoadSessionResponse)resp);
			} else if (resp instanceof SetSessionModeResponse) {
				listener.accept((SetSessionModeResponse)resp);
			} else if (resp instanceof PromptResponse) {
				listener.accept((PromptResponse)resp);
			}
		}
	}
	
	public void agentNotifies(AgentNotification notification) {
		for (ISessionListener listener: sesionListeners) {
			if (notification instanceof SessionNotification) {
				listener.accept((SessionNotification)notification);
			}
		}
	}
	
	public void acceptRequestsPermission(RequestPermissionRequest permissionRequest, CompletableFuture<RequestPermissionResponse> pendingResponse) {
		for (ISessionListener listener: sesionListeners) {
			listener.accept(permissionRequest, pendingResponse);
		}
	}

	public void agentStarted(IAgentService service) {
		for (IAgentServiceListener listener: agentListeners) {
			listener.agentStarted(service);
		}
	}
	
	public void agentStopped(IAgentService service) {
		for (IAgentServiceListener listener: agentListeners) {
			listener.agentStopped(service);
		}
	}
	
	public void agentFailed(IAgentService service) {
		for (IAgentServiceListener listener: agentListeners) {
			listener.agentFailed(service);
		}
	}
	
	public void agentScheduled(IAgentService service) {
		for (IAgentServiceListener listener: agentListeners) {
			listener.agentScheduled(service);
		}
	}
}
