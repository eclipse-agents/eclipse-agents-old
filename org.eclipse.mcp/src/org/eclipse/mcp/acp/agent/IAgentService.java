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
package org.eclipse.mcp.acp.agent;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.mcp.acp.protocol.AcpSchema.AuthenticateResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.protocol.IAcpAgent;

public interface IAgentService {

	public String getName();
	
	public String getId();
	
	public String[] getDefaultStartupCommand();

	public void start();
	
	public void stop();
	
	public boolean isRunning();
	
	public IAcpAgent getAgent();
	
	public InputStream getInputStream();

	public OutputStream getOutputStream();

	public InputStream getErrorStream();
	
	public InitializeRequest getInitializeRequest() ;

	public void setInitializeRequest(InitializeRequest initializeRequest);

	public InitializeResponse getInitializeResponse();

	public void setInitializeResponse(InitializeResponse initializeResponse);

	public AuthenticateResponse getAuthenticateResponse();

	public void setAuthenticateResponse(AuthenticateResponse authenticateResponse);
	
}
