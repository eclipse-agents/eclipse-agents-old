package org.eclipse.mcp.acp.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mcp.acp.protocol.AcpSchema.AudioBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.AuthenticateRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.AuthenticateResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.CancelNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.ContentBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.EmbeddedResourceBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.ImageBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.LoadSessionRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.LoadSessionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.PromptRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.PromptResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReleaseTerminalRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ResourceLinkBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionAgentMessageChunk;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionAgentThoughtChunk;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionUserMessageChunk;
import org.eclipse.mcp.acp.protocol.AcpSchema.SetSessionModeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.SetSessionModeResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.TextBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.WriteTextFileResponse;

public class AcpSessionModel {

	private static Map<String, AcpSessionModel> sessions = new HashMap<String, AcpSessionModel>();
	
	public AcpSessionModel(String sessionId) {
		this.sessionId = sessionId;
		sessions.put(sessionId, this);
	}
	
	public static AcpSessionModel getSession(String sessionId) {
		return sessions.get(sessionId);
	}
	
	String sessionId;
	int index = 0;
	List<Object> session = new ArrayList<Object>();
	List<Object> prompt = new ArrayList<Object>();
	String promptId;
	AcpBrowser browser;
	
	enum MessageType { session_prompt, agent_thought_chunk, agent_message_chunk };


	public String getSessionId() {
		return sessionId;
	}

	public void add(SessionNotification notification) {
		if (notification.update() instanceof SessionUserMessageChunk) {
			
		} else if (notification.update() instanceof SessionAgentThoughtChunk) {
			session.add(notification);
			String clazz = MessageType.agent_thought_chunk.name();
			String id = clazz + "-" + ++index;
			addMessage(id, clazz, ((SessionAgentThoughtChunk)notification.update()).content());
		} else if (notification.update() instanceof SessionAgentMessageChunk) {
			session.add(notification);
			if (prompt.isEmpty()) {
				String clazz = MessageType.agent_message_chunk.name();
				promptId = clazz + "-" + ++index;
				addMessage(promptId, clazz, ((SessionAgentMessageChunk)notification.update()).content());
			} else {
				updateMessage(promptId, ((SessionAgentMessageChunk)notification.update()).content());
			}
			prompt.add(notification);
		}
	}

	public void add(WriteTextFileRequest o) {

	}

	public void add(ReadTextFileRequest o) {

	}

	public void add(RequestPermissionRequest o) {

	}

	public void add(CreateTerminalRequest o) {

	}

	public void add(TerminalOutputRequest o) {

	}

	public void add(ReleaseTerminalRequest o) {

	}

	public void add(WaitForTerminalExitRequest o) {

	}

	public void add(KillTerminalCommandRequest o) {

	}

	public void add(InitializeResponse o) {

	}

	public void add(AuthenticateResponse o) {

	}

	public void add(NewSessionResponse o) {

	}

	public void add(LoadSessionResponse o) {

	}

	public void add(SetSessionModeResponse o) {

	}

	public void add(PromptResponse o) {
		prompt.clear();
	}

	public void add(CancelNotification o) {

	}

	public void add(InitializeRequest o) {

	}

	public void add(AuthenticateRequest o) {

	}

	public void add(NewSessionRequest o) {

	}

	public void add(LoadSessionRequest o) {

	}

	public void add(SetSessionModeRequest o) {

	}

	public void add(PromptRequest request) {
		ContentBlock[] cbs = request.prompt();
		for (ContentBlock cb: cbs) {
			String clazz = MessageType.session_prompt.name();
			String id = clazz + "-" + ++index;
			addMessage(id, clazz, cb);
		}
	}

	public void add(WriteTextFileResponse o) {

	}

	public void add(ReadTextFileResponse o) {

	}

	public void add(RequestPermissionResponse o) {

	}

	public void add(CreateTerminalResponse o) {

	}

	public void add(TerminalOutputResponse o) {

	}

	public void add(ReleaseTerminalResponse o) {

	}

	public void add(WaitForTerminalExitResponse o) {

	}

	public void add(KillTerminalCommandResponse o) {

	}

	
	private void addMessage(String id, String clazz, ContentBlock content) {
		if (content instanceof TextBlock) {
			browser.addMessage(id, clazz, ((TextBlock)content).text());
		} else if (content instanceof ImageBlock) {
			
		} else if (content instanceof AudioBlock) {
				
		} else if (content instanceof ResourceLinkBlock) {
					
		} else if (content instanceof EmbeddedResourceBlock) {
					
		}
	}
	
	private void updateMessage(String id, ContentBlock content) {
		if (content instanceof TextBlock) {
			browser.updateMessage(id, ((TextBlock)content).text());
		} else if (content instanceof ImageBlock) {
			
		} else if (content instanceof AudioBlock) {
				
		} else if (content instanceof ResourceLinkBlock) {
					
		} else if (content instanceof EmbeddedResourceBlock) {
					
		}
	}
}
