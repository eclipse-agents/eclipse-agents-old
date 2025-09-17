/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2022, 2025
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package org.eclipse.acp;

import org.eclipse.acp.schema.AcpSchema.CreateTerminalRequest;
import org.eclipse.acp.schema.AcpSchema.CreateTerminalResponse;
import org.eclipse.acp.schema.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.acp.schema.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.acp.schema.AcpSchema.ReadTextFileRequest;
import org.eclipse.acp.schema.AcpSchema.ReadTextFileResponse;
import org.eclipse.acp.schema.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.acp.schema.AcpSchema.RequestPermissionRequest;
import org.eclipse.acp.schema.AcpSchema.RequestPermissionResponse;
import org.eclipse.acp.schema.AcpSchema.SessionNotification;
import org.eclipse.acp.schema.AcpSchema.TerminalOutputRequest;
import org.eclipse.acp.schema.AcpSchema.TerminalOutputResponse;
import org.eclipse.acp.schema.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.acp.schema.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.acp.schema.AcpSchema.WriteTextFileRequest;
import org.eclipse.acp.schema.AcpSchema.WriteTextFileResponse;

public class AcpClient implements IAcpClient {

	private ContextStore<IAcpClient> store;

	public AcpClient(ContextStore<IAcpClient> store) {
		this.store = store;
	}

	@Override
	public RequestPermissionResponse requestPermission(RequestPermissionRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReadTextFileResponse readTextFile(ReadTextFileRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WriteTextFileResponse writeTextFile(WriteTextFileRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CreateTerminalResponse terminalCreate(CreateTerminalRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TerminalOutputResponse terminalOutput(TerminalOutputRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReleaseTerminalResponse terminalRelease(WaitForTerminalExitRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WaitForTerminalExitResponse terminalWaitForExit(CreateTerminalRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KillTerminalCommandResponse terminalKill(KillTerminalCommandRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(SessionNotification notification) {
		// TODO Auto-generated method stub
		
	}
}
