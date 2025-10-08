package org.eclipse.mcp.acp.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.IAcpSessionListener;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.protocol.AcpSchema.CancelNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.McpServer;
import org.eclipse.mcp.acp.protocol.AcpSchema.PlanEntry;
import org.eclipse.mcp.acp.protocol.AcpSchema.PlanEntryStatus;
import org.eclipse.mcp.acp.protocol.AcpSchema.PromptRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.PromptResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReleaseTerminalRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionAgentMessageChunk;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionAgentThoughtChunk;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionAvailableCommandsUpdate;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionModeState;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionModeUpdate;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionPlan;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionToolCall;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionToolCallUpdate;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionUserMessageChunk;
import org.eclipse.mcp.acp.protocol.AcpSchema.SetSessionModeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.SetSessionModeResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.WriteTextFileResponse;

public class AcpSessionModel implements IAcpSessionListener {

	// Initialization
	IAgentService agent;
	String sessionId; 
	String cwd;
	McpServer[] mcpServers; 
	SessionModeState modes;
	
	// State
//	int promptId = 0;
	List<Object> session = new ArrayList<Object>();
	
	AcpBrowser browser;
	
	enum MessageType { session_prompt, user_message_chunk, agent_thought_chunk, agent_message_chunk, resource_link };

	
	public AcpSessionModel(IAgentService agent, String sessionId, String cwd, McpServer[] mcpServers, SessionModeState modes) {
		this.agent = agent;
		this.sessionId = sessionId;
		this.cwd = cwd;
		this.mcpServers = mcpServers;  
		this.modes = modes;
		
		AcpService.instance().addAcpListener(this);
	}
	
	@Override
	public String getSessionId() {
		return sessionId;
	}
	
	public void setBrowser(AcpBrowser browser) {
		this.browser = browser;
		
		
	}
	
	public IAgentService getAgent() {
		return agent;
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
		
		
		if (notification.update() instanceof SessionUserMessageChunk) {
			browser.acceptSessionUserMessageChunk(
					((SessionUserMessageChunk)notification.update()).content());
		} else if (notification.update() instanceof SessionAgentThoughtChunk) {
			browser.acceptSessionAgentThoughtChunk(
					((SessionAgentThoughtChunk)notification.update()).content());
		} else if (notification.update() instanceof SessionAgentMessageChunk) {
			browser.acceptSessionAgentMessageChunk(
					((SessionAgentMessageChunk)notification.update()).content());
		}
		else if (notification.update() instanceof SessionToolCall) {
			
			System.err.println(SessionToolCall.class.getCanonicalName());
		}
		else if (notification.update() instanceof SessionToolCallUpdate) {
			System.err.println(SessionToolCallUpdate.class.getCanonicalName());
		}
		else if (notification.update() instanceof SessionPlan) {
			PlanEntry[] entries = ((SessionPlan)notification.update()).entries();
			for (int i = 1; i <= entries.length; i++) {
				if (entries[i].status() == PlanEntryStatus.in_progress) {
					System.err.println("Step " + i + " of " + (entries.length + 1) + ": " + entries[i].content());
				}
			}
		}
		else if (notification.update() instanceof SessionAvailableCommandsUpdate) {
			System.err.println(SessionAvailableCommandsUpdate.class.getCanonicalName());
		}
		else if (notification.update() instanceof SessionModeUpdate ) {
			System.err.println(SessionModeUpdate.class.getCanonicalName());
		}
	}

	//------------------------
	// AgentRequest
	//------------------------
	@Override
	public void accept(WriteTextFileRequest request) {
		// TODO Auto-generated method stub
		
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
		switch (((PromptResponse)response).stopReason()) {
		case cancelled:
//			write("Cancelled\n");
			break;
		case end_turn:
			break;
		case max_tokens:
//			write("Max Tokens Reached\n");
			break;
		case max_turn_requests:
//			write("Max Turns Reached\n");
			break;
		case refusal:
//			write("Refused by Agent\n");
			break;
		default:
			break;
		
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
		browser.acceptPromptRequest(request);
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
}
