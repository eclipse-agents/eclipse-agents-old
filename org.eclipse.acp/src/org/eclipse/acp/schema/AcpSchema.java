package org.eclipse.acp.schema;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.modelcontextprotocol.spec.McpSchema.InitializeRequest;

public class AcpSchema {

	//https://github.com/zed-industries/agent-client-protocol/blob/1cf7e2c5b42ae6c238bab266f11a6456d85e8a58/schema/schema.json
	
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
			String uri) {}


	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CancelNotification (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty(required = true)
			String sessionId) { }
	
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ClientCapabilities(
			@JsonProperty("_meta")
			Map<String, Object> meta,
			FileSystemCapability fs,
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
		ExtMethodRequest {}
      

	public sealed interface ClientResponse permits
		WriteTextFileResponse,
		ReadTextFileResponse,
		RequestPermissionResponse,
		TerminalOutputResponse,
		ReleaseTerminalResponse,
		WaitForTerminalExitResponse,
		KillTerminalResponse,
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
	public record TextBlock (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Annotations annotations,
			@JsonProperty(required = true)
			String text,
			@JsonProperty(required = true, defaultValue = "text")
			String type) implements ContentBlock {}
			

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
			
	public record AudioBlock (
			@JsonProperty("_meta")
			Map<String, Object> meta,
			@JsonProperty
			Annotations annotations,
			@JsonProperty(required = true)
			String data,
			@JsonProperty(required = true)
			String mimeType,
			@JsonProperty(required = true, defaultValue = "image")
			String type) implements ContentBlock {}
			
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
	
	//TODO stopped here
	"CreateTerminalRequest": {

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record EmbeddedResource(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record EmbeddedResourceResource(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record EnvVariable(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record FileSystemCapability(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record HttpHeader(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record ImageContent(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record McpCapabilities(
			@JsonProperty 
	
//"default": {
//            "http": false,
//            "sse": false
//          },
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record McpServer(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record PermissionOption(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record PermissionOptionId(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record PermissionOptionKind(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record Plan(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record PlanEntry(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record PlanEntryPriority(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record PlanEntryStatus(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record PromptCapabilities(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record ProtocolVersion(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record RequestPermissionOutcome(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record ResourceLink(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record Role(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record SessionId(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record SessionMode(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record SessionModeId(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record SessionModeState(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record SessionUpdate(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record SetSessionModeResponse(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record StopReason(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record TerminalExitStatus(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record TextContent(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record TextResourceContents(
			@JsonProperty 
	
			String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record ToolCall(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record ToolCallContent(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record ToolCallId(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record ToolCallLocation(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record ToolCallStatus(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record ToolCallUpdate(
			@JsonProperty 
	
			
	String aaa) {

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record ToolKind(
			@JsonProperty 
	
			
	String aaa) {

	}

}
