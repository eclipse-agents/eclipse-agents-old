package org.eclipse.agents.chat.controller;

import java.util.concurrent.CompletableFuture;

import org.eclipse.agents.services.protocol.AcpSchema.CancelNotification;
import org.eclipse.agents.services.protocol.AcpSchema.CreateTerminalRequest;
import org.eclipse.agents.services.protocol.AcpSchema.CreateTerminalResponse;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.KillTerminalCommandRequest;
import org.eclipse.agents.services.protocol.AcpSchema.KillTerminalCommandResponse;
import org.eclipse.agents.services.protocol.AcpSchema.NewSessionRequest;
import org.eclipse.agents.services.protocol.AcpSchema.NewSessionResponse;
import org.eclipse.agents.services.protocol.AcpSchema.PromptRequest;
import org.eclipse.agents.services.protocol.AcpSchema.PromptResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ReadTextFileResponse;
import org.eclipse.agents.services.protocol.AcpSchema.ReleaseTerminalRequest;
import org.eclipse.agents.services.protocol.AcpSchema.ReleaseTerminalResponse;
import org.eclipse.agents.services.protocol.AcpSchema.RequestPermissionRequest;
import org.eclipse.agents.services.protocol.AcpSchema.RequestPermissionResponse;
import org.eclipse.agents.services.protocol.AcpSchema.SessionNotification;
import org.eclipse.agents.services.protocol.AcpSchema.SetSessionModeRequest;
import org.eclipse.agents.services.protocol.AcpSchema.SetSessionModeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.TerminalOutputRequest;
import org.eclipse.agents.services.protocol.AcpSchema.TerminalOutputResponse;
import org.eclipse.agents.services.protocol.AcpSchema.WaitForTerminalExitRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WaitForTerminalExitResponse;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileRequest;
import org.eclipse.agents.services.protocol.AcpSchema.WriteTextFileResponse;

public abstract class SessionAdapter implements ISessionListener {

	@Override
	public void accept(SessionNotification notification) {
		
	}

	@Override
	public void accept(WriteTextFileRequest request) {
		
	}

	@Override
	public void accept(ReadTextFileRequest request) {
		
	}

	@Override
	public void accept(RequestPermissionRequest request, CompletableFuture<RequestPermissionResponse> pendingResponse) {
		
	}

	@Override
	public void accept(CreateTerminalRequest request) {
		
	}

	@Override
	public void accept(TerminalOutputRequest request) {
		
	}

	@Override
	public void accept(ReleaseTerminalRequest request) {
		
	}

	@Override
	public void accept(WaitForTerminalExitRequest request) {
		
	}

	@Override
	public void accept(KillTerminalCommandRequest request) {
		
	}

	@Override
	public void accept(InitializeResponse response) {
		
	}

	@Override
	public void accept(NewSessionResponse response) {
		
	}

	@Override
	public void accept(SetSessionModeResponse response) {
		
	}

	@Override
	public void accept(PromptResponse response) {
		
	}

	@Override
	public void accept(CancelNotification notification) {
		
	}

	@Override
	public void accept(InitializeRequest request) {
		
	}

	@Override
	public void accept(NewSessionRequest request) {
		
	}

	@Override
	public void accept(SetSessionModeRequest request) {
		
	}

	@Override
	public void accept(PromptRequest request) {
		
	}

	@Override
	public void accept(WriteTextFileResponse response) {
		
	}

	@Override
	public void accept(ReadTextFileResponse response) {
		
	}

	@Override
	public void accept(RequestPermissionResponse response) {
		
	}

	@Override
	public void accept(CreateTerminalResponse response) {
		
	}

	@Override
	public void accept(TerminalOutputResponse response) {
		
	}

	@Override
	public void accept(ReleaseTerminalResponse response) {
		
	}

	@Override
	public void accept(WaitForTerminalExitResponse response) {
		
	}

	@Override
	public void accept(KillTerminalCommandResponse response) {
		
	}

}
