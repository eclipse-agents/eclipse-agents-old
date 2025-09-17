/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2022, 2025
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package org.eclipse.mcp.acp;

import java.util.concurrent.CompletableFuture;

import org.eclipse.mcp.acp.AcpSchema.CreateTerminalRequest;
import org.eclipse.mcp.acp.AcpSchema.CreateTerminalResponse;
import org.eclipse.mcp.acp.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.mcp.acp.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.mcp.acp.AcpSchema.ReadTextFileRequest;
import org.eclipse.mcp.acp.AcpSchema.ReadTextFileResponse;
import org.eclipse.mcp.acp.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.mcp.acp.AcpSchema.RequestPermissionRequest;
import org.eclipse.mcp.acp.AcpSchema.RequestPermissionResponse;
import org.eclipse.mcp.acp.AcpSchema.SessionNotification;
import org.eclipse.mcp.acp.AcpSchema.TerminalOutputRequest;
import org.eclipse.mcp.acp.AcpSchema.TerminalOutputResponse;
import org.eclipse.mcp.acp.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.mcp.acp.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.mcp.acp.AcpSchema.WriteTextFileRequest;
import org.eclipse.mcp.acp.AcpSchema.WriteTextFileResponse;

public class AcpClient implements IAcpClient {

	public AcpClient() {
	}

	@Override
	public CompletableFuture<RequestPermissionResponse> requestPermission(RequestPermissionRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<ReadTextFileResponse> readTextFile(ReadTextFileRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<WriteTextFileResponse> writeTextFile(WriteTextFileRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<CreateTerminalResponse> terminalCreate(CreateTerminalRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<TerminalOutputResponse> terminalOutput(TerminalOutputRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<ReleaseTerminalResponse> terminalRelease(WaitForTerminalExitRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<WaitForTerminalExitResponse> terminalWaitForExit(CreateTerminalRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<KillTerminalCommandResponse> terminalKill(KillTerminalCommandRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(SessionNotification notification) {
		// TODO Auto-generated method stub
		System.out.println();
	}

}
