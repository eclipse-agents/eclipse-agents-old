package org.eclipse.mcp.acp.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
import org.eclipse.mcp.acp.protocol.AcpSchema.PromptResponse;
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
import org.eclipse.ui.console.IOConsole;

public class AcpConsole extends IOConsole implements IAcpListener {


	String sessionId;
//	PromptJob promptJob = new PromptJob();
	
	MarkdownWriter mdWriter;
	
	public AcpConsole() {
		super("ACP Console", null);
		
		mdWriter = new MarkdownWriter(this);
		
//		IDocumentPartitioner dp = getDocument().getDocumentPartitioner();
//		getDocument().setDocumentPartitioner(new AcpDocumentPartitioner(dp));
		
		AcpService.instance().addAcpListener(this);
		
		new Thread("Acp Input") {
			public void run() {
				InputStreamReader isr  = new InputStreamReader(getInputStream());
				BufferedReader br = new BufferedReader(isr);
				try {
					String line = br.readLine();
					while (line != null) {
						
						TextBlock block = new TextBlock(null, null, line, "text");
						AcpService.instance().prompt(new ContentBlock[] { block });
						line = br.readLine();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public void newSession(String sessionId) {
		this.sessionId = sessionId;
		clearConsole();
	}

	@Override
	protected void dispose() {
		super.dispose();
		AcpService.instance().removeAcpListener(this);
		//TODO needed
		mdWriter.dispose();
	}

	public void write(ContentBlock block) {
		if (block instanceof TextBlock) {
			String text = ((TextBlock)block).text();
			write(text);
		}
	}
	
	public void write(String s) {
		mdWriter.write(s);
	}

	@Override
	public void clientRequests(ClientRequest req) {
		if (req instanceof PromptRequest) {
			ContentBlock[] cbs = ((PromptRequest)req).prompt();
			for (ContentBlock cb: cbs) {
				if (cb instanceof TextBlock) {
					write(((TextBlock)cb).text());
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
			clearConsole();
			this.sessionId = ((NewSessionResponse)resp).sessionId();
			write("Session: " + ((NewSessionResponse)resp).sessionId() + "\n\n");
			
//			write("""
//# Heading 1
//## Heading 2
//### Heading 3
//```
//sample code
//```
//*Italic text* or _Italic text_
//**Bold text** or __Bold text__
//~~Strikethrough text~~
//					""");
		} else if (resp instanceof PromptResponse) {
			switch (((PromptResponse)resp).stopReason()) {
			case cancelled:
				write("Cancelled\n");
				break;
			case end_turn:
				break;
			case max_tokens:
				write("Max Tokens Reached\n");
				break;
			case max_turn_requests:
				write("Max Turns Reached\n");
				break;
			case refusal:
				write("Refused by Agent\n");
				break;
			default:
				break;
			
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
					write(block);
				}
				if (update instanceof SessionAgentMessageChunk) {
					ContentBlock block = ((SessionAgentMessageChunk)update).content();
					write(block);
				}
				if (update instanceof SessionAgentThoughtChunk) {
					ContentBlock block = ((SessionAgentThoughtChunk)update).content();
					write(block);
				}
				if (update instanceof SessionToolCall) {
					System.err.println(SessionToolCall.class.getCanonicalName());
				}
				if (update instanceof SessionToolCallUpdate) {
					System.err.println(SessionToolCallUpdate.class.getCanonicalName());
				}
				if (update instanceof SessionPlan) {
					PlanEntry[] entries = ((SessionPlan)update).entries();
					for (int i = 1; i <= entries.length; i++) {
						if (entries[i].status() == PlanEntryStatus.in_progress) {
							write("Step " + i + " of " + (entries.length + 1) + ": " + entries[i].content());
						}
					}
				}
				if (update instanceof SessionAvailableCommandsUpdate) {
					System.err.println(SessionAvailableCommandsUpdate.class.getCanonicalName());
				}
				if (update instanceof SessionModeUpdate ) {
					System.err.println(SessionModeUpdate.class.getCanonicalName());
				}
			}
			
		}
	}
}
