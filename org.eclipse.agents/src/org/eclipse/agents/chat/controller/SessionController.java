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
import java.util.List;
import java.util.Map;

import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.ChatBrowser;
import org.eclipse.agents.chat.ChatView;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.protocol.AcpSchema.CancelNotification;
import org.eclipse.agents.services.protocol.AcpSchema.ContentBlock;
import org.eclipse.agents.services.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.agents.services.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.agents.services.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.agents.services.protocol.AcpSchema.McpServer;
import org.eclipse.agents.services.protocol.AcpSchema.NewSessionRequest;
import org.eclipse.agents.services.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.agents.services.protocol.AcpSchema.PlanEntry;
import org.eclipse.agents.services.protocol.AcpSchema.PlanEntryStatus;
import org.eclipse.agents.services.protocol.AcpSchema.PromptRequest;
import org.eclipse.agents.services.protocol.AcpSchema.PromptResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ReleaseTerminalRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.agents.services.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.agents.services.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.agents.services.protocol.AcpSchema.SessionAgentMessageChunk;
import org.eclipse.agents.services.protocol.AcpSchema.SessionAgentThoughtChunk;
import org.eclipse.agents.services.protocol.AcpSchema.SessionAvailableCommandsUpdate;
import org.eclipse.agents.services.protocol.AcpSchema.SessionModeState;
import org.eclipse.agents.services.protocol.AcpSchema.SessionModeUpdate;
import org.eclipse.agents.services.protocol.AcpSchema.SessionModelState;
import org.eclipse.agents.services.protocol.AcpSchema.SessionNotification;
import org.eclipse.agents.services.protocol.AcpSchema.SessionPlan;
import org.eclipse.agents.services.protocol.AcpSchema.SessionToolCall;
import org.eclipse.agents.services.protocol.AcpSchema.SessionToolCallUpdate;
import org.eclipse.agents.services.protocol.AcpSchema.SessionUserMessageChunk;
import org.eclipse.agents.services.protocol.AcpSchema.SetSessionModeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.SetSessionModeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.StopReason;
import org.eclipse.agents.services.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.agents.services.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.agents.services.protocol.AcpSchema.TextBlock;
import org.eclipse.agents.services.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileResponse;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;

public class SessionController implements ISessionListener {

	// Initialization
	private IAgentService agent;
	private String sessionId; 
	private String cwd;
	private McpServer[] mcpServers; 
	private SessionModeState modes;
	private SessionModelState models;
	
	// State
//	int promptId = 0;
	private List<Object> session = new ArrayList<Object>();
	private static ListenerList<ChatView> chatViews = new ListenerList<ChatView>();
	
	enum MessageType { session_prompt, user_message_chunk, agent_thought_chunk, agent_message_chunk, resource_link };

	
	public SessionController(IAgentService agent, String sessionId, String cwd, 
			McpServer[] mcpServers, SessionModeState modes, SessionModelState models) {

		this.agent = agent;
		this.sessionId = sessionId;
		this.cwd = cwd;
		this.mcpServers = mcpServers;  
		this.modes = modes;
		this.models = models;
		
		AgentController.instance().addSessionListener(this);
	}
	
	@Override
	public String getSessionId() {
		return sessionId;
	}
	
	public static void addChatView(ChatView view) {
		chatViews.add(view);
	}
	
	public static ChatView[] getChatViews(String sessionId) {
		 return chatViews.stream().filter(cv -> {
			 return sessionId.equals(cv.getActiveSessionId());
		 }).toArray(ChatView[]::new);
	}
	
	public static void removeChatView(ChatView view) {
		chatViews.remove(view);
	}
	
	public IAgentService getAgent() {
		return agent;
	}
		
	public void prompt(ContentBlock[] contentBlocks) {
		PromptRequest request = new PromptRequest(null, contentBlocks, sessionId);
		AgentController.instance().clientRequests(request);
		agent.getAgent().prompt(request).whenComplete((result, ex) -> {
	        if (ex != null) {
	        	Tracer.trace().trace(Tracer.CHAT, "prompt error", ex); //$NON-NLS-1$
	            ex.printStackTrace();
	            
	            // Gemini CLI: cancel before first thought throws JSONRPC error
	            AgentController.instance().agentResponds(new PromptResponse(null, StopReason.refusal));
	        } else {
	        	AgentController.instance().agentResponds(result);
	        }
	    });
	}
	
	public void stopPromptTurn(String sessionId) {
		CancelNotification notification = new CancelNotification(null, sessionId);
		AgentController.instance().clientNotifies(notification);
		try {
			agent.getAgent().cancel(notification);
		} catch (Exception ex) {
			Tracer.trace().trace(Tracer.CHAT, "stop prompt error", ex); //$NON-NLS-1$
			ex.printStackTrace();
		}
	}

	//------------------------
	// AgentNotification
	//------------------------
	@Override
	public void accept(SessionNotification notification) {
		
		if (!sessionId.equals(notification.sessionId())) {
			return;
		}

		session.add(notification);
		
		for (ChatView view: getChatViews(notification.sessionId())) {
			ChatBrowser browser = view.getBrowser();

			if (notification.update() instanceof SessionUserMessageChunk) {
				browser.acceptSessionUserMessageChunk(
						((SessionUserMessageChunk)notification.update()).content());
			} else if (notification.update() instanceof SessionAgentThoughtChunk) {
				browser.acceptSessionAgentThoughtChunk(
						((SessionAgentThoughtChunk)notification.update()).content());
			} else if (notification.update() instanceof SessionAgentMessageChunk) {
				browser.acceptSessionAgentMessageChunk(
						((SessionAgentMessageChunk)notification.update()).content());
			} else if (notification.update() instanceof SessionToolCall) {
				SessionToolCall toolCall = (SessionToolCall)notification.update();
				browser.acceptSessionToolCall(
						toolCall.toolCallId(), 
						toolCall.title(), 
						toolCall.kind().toString(), 
						toolCall.status().toString());
	
			} else if (notification.update() instanceof SessionToolCallUpdate) {
				SessionToolCallUpdate toolCall = (SessionToolCallUpdate)notification.update();
				browser.acceptSessionToolCallUpdate(
						toolCall.toolCallId(), 
						toolCall.status().toString());
			}
			else if (notification.update() instanceof SessionPlan) {
				PlanEntry[] entries = ((SessionPlan)notification.update()).entries();
				for (int i = 1; i <= entries.length; i++) {
					if (entries[i].status() == PlanEntryStatus.in_progress) {
						Tracer.trace().trace(Tracer.ACP, "Step " + i + " of " + (entries.length + 1) + ": " + entries[i].content());
					}
				}
			}
			else if (notification.update() instanceof SessionAvailableCommandsUpdate) {
				Tracer.trace().trace(Tracer.ACP, SessionAvailableCommandsUpdate.class.getCanonicalName());
			}
			else if (notification.update() instanceof SessionModeUpdate ) {
				Tracer.trace().trace(Tracer.ACP, SessionModeUpdate.class.getCanonicalName());
			}
		}
	}

	//------------------------
	// AgentRequest
	//------------------------
	@Override
	public void accept(WriteTextFileRequest request) {
		writeRequests.add(request);
	}

	@Override
	public void accept(ReadTextFileRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(RequestPermissionRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(CreateTerminalRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(TerminalOutputRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(ReleaseTerminalRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(WaitForTerminalExitRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(KillTerminalCommandRequest request) {
		// TODO Auto-generated method stub
		
	}

	//------------------------
	// AgentResponse
	//------------------------
//	@Override
//	public void accept(InitializeResponse response) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void accept(AuthenticateResponse response) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void accept(NewSessionResponse response) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void accept(LoadSessionResponse response) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void accept(SetSessionModeResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(PromptResponse response) {
		TextBlock error = null;
		
		switch (((PromptResponse)response).stopReason()) {
		case cancelled:
			error = new TextBlock (null, null, "\nThe exchange has been canceled", "text");
			break;
		case end_turn:
			// the turn has ended normally
			break;
		case max_tokens:
			error = new TextBlock (null, null, "\nThe maximum token limit has been reached", "text");
			break;
		case max_turn_requests:
			error = new TextBlock (null, null, "\nThe maximum number of model requests in a single turn has been exceeded", "text");
			break;
		case refusal:
			error = new TextBlock (null, null, "\nThe Agent has refused to continue", "text");
			break;
		default:
			break;
		
		}
		
		for (ChatView view: getChatViews(sessionId)) {
			if (error != null) {
				view.getBrowser().acceptSessionAgentMessageChunk(error);
			}
			
			view.prompTurnEnded();
		}
		
		if (!fileStates.isEmpty()) {
			SyncInfoSet myEmptySet = new SyncInfoSet();
			SyncInfoSet syncSet = new SyncInfoTree();
			for (IFile file: fileStates.keySet()) {
				if (file.exists()) {

			     IResourceVariant base = fileStates.get(file).get; // null if it doesn't exist in base/remote
			     IResourceVariant remote = null; // null if it doesn't exist in base/remote
			     IResourceVariantComparator comparator = getMyTeamProviderComparator(file.getProject()); // Must be acquired from your SCM provider

	            try {
	                // 4. Instantiate SyncInfo. The constructor requires specific parameters.
	                // The 'kind' (sync state) is calculated internally by the SyncInfo using the comparator.
	                SyncInfo info = new SyncInfo(file, base, remote, comparator);
	                // The kind will likely be SyncInfo.OUTGOING | SyncInfo.ADDITION if it's new locally.

	                // 5. Add the SyncInfo to the set
	                syncSet.add(info);

	            } catch (Exception e) {
	                // Handle exceptions (e.g., CoreException if comparator is missing)
	                e.printStackTrace();
	            }
			        }
			    }
				myEmptySet.add(new SyncInfo());
			}
		}
		
		
	}

	//------------------------
	// AgentResponse
	//------------------------
	@Override
	public void accept(CancelNotification notification) {
		// TODO Auto-generated method stub
	}

	//------------------------
	// ClientNotification
	//------------------------
//	@Override
//	public void accept(InitializeRequest request) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void accept(AuthenticateRequest request) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void accept(NewSessionRequest request) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void accept(LoadSessionRequest request) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void accept(SetSessionModeRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(PromptRequest request) {
		writeRequests.clear();
		for (ChatView view: getChatViews(sessionId)) {
			view.getBrowser().acceptPromptRequest(request);
			view.prompTurnStarted();
		}
		
	}

	//------------------------
	// ClientResponse
	//------------------------
	@Override
	public void accept(WriteTextFileResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(ReadTextFileResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(RequestPermissionResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(CreateTerminalResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(TerminalOutputResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(ReleaseTerminalResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(WaitForTerminalExitResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(KillTerminalCommandResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(InitializeResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(NewSessionResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(InitializeRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(NewSessionRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileAboutToBeChanged(String sessionId, IFile file) {
		if (!sessionId.equals(sessionId)) {
			return;
		}
		
		if (!fileStates.containsKey(file)) {
			 try {
				IFileState[] history = file.getHistory(new NullProgressMonitor());
				 if (history.length > 0) {
					 fileStates.put(file,  history[history.length - 1]);
				}
			 } catch (CoreException e) {
				e.printStackTrace();
			 }
		}
	}
	
	
}
