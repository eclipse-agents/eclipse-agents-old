package org.eclipse.mcp.acp;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.mcp.acp.AcpSchema.AuthenticateRequest;
import org.eclipse.mcp.acp.AcpSchema.AuthenticateResponse;
import org.eclipse.mcp.acp.AcpSchema.CancelNotification;
import org.eclipse.mcp.acp.AcpSchema.InitializeRequest;
import org.eclipse.mcp.acp.AcpSchema.InitializeResponse;
import org.eclipse.mcp.acp.AcpSchema.LoadSessionRequest;
import org.eclipse.mcp.acp.AcpSchema.LoadSessionResponse;
import org.eclipse.mcp.acp.AcpSchema.NewSessionRequest;
import org.eclipse.mcp.acp.AcpSchema.NewSessionResponse;
import org.eclipse.mcp.acp.AcpSchema.PromptRequest;
import org.eclipse.mcp.acp.AcpSchema.PromptResponse;
import org.eclipse.mcp.acp.AcpSchema.SetSessionModeRequest;
import org.eclipse.mcp.acp.AcpSchema.SetSessionModeResponse;

@JsonSegment("session")
public interface IAcpAgent {

	@JsonRequest
	CompletableFuture<InitializeResponse> initialize(InitializeRequest request);
	
	@JsonRequest
	CompletableFuture<AuthenticateResponse> authenticate(AuthenticateRequest request);
	
	@JsonRequest(value = "new", useSegment = true)
	CompletableFuture<NewSessionRequest> _new(NewSessionResponse request);
	
	@JsonRequest(useSegment = true)
	CompletableFuture<LoadSessionResponse> load(LoadSessionRequest Response);
	
	@JsonRequest(useSegment = true)
	CompletableFuture<SetSessionModeResponse> set_mode(SetSessionModeRequest request);
	
	@JsonRequest(useSegment = true)
	CompletableFuture<PromptResponse> prompt(PromptRequest request);
	
	@JsonNotification(useSegment = true)
	void cancel(CancelNotification request);
	
}

