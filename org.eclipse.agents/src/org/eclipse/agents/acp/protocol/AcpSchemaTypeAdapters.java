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

import java.io.IOException;

import org.eclipse.agents.acp.protocol.AcpSchema.AudioBlock;
import org.eclipse.agents.acp.protocol.AcpSchema.BlobResourceContents;
import org.eclipse.agents.acp.protocol.AcpSchema.ContentBlock;
import org.eclipse.agents.acp.protocol.AcpSchema.EmbeddedResourceBlock;
import org.eclipse.agents.acp.protocol.AcpSchema.EmbeddedResourceResource;
import org.eclipse.agents.acp.protocol.AcpSchema.ImageBlock;
import org.eclipse.agents.acp.protocol.AcpSchema.ResourceLinkBlock;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionAgentMessageChunk;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionAgentThoughtChunk;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionAvailableCommandsUpdate;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionModeUpdate;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionPlan;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionToolCall;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionToolCallUpdate;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionUpdate;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionUserMessageChunk;
import org.eclipse.agents.acp.protocol.AcpSchema.TextBlock;
import org.eclipse.agents.acp.protocol.AcpSchema.TextResourceContents;
import org.eclipse.agents.acp.protocol.AcpSchema.ToolCallContent;
import org.eclipse.agents.acp.protocol.AcpSchema.ToolCallContentContent;
import org.eclipse.agents.acp.protocol.AcpSchema.ToolCallContentDiff;
import org.eclipse.agents.acp.protocol.AcpSchema.ToolCallContentTerminal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class AcpSchemaTypeAdapters {

	Gson gson;
	
	public AcpSchemaTypeAdapters() {
		GsonBuilder builder = new GsonBuilder();
		registerTypeAdapters(builder);
		gson = builder.create();
	}
	
	public void registerTypeAdapters(GsonBuilder builder) {
		builder.registerTypeAdapter(SessionUpdate.class, new SessionUpdateAdapter());
		builder.registerTypeAdapter(ContentBlock.class, new ContentBlockAdapter());
		builder.registerTypeAdapter(EmbeddedResourceResource.class, new EmbeddedResourceResourcekAdapter());
		builder.registerTypeAdapter(ToolCallContent.class, new ToolCallContentAdapter());
	}
	
	abstract class AbstractTypeAdapter<T> extends TypeAdapter<T> {
		@Override
		public void write(JsonWriter out, T value) throws IOException {
			out.jsonValue(gson.toJson(value));
		}
	}
	
	class SessionUpdateAdapter extends AbstractTypeAdapter<SessionUpdate> {
		@Override
		public SessionUpdate read(JsonReader in) throws IOException {
			if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }

			
			JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
            String sessionUpdate = jsonObject.get("sessionUpdate").getAsString();
            switch(sessionUpdate) {
            case "user_message_chunk":
            	return gson.fromJson(jsonObject, SessionUserMessageChunk.class);
            case "agent_message_chunk":
            	return gson.fromJson(jsonObject, SessionAgentMessageChunk.class);
            case "agent_thought_chunk":
            	return gson.fromJson(jsonObject, SessionAgentThoughtChunk.class);
            case "tool_call":
            	return gson.fromJson(jsonObject, SessionToolCall.class);
            case "tool_call_update":
            	return gson.fromJson(jsonObject, SessionToolCallUpdate.class);
            case "plan":
            	return gson.fromJson(jsonObject, SessionPlan.class);
            case "available_commands_update":
            	return gson.fromJson(jsonObject, SessionAvailableCommandsUpdate.class);
            case "current_mode_update":
            	return gson.fromJson(jsonObject, SessionModeUpdate.class);
            }

            return null;
		}
		
	};
	
	class ContentBlockAdapter extends AbstractTypeAdapter<ContentBlock> {
		
		@Override
		public ContentBlock read(JsonReader in) throws IOException {
			if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
		
			JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
			String typeString = jsonObject.get("type").getAsString();
			
			switch (typeString) {
			case "text":
				return gson.fromJson(jsonObject, TextBlock.class);
			case "image":
				return gson.fromJson(jsonObject, ImageBlock.class);
			case "audio":
				return gson.fromJson(jsonObject, AudioBlock.class);
			case "resource_link":
				return gson.fromJson(jsonObject, ResourceLinkBlock.class);
			case "resource":
				return gson.fromJson(jsonObject, EmbeddedResourceBlock.class);
			}
			return null;
		}
	};
	
	class EmbeddedResourceResourcekAdapter extends AbstractTypeAdapter<EmbeddedResourceResource> {
		
		@Override
		public EmbeddedResourceResource read(JsonReader in) throws IOException {
			if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
		
			JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
			if (jsonObject.has("blob")) {
				return gson.fromJson(jsonObject, BlobResourceContents.class);
			} else if (jsonObject.has("text")) {
				return gson.fromJson(jsonObject, TextResourceContents.class);
			}

			return null;
		}
	};
	
	class ToolCallContentAdapter extends AbstractTypeAdapter<ToolCallContent> {
		
		@Override
		public ToolCallContent read(JsonReader in) throws IOException {
			if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
		
			JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
			String typeString = jsonObject.get("type").getAsString();
			
			switch (typeString) {
			case "content":
				return gson.fromJson(jsonObject, ToolCallContentContent.class);
			case "diff":
				return gson.fromJson(jsonObject, ToolCallContentDiff.class);
			case "terminal":
				return gson.fromJson(jsonObject, ToolCallContentTerminal.class);

			}
			return null;
		}
	}
	
}
