/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2022, 2025
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package org.eclipse.mcp.acp.protocol;

import java.util.concurrent.CompletableFuture;

import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.WriteTextFileResponse;

public class AcpClient implements IAcpClient {

	IAgentService service;
	
	public AcpClient(IAgentService service) {
		this.service = service;
	}

	@Override
	public CompletableFuture<RequestPermissionResponse> requestPermission(RequestPermissionRequest request) {
//		try {
//			output.write("Request Permission: " + request.toolCall().title());
//			for (int i = 0; i < request.options().length; i++) {
//				output.write("\t" + i + ". " + request.options()[i].name());
//			}
//			console.getInputStream().read();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
		AcpService.instance().agentNotifies(notification);
	}

}
