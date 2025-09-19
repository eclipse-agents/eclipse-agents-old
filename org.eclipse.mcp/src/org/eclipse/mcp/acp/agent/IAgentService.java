package org.eclipse.mcp.acp.agent;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.mcp.acp.protocol.IAcpAgent;

public interface IAgentService {

	public String getName();

	public void start();
	
	public void stop();
	
	public IAcpAgent getAgent();
	
	public InputStream getInputStream();

	public OutputStream getOutputStream();

	public InputStream getErrorStream();
}
