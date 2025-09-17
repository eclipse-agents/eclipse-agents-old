package org.eclipse.mcp.acp;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
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

@JsonSegment("server")
public interface IAcpClient {


   @JsonRequest(value = "request_permission")
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
