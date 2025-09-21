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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.mcp.Activator;
import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.Outcome;
import org.eclipse.mcp.acp.protocol.AcpSchema.PermissionOption;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionOutcome;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.SessionNotification;
import org.eclipse.mcp.acp.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.mcp.acp.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.mcp.acp.protocol.AcpSchema.WriteTextFileResponse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.SelectionDialog;

public class AcpClient implements IAcpClient {

	IAgentService service;
	
	public AcpClient(IAgentService service) {
		this.service = service;
	}

	@Override
	public CompletableFuture<RequestPermissionResponse> requestPermission(RequestPermissionRequest request) {
		CompletableFuture<RequestPermissionResponse>  future = new CompletableFuture<RequestPermissionResponse>();
		Activator.getDisplay().syncExec(new Runnable() {
			public void run() {
				SelectionDialog dialog = new SelectionDialog(Activator.getDisplay().getActiveShell()) {

					@Override
					protected Control createDialogArea(Composite parent) {
						Composite top = (Composite) super.createDialogArea(parent);
						top.setLayout(new GridLayout(1, true));
						
						Combo combo = new Combo(top, SWT.READ_ONLY);
						for (PermissionOption po: request.options()) {
							combo.add(po.name());
						}
						combo.addModifyListener(new ModifyListener() {
							@Override
							public void modifyText(ModifyEvent arg0) {
								setSelectionResult(new Object[] { 
										request.options()[combo.getSelectionIndex()]
								});
								getOkButton().setEnabled(true);
							}
						});
						return top;
					}
				};
				
				String message =  
						"Agent would like to call " + request.toolCall().toolCallId() + ": " 
								+ request.toolCall().title() + " TODO";
				
				dialog.setMessage(message);
				if (dialog.open() == Dialog.OK) {
					Object result = dialog.getResult()[0];
					if (result instanceof PermissionOption) {
						PermissionOption option = (PermissionOption)result;
						RequestPermissionOutcome outcome = new RequestPermissionOutcome(Outcome.selected, option.optionId());
						future.complete(new RequestPermissionResponse(null, outcome));
					}
				}
			}
		});
		
		
		return future;
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
