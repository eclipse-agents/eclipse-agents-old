package org.eclipse.acp;

import org.eclipse.acp.schema.AcpSchema.AuthenticateRequest;
import org.eclipse.acp.schema.AcpSchema.AuthenticateResponse;
import org.eclipse.acp.schema.AcpSchema.CancelNotification;
import org.eclipse.acp.schema.AcpSchema.InitializeRequest;
import org.eclipse.acp.schema.AcpSchema.InitializeResponse;
import org.eclipse.acp.schema.AcpSchema.LoadSessionRequest;
import org.eclipse.acp.schema.AcpSchema.LoadSessionResponse;
import org.eclipse.acp.schema.AcpSchema.NewSessionRequest;
import org.eclipse.acp.schema.AcpSchema.NewSessionResponse;
import org.eclipse.acp.schema.AcpSchema.PromptRequest;
import org.eclipse.acp.schema.AcpSchema.PromptResponse;
import org.eclipse.acp.schema.AcpSchema.SetSessionModeRequest;
import org.eclipse.acp.schema.AcpSchema.SetSessionModeResponse;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

@JsonSegment("session")
public interface IAcpAgent {

	@JsonRequest
	InitializeResponse initialize(InitializeRequest request);
	
	@JsonRequest
	AuthenticateResponse authenticate(AuthenticateRequest request);
	
	@JsonRequest(value = "new", useSegment = true)
	NewSessionRequest _new(NewSessionResponse request);
	
	@JsonRequest(useSegment = true)
	LoadSessionResponse load(LoadSessionRequest Response);
	
	@JsonRequest(useSegment = true)
	SetSessionModeResponse set_mode(SetSessionModeRequest request);
	
	@JsonRequest(useSegment = true)
	PromptResponse prompt(PromptRequest request);
	
	@JsonNotification(useSegment = true)
	void cancel(CancelNotification request);
	
}

