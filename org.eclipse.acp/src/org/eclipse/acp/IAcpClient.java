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
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

@JsonSegment("server")
public interface IAcpClient {


   @JsonRequest(value = "request_permission")
   RequestPermissionResponse requestPermission(RequestPermissionRequest request);
   
   @JsonRequest(value = "fs/read_text_file")
   ReadTextFileResponse readTextFile(ReadTextFileRequest request);
   
   @JsonRequest(value = "fs/write_text_file")
   WriteTextFileResponse writeTextFile(WriteTextFileRequest request);
   
   @JsonRequest(value = "terminal/create")
   CreateTerminalResponse terminalCreate(CreateTerminalRequest request);
   
   @JsonRequest(value = "terminal/output")
   TerminalOutputResponse terminalOutput(TerminalOutputRequest request);
   
   @JsonRequest(value = "terminal/release")
   ReleaseTerminalResponse terminalRelease(WaitForTerminalExitRequest request);
   
   @JsonRequest(value = "terminal/wait_for_exit")
   WaitForTerminalExitResponse terminalWaitForExit(CreateTerminalRequest request);
   
   @JsonRequest(value = "terminal/kill")
   KillTerminalCommandResponse terminalKill(KillTerminalCommandRequest request);

   @JsonNotification(value = "session/update")
   void update(SessionNotification notification);
}
