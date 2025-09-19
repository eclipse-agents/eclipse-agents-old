package org.eclipse.mcp.acp.agent;

import org.eclipse.mcp.acp.protocol.IAcpAgent;

public interface IAgentService {

	public String getName();

	public void start();
	
	public void stop();
	
	public IAcpAgent getAgent();
}
