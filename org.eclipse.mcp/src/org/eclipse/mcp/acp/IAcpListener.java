package org.eclipse.mcp.acp;

import org.eclipse.mcp.acp.protocol.AcpSchema.AgentNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.AgentResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ClientResponse;

public interface IAcpListener {

	public void clientRequests(ClientRequest req);
	
	public void clientResponds(ClientResponse resp);
	
	public void clientNotifies(ClientNotification notification);
	
	public void agentRequests(AgentRequest req);
	
	public void agentResponds(AgentResponse resp);
	
	public void agentNotifies(AgentNotification notification);
	
}
