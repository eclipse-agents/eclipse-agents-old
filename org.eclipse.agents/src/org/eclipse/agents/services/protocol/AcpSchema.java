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
package org.eclipse.agents.services.protocol;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


public class AcpSchema {

// TODO: automate json schema -> java api

// Last Manual Sync
//	commit b277ed22081ca7cd1b210027732dc0cb6efb06ab (HEAD -> main, origin/main, origin/governance, origin/HEAD)
//	Merge: a75bd5b d56ffe5
//	Author: morgankrey <morgan@zed.dev>
//	Date:   Mon Oct 6 13:24:59 2025 -0500
//
//	    Merge pull request #136 from zed-industries/statpak-typo
//	    
//	    Fix Typo


	
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AgentCapabilities(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(defaultValue = "false")
			Boolean loadSession,
			@JsonProperty
			McpCapabilities mcpCapabilities,
			@JsonProperty
			PromptCapabilities promptCapabilities) {

	}
	
	public sealed interface AgentNotification permits SessionNotification, ExtNotification {}

	public sealed interface AgentRequest permits WriteTextFileRequest, 
		ReadTextFileRequest, 
		RequestPermissionRequest, 
		CreateTerminalRequest, 
		TerminalOutputRequest, 
		ReleaseTerminalRequest, 
		WaitForTerminalExitRequest, 
		KillTerminalCommandRequest, 
		ExtMethodRequest {}

	public sealed interface AgentResponse permits InitializeResponse,
		AuthenticateResponse,
		NewSessionResponse,
		LoadSessionResponse,
		SetSessionModeResponse,
		PromptResponse,
		SetSessionModelResponse,
		ExtMethodResponse {}
	

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Annotations(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			Role[] audience,
			String lastModified,
			Double priority) {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AudioContent(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Annotations[] annotations,
			@JsonProperty(required = true)
			String data,
			@JsonProperty(required = true)
			String mimeType) {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AuthMethod(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			String id,
			@JsonProperty
			String name) {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AuthenticateRequest (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			String methodId) implements ClientRequest {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AuthenticateResponse (
			@JsonProperty("_meta")
			Map<String, Object> meta) implements AgentResponse {}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AvailableCommand(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String description,
			@JsonProperty
			AvailableCommandInput input,
			@JsonProperty(required = true)
			String name) {}	

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AvailableCommandInput(
			@JsonProperty 
			String hint) {}
	
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record BlobResourceContents(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String blob,
			@JsonProperty
			String mimeType,
			@JsonProperty(required = true)
			String uri) implements EmbeddedResourceResource {}


	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CancelNotification (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String sessionId) implements ClientNotification {}
	
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ClientCapabilities(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			FileSystemCapability fs,
			@JsonProperty
			boolean terminal) {}
			
	
	public sealed interface ClientNotification permits
	      CancelNotification,
	      ExtNotification {}
	      
	public sealed interface ClientRequest permits
		InitializeRequest,
		AuthenticateRequest,
		NewSessionRequest,
		LoadSessionRequest,
		SetSessionModeRequest,
		PromptRequest,
		SetSessionModelRequest,
		ExtMethodRequest {}
      

	public sealed interface ClientResponse permits
		WriteTextFileResponse,
		ReadTextFileResponse,
		RequestPermissionResponse,
		CreateTerminalResponse,
		TerminalOutputResponse,
		ReleaseTerminalResponse,
		WaitForTerminalExitResponse,
		KillTerminalCommandResponse,
		ExtMethodResponse {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public sealed interface ContentBlock permits
		TextBlock,
		ImageBlock,
		AudioBlock,
		ResourceLinkBlock,
		EmbeddedResourceBlock {}
	
	// ------------ anonymous block types -----------
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record TextBlock (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Annotations annotations,
			@JsonProperty(required = true)
			String text,
			@JsonProperty(required = true, defaultValue = "text")
			String type) implements ContentBlock {}
			

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ImageBlock (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Annotations annotations,
			@JsonProperty(required = true)
			String data,
			@JsonProperty(required = true)
			String mimeType,
			@JsonProperty(required = true, defaultValue = "image")
			String type,
			@JsonProperty
			String uri) implements ContentBlock {}
			
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AudioBlock (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Annotations annotations,
			@JsonProperty(required = true)
			String data,
			@JsonProperty(required = true)
			String mimeType,
			@JsonProperty(required = true, defaultValue = "audio")
			String type) implements ContentBlock {}
			
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ResourceLinkBlock (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Annotations annotations,
			@JsonProperty
			String description,
			@JsonProperty
			String mimeType,
			@JsonProperty(required = true)
			String name,
			@JsonProperty
			Integer size,
			@JsonProperty
			String title,
			@JsonProperty(required = true, defaultValue = "resource_link")
			String type,
			@JsonProperty(required = true)
			String uri) implements ContentBlock {}
			
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record EmbeddedResourceBlock (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Annotations annotations,
			@JsonProperty(required = true)
			EmbeddedResourceResource resource,
			@JsonProperty(required = true, defaultValue = "resource")
			String type) implements ContentBlock {}
			
	// ------------ end anonymous block types -----------
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CreateTerminalRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			String[] args,
			@JsonProperty(required = true)
			String command,
			@JsonProperty
			String cwd,
			@JsonProperty
			EnvVariable env,
			@JsonProperty
			Integer outputByteLimit,
			@JsonProperty(required = true)
			String sessionId) implements AgentRequest {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CreateTerminalResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String  terminalId) implements ClientResponse {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record EmbeddedResource(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Annotations annotations,
			@JsonProperty(required = true)
			EmbeddedResourceResource resource) {}
	
	public sealed interface EmbeddedResourceResource permits
			TextResourceContents,
			BlobResourceContents {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record EnvVariable(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String name,
			@JsonProperty(required = true)
			String value) {}
		

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record FileSystemCapability(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(defaultValue = "false")
	        Boolean readTextFile,
		    @JsonProperty(defaultValue = "false")
			Boolean writeTextFile) {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record HttpHeader(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String name,
			@JsonProperty(required = true)
		    String value) {}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ImageContent(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty("_meta")
			Annotations annotations,
			@JsonProperty(required = true)
			String data,
			@JsonProperty(required = true)
			String mimeType,
			@JsonProperty
			String uri) {}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record InitializeRequest (
			@JsonProperty("_meta")
			Map<String, Object> meta,
	        @JsonProperty
	        ClientCapabilities clientCapabilities,
	        @JsonProperty(required = true)
	        Integer protocolVersion) implements ClientRequest {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record InitializeResponse (
			@JsonProperty("_meta")
			Map<String, Object> meta,
	        @JsonProperty
	        AgentCapabilities agentCapabilities,
	        @JsonProperty
	        AuthMethod[] authMethods,
	        @JsonProperty(required = true)
	        Integer protocolVersion) implements AgentResponse {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record  KillTerminalCommandRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
	        String sessionId,
	        @JsonProperty(required = true)
	        String terminalId) implements AgentRequest {}
	        
			

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record  KillTerminalCommandResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta) implements ClientResponse {}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record  LoadSessionRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
	        String cwd,
	        @JsonProperty(required = true)
	        McpServer[] mcpServers,
	        @JsonProperty(required = true)
	        String sessionId) implements ClientRequest {}
	        
			

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record  LoadSessionResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			SessionModelState modes) implements AgentResponse {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	
	public record McpCapabilities(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(defaultValue = "false")
			Boolean http,
			@JsonProperty(defaultValue = "false")
		    Boolean sse) {}

	public sealed interface McpServer permits 
			HttpTransport,
			SseTransport,
			StdioTransport {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record HttpTransport(
			@JsonProperty(required = true)
			HttpHeader[] headers,
			@JsonProperty(required = true)
			String name,
			@JsonProperty(defaultValue = "http")
			String type,
			@JsonProperty(required = true)
			String url) implements McpServer {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SseTransport(
			@JsonProperty(required = true)
			HttpHeader[] headers,
			@JsonProperty(required = true)
			String name,
			@JsonProperty(defaultValue = "sse")
			String type,
			@JsonProperty(required = true)
			String url) implements McpServer {}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record StdioTransport(
			@JsonProperty(required = true)
			String[] args,
			@JsonProperty(required = true)
			String command,
			@JsonProperty(required = true)
			EnvVariable[] env,
			@JsonProperty(required = true)
			String name)  implements McpServer {}

	public record ModelInfo(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			String description,
			@JsonProperty(required = true)
			String modelId,
			@JsonProperty(required = true)
			String name
			) {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record NewSessionRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String cwd,
			@JsonProperty(required = true)
			McpServer[] mcpServers) implements ClientRequest {};
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record NewSessionResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			SessionModelState models,
			@JsonProperty
			SessionModeState modes,
			@JsonProperty(required = true)
			String sessionId) implements AgentResponse {};

	
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record PermissionOption(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			PermissionOptionKind kind,
			@JsonProperty(required = true)
			String name,
			@JsonProperty(required = true)
			String optionId) {}
	
	enum PermissionOptionKind { allow_once, allow_always, reject_once, reject_always }
    

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Plan(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			PlanEntry entries) {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record PlanEntry(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String content,
			@JsonProperty(required = true)
			PlanEntryPriority priority,
			@JsonProperty(required = true)
			PlanEntryStatus status) {}

	public enum PlanEntryPriority { high, medium, low }
	
	public enum PlanEntryStatus {pending, in_progress, completed};

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record PromptCapabilities(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(defaultValue = "false")
			boolean audio,
			@JsonProperty(defaultValue = "false")
			boolean embeddedContext,
			@JsonProperty(defaultValue = "false")
			boolean image) {}
	
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record PromptRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			ContentBlock[] prompt,
			@JsonProperty(required = true)
			String sessionId) implements ClientRequest {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record PromptResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			StopReason stopReason) implements AgentResponse {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ReadTextFileRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Integer limit,
			@JsonProperty
			Integer line,
			@JsonProperty(required = true)
			String path,
			@JsonProperty(required = true)
			String sessionId) implements AgentRequest {}
	   
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ReadTextFileResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String content) implements ClientResponse {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ReleaseTerminalRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String sessionId,
			@JsonProperty(required = true)
			String terminalId) implements AgentRequest {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ReleaseTerminalResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta) implements ClientResponse {}


	public enum Outcome { cancelled, selected};
     
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record RequestPermissionOutcome(
			@JsonProperty(required = true)
			Outcome outcome,
			@JsonProperty
			String optionId) {}
	

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record RequestPermissionRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			PermissionOption[] options,
			@JsonProperty(required = true)
	        String sessionId,
	        @JsonProperty(required = true)
	        ToolCallUpdate toolCall) implements AgentRequest {}
	 
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record RequestPermissionResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			RequestPermissionOutcome outcome) implements ClientResponse {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ResourceLink(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty 
			Annotations annotations,
			@JsonProperty
			String description,
			@JsonProperty
			String mimeType,
			@JsonProperty(required = true)
			String name,
			@JsonProperty
			Integer size,
		    @JsonProperty
		    String title,
		    @JsonProperty(required = true)
		    String uri) {}

	
	public enum Role { assistant, user };
		

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SessionMode(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty 
			String description,
			@JsonProperty(required = true)
			String id,
			@JsonProperty(required = true)
			String name) {}


	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SessionModeState(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			SessionMode[] availableModes,
			@JsonProperty(required = true)
			String currentModeId) {}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SessionModelState(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			ModelInfo[] availableModels,
			@JsonProperty(required = true)
			String currentModelId) {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SessionNotification(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String sessionId,
			@JsonProperty(required = true)
			SessionUpdate update) implements AgentNotification {}
   


	public sealed interface SessionUpdate permits 
		SessionUserMessageChunk,
		SessionAgentMessageChunk,
		SessionAgentThoughtChunk,
		SessionToolCall,
		SessionToolCallUpdate,
		SessionPlan,
		SessionAvailableCommandsUpdate,
		SessionModeUpdate {}
	
	
	public record SessionUserMessageChunk(
			@JsonProperty(required = true)
			ContentBlock content,
			@JsonProperty(defaultValue = "user_message_chunk")
			String sessionUpdate) implements SessionUpdate {}
	
	public record SessionAgentMessageChunk(
			@JsonProperty(required = true)
			ContentBlock content,
			@JsonProperty(defaultValue = "agent_message_chunk")
			String sessionUpdate) implements SessionUpdate {}
	
	public record SessionAgentThoughtChunk(
			@JsonProperty(required = true)
			ContentBlock content,
			@JsonProperty(defaultValue = "agent_thought_chunk")
			String sessionUpdate) implements SessionUpdate {}
	
	public record SessionToolCall(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			ToolCallContent[] content,
			@JsonProperty
			ToolKind kind,
			@JsonProperty
			ToolCallLocation[] locations,
			@JsonProperty
			Object rawInput,
			@JsonProperty
			Object rawOutput,
			@JsonProperty(defaultValue = "tool_call")
			String sessionUpdate,
			@JsonProperty
			ToolCallStatus status,
			@JsonProperty(required = true)
			String title,
			@JsonProperty(required = true)
			String toolCallId) implements SessionUpdate {}
			
	public record SessionToolCallUpdate(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			ToolCallContent[] content,
			@JsonProperty
			ToolKind kind,
			@JsonProperty
			ToolCallLocation[] locations,
			@JsonProperty
			Object rawInput,
			@JsonProperty
			Object rawOutput,
			@JsonProperty(defaultValue = "tool_call_update")
			String sessionUpdate,
			@JsonProperty
			ToolCallStatus status,
			@JsonProperty(required = true)
			String toolCallId) implements SessionUpdate {}
	
	public record SessionPlan(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			PlanEntry[] entries,
			@JsonProperty(defaultValue = "plan")
			String sessionUpdate) implements SessionUpdate {}
	
	public record SessionAvailableCommandsUpdate(
			@JsonProperty(required = true)
			AvailableCommand[] commands,
			@JsonProperty(defaultValue = "available_commands_update")
			String sessionUpdate) implements SessionUpdate {}
	
	public record SessionModeUpdate(
			@JsonProperty(required = true)
			String currentModeId,
			@JsonProperty(defaultValue = "current_mode_update")
			String sessionUpdate) implements SessionUpdate {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SetSessionModeRequest (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			SessionMode modeId,
			@JsonProperty(required = true)
			String sessionId) implements ClientRequest {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)	
	public record SetSessionModeResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta) implements AgentResponse {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SetSessionModelRequest (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			SessionMode modelId,
			@JsonProperty(required = true)
			String sessionId) implements ClientRequest {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)	
	public record SetSessionModelResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta) implements AgentResponse {}
	 
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public enum StopReason { end_turn, max_tokens, max_turn_requests, refusal, cancelled }


	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record TerminalExitStatus(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty 
			Integer exitCode,
			@JsonProperty
			String signal) {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record TerminalOutputRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String sessionId,
			@JsonProperty(required = true)
			String terminalId) implements AgentRequest {}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record TerminalOutputResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			TerminalExitStatus exitStatus,
			@JsonProperty(required = true)
			String output,
			@JsonProperty(required = true)
			Boolean truncated) implements ClientResponse {}

  

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record TextContent(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Annotations annotations,
			@JsonProperty(required = true)
			String text) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	
	public record TextResourceContents(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty 
			String mimeType,
			@JsonProperty(required = true)
			String text,
			@JsonProperty(required = true)
			String uri) implements EmbeddedResourceResource {}
		

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ToolCall(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty 
			ToolCallContent[] content,
			@JsonProperty 
			ToolKind kind,
			@JsonProperty 
			ToolCallLocation[] locations,
			@JsonProperty 
			Object rawInput,
			@JsonProperty 
			Object rawOutput,
			@JsonProperty 
			ToolCallStatus status,
			@JsonProperty(required = true)
			String title,
			@JsonProperty(required = true)
			String toolCallId) {}


	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public sealed interface ToolCallContent permits
		ToolCallContentContent,
		ToolCallContentDiff,
		ToolCallContentTerminal {}

	public record ToolCallContentContent(
			@JsonProperty(required = true)
			ContentBlock content,
			@JsonProperty(defaultValue = "content")
			String type) implements ToolCallContent {}
	
	
	public record ToolCallContentDiff(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String newText,
			@JsonProperty
			String oldText,
			@JsonProperty(required = true)
			String path,
			@JsonProperty(defaultValue = "diff")
			String type) implements ToolCallContent {}
	
	public record ToolCallContentTerminal(
			@JsonProperty(required = true)
			String terminalId,
			@JsonProperty(defaultValue = "terminal")
			String type) implements ToolCallContent {}



	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ToolCallLocation(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty 
			Integer line,
			@JsonProperty(required = true)
			String path) {}

	public enum ToolCallStatus{ pending, in_progress, completed, failed }
	
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ToolCallUpdate(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty 
			ToolCallContent[] content,
			@JsonProperty 
			ToolKind kind,
			@JsonProperty 
			ToolCallLocation[] locations,
			@JsonProperty 
			Object rawInput,
			@JsonProperty 
			Object rawOutput,
			@JsonProperty 
			ToolCallStatus status,
			@JsonProperty
			String title,
			@JsonProperty(required = true)
			String toolCallId) {}
			


	public enum ToolKind {
		  read, edit, delete, move, search, execute, think, fetch, switch_mode, other
	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record WaitForTerminalExitRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String sessionId,
			@JsonProperty(required = true)
			String terminalId) implements AgentRequest {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record WaitForTerminalExitResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			Integer exitCode,
			String signal) implements ClientResponse {}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record WriteTextFileRequest(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String content,
			@JsonProperty(required = true)
			String path,
			@JsonProperty(required = true)
			String sessionId) implements AgentRequest {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record WriteTextFileResponse(
			@JsonProperty("_meta")
			Map<String, Object> meta) implements ClientResponse {}
	

	public record ExtNotification() implements AgentNotification, ClientNotification{};
	public record ExtMethodRequest() implements AgentRequest, ClientRequest {};
	public record ExtMethodResponse() implements AgentResponse, ClientResponse{};

}
