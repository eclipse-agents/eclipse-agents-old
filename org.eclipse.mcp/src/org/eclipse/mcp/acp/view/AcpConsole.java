package org.eclipse.mcp.acp.view;

import java.io.IOException;

import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.mcp.Activator;
import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.IAcpListener;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ContentBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.EmbeddedResourceBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.PlanEntry;
import org.eclipse.mcp.acp.protocol.AcpSchema.PlanEntryStatus;
import org.eclipse.mcp.acp.protocol.AcpSchema.PromptRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ResourceLinkBlock;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionAgentMessageChunk;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionAgentThoughtChunk;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionAvailableCommandsUpdate;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionModeUpdate;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionPlan;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionToolCall;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionToolCallUpdate;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionUpdate;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionUserMessageChunk;
import org.eclipse.mcp.acp.protocol.AcpSchema.TextBlock;
import org.eclipse.swt.SWT;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class AcpConsole extends IOConsole implements IAcpListener {

	IOConsoleOutputStream userStream, agentStream, thoughtStream, traceStream, errorStream;
	String sessionId;
	
	public AcpConsole() {
		super("ACP Console", null);
		
		agentStream = newOutputStream();
		
		traceStream = newOutputStream();
		traceStream.setColor(Activator.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		
		errorStream = newOutputStream();
		errorStream.setColor(Activator.getDisplay().getSystemColor(SWT.COLOR_RED));
		
		thoughtStream = newOutputStream();
		thoughtStream.setColor(Activator.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		thoughtStream.setFontStyle(SWT.ITALIC);
		
		userStream = newOutputStream();
		
//		IDocumentPartitioner dp = getDocument().getDocumentPartitioner();
//		getDocument().setDocumentPartitioner(new AcpDocumentPartitioner(dp));
		
		AcpService.instance().addAcpListener(this);
	}
	
	public void newSession(String sessionId) {
		this.sessionId = sessionId;
		clearConsole();
	}

	@Override
	protected void dispose() {
		super.dispose();
		AcpService.instance().removeAcpListener(this);
		
		//TODO needed?
		try {
			userStream.close();
			agentStream.close();
			thoughtStream.close();
			traceStream.close();
			errorStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void write(IOConsoleOutputStream stream, ContentBlock block) {
		if (block instanceof TextBlock) {
			String text = ((TextBlock)block).text();
			write(stream, text);
		}
	}
	
	public void write(IOConsoleOutputStream stream, String s) {
		if (!stream.isClosed()) {
			try {
				stream.write(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void clientRequests(ClientRequest req) {
		if (req instanceof PromptRequest) {
			ContentBlock[] cbs = ((PromptRequest)req).prompt();
			for (ContentBlock cb: cbs) {
				if (cb instanceof TextBlock) {
					write(userStream, ((TextBlock)cb).text());
				} else if (cb instanceof EmbeddedResourceBlock) {
					
				} else if (cb instanceof ResourceLinkBlock) {
					
				}
			}
		}
	}

	@Override
	public void clientResponds(ClientResponse resp) {
		
	}

	@Override
	public void clientNotifies(ClientNotification notification) {
		
	}

	@Override
	public void agentRequests(AgentRequest req) {
		
	}

	@Override
	public void agentResponds(AgentResponse resp) {
		if (resp instanceof NewSessionResponse) {
			try {
				clearConsole();
				this.sessionId = ((NewSessionResponse)resp).sessionId();
				traceStream.write("new Session: " + ((NewSessionResponse)resp).sessionId());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void agentNotifies(AgentNotification notification) {
		if (notification instanceof SessionNotification) {
			SessionNotification sn = (SessionNotification)notification;
			if (sessionId.equals(sn.sessionId())) {
				SessionUpdate update = sn.update();
				
				if (update instanceof SessionUserMessageChunk) {
					ContentBlock block = ((SessionUserMessageChunk)update).content();
					write(userStream, block);
				}
				if (update instanceof SessionAgentMessageChunk) {
					ContentBlock block = ((SessionAgentMessageChunk)update).content();
					write(agentStream, block);
				}
				if (update instanceof SessionAgentThoughtChunk) {
					ContentBlock block = ((SessionAgentThoughtChunk)update).content();
					write(thoughtStream, block);
				}
				if (update instanceof SessionToolCall) {
					
				}
				if (update instanceof SessionToolCallUpdate) {
					
				}
				if (update instanceof SessionPlan) {
					PlanEntry[] entries = ((SessionPlan)update).entries();
					for (int i = 1; i <= entries.length; i++) {
						if (entries[i].status() == PlanEntryStatus.in_progress) {
							write(thoughtStream, "Step " + i + " of " + (entries.length + 1) + ": " + entries[i].content());
						}
					}
				}
				if (update instanceof SessionAvailableCommandsUpdate) {
					
				}
				if (update instanceof SessionModeUpdate ) {
				
				}
			}
			
		}
	}
	
	
}
