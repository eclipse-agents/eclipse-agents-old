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
package org.eclipse.agents.acp.protocol;

import java.util.concurrent.CompletableFuture;

import org.eclipse.agents.acp.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionNotification;
import org.eclipse.agents.acp.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.agents.acp.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.WriteTextFileResponse;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface IAcpClient {


   @JsonRequest(value = "session/request_permission")
   CompletableFuture<RequestPermissionResponse> requestPermission(RequestPermissionRequest request);
   
   @JsonRequest(value = "fs/read_text_file")
   CompletableFuture<ReadTextFileResponse> readTextFile(ReadTextFileRequest request);
   
   @JsonRequest(value = "fs/write_text_file")
   CompletableFuture<WriteTextFileResponse> writeTextFile(WriteTextFileRequest request);
   
   @JsonRequest(value = "terminal/create")
   CompletableFuture<CreateTerminalResponse> terminalCreate(CreateTerminalRequest request);
   
   @JsonRequest(value = "terminal/output")
   CompletableFuture<TerminalOutputResponse> terminalOutput(TerminalOutputRequest request);
   
   @JsonRequest(value = "terminal/release")
   CompletableFuture<ReleaseTerminalResponse> terminalRelease(WaitForTerminalExitRequest request);
   
   @JsonRequest(value = "terminal/wait_for_exit")
   CompletableFuture<WaitForTerminalExitResponse> terminalWaitForExit(CreateTerminalRequest request);
   
   @JsonRequest(value = "terminal/kill")
   CompletableFuture<KillTerminalCommandResponse> terminalKill(KillTerminalCommandRequest request);

   @JsonNotification(value = "session/update")
   void update(SessionNotification notification);
}
