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
package org.eclipse.agents.contexts.platform.resource;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.modelcontextprotocol.spec.McpSchema;

public class ResourceSchema {

	public enum DEPTH { 
		CHILDREN(0), 
		GRANDCHILDREN(1), 
		INFINITE(2);
		
		int value;
		private DEPTH(int value) {
			this.value = value;
		}
		
		public int value() {
			return value;
		}
	};
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("Element of an hierarchical file system")
	public record File (
		
		@JsonProperty
		String name,
		
		@JsonPropertyDescription("Folders may have children")
		@JsonProperty
		boolean isFolder,
		
		@JsonProperty
		McpSchema.ResourceLink uri) {
	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Children<T> (

		@JsonProperty
		T[] children,
	
		@JsonPropertyDescription("The actual depth searched, may differ from input")
		@JsonProperty
		DEPTH depthSearched) {
		
	}

	public enum SEVERITY { ERROR, INFO, WARNING };
	public enum PRIORITY { HIGH, LOW, NORMAL };
	public enum TYPE { Bookmark("Bookmark"), 
		Problem("Problem"), 
		Task("Task"), 
		Text("Text");
		
		String label;
		private TYPE(String label) {
			this.label = label;
		}
		
		public String label() {
			return label;
		}
	};

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("An Eclipse IDE console")
	public record Console (
		@JsonProperty("name") String name,
		@JsonProperty("type") String type,
		@JsonProperty("uri") Object uri) {
	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("List of Eclipse IDE consoles")
	public record Consoles (
		@JsonProperty
		Console[] consoles) {
	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("An Eclipse IDE text editor")
	public record Editor (

		@JsonProperty()
		String name,
		
		@JsonProperty(required = false)
		@JsonPropertyDescription("Editor resource, if it contains text")
		McpSchema.ResourceLink editor,
		
		@JsonProperty(required = false)
		@JsonPropertyDescription("The file being edited, if applicable")
		McpSchema.ResourceLink file,
		
		@JsonProperty
		@JsonPropertyDescription("Only one editor is active at a time")
		boolean isActive,
		
		@JsonProperty
		@JsonPropertyDescription("If editor contains unsaved changes")
		boolean isDirty) {
	}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("List of Eclipse IDE text editors")
	public record Editors (
		@JsonProperty
		Editor[] editors) {
	}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("")
	public record TextEditorSelection (

		@JsonPropertyDescription("Selected Text Editor")
		@JsonProperty
		Editor editor,
		
		@JsonProperty
		@JsonPropertyDescription("Selected text")
		TextSelection textSelection){}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("Range of characters selected in a text editor")
	public record TextSelection (

		@JsonProperty
		@JsonPropertyDescription("position of the first selected character")
		int offset,
		
		@JsonProperty
		@JsonPropertyDescription("length of the text selection")
		int length,
		
		@JsonProperty
		@JsonPropertyDescription("line of the offset of the selected text")
		int startLine,
		
		@JsonProperty
		@JsonPropertyDescription("line of the last character of the selected text")
		int endLine,
		
		@JsonProperty
		@JsonPropertyDescription("selected text")
		String text) {
	}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("A single text replacement")
	public record TextReplacement (

		@JsonProperty
		@JsonPropertyDescription("the text to insert into editor")
		String text,

		@JsonProperty
		@JsonPropertyDescription("the character offset to insert the text")
		int offset,
		
		@JsonProperty
		@JsonPropertyDescription("the length of text after the offset to remove")
		int length) {
	}

	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonClassDescription("A note associated with an an Eclipse IDE file or editor")
	public record Marker (

		TYPE type,
		
		@JsonProperty(required = false)
		String message,
		
		@JsonProperty(required = false)
		@JsonPropertyDescription("An integer value indicating where a text marker starts. This attribute is zero-relative and inclusive.")
		int charStart,
		
		@JsonProperty(required = false)
		@JsonPropertyDescription("An integer value indicating where a text marker ends. This attribute is zero-relative and exclusive.")
		int charEnd,
		
		@JsonProperty(required = false)
		@JsonPropertyDescription("An integer value indicating the line number for a text marker. This attribute is 1-relative")
		int lineNumber,
		
		@JsonProperty(required = false)
		@JsonPropertyDescription("Indicates if a task is complete")	
		boolean done,
		
		@JsonProperty(required = false)
		@JsonPropertyDescription("The location is a human-readable (localized) string which can be used to distinguish between markers on a resource")	
		String location,
		
		@JsonProperty
		@JsonPropertyDescription("The associated file or editor")
		McpSchema.ResourceLink resource_link,
		
		@JsonProperty
		long id,
		
		@JsonProperty
		long creationTime,
		
		@JsonProperty(required = false)
		@JsonPropertyDescription("error, warning or info severity")
		SEVERITY severity,
		
		@JsonPropertyDescription("high, normal or low priority")
		PRIORITY priority) {
	}
	

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Problems (
		@JsonProperty(value = "problems")
		Marker[] problems) {}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Tasks (
			@JsonProperty(value = "tasks")
			Marker[] tasks) {}
	
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ElicitationChoice (
			@JsonProperty String type,
			@JsonProperty String title,
			@JsonProperty String description,
			@JsonProperty String[] enums,
			@JsonProperty String[] enumNames,
			@JsonProperty(value = "default") String _default) {}
	
	public static Map<String, Object> createEliciationRequestSchema(Map<String, Object> propertyValues, String[] required) {
		Map<String, Object> requestedSchema = new HashMap<String, Object>();
		requestedSchema.put("type",  "object");
		requestedSchema.put("required", required);
	
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode properties = mapper.createObjectNode();
		requestedSchema.put("properties", properties);
		
		for (String key: propertyValues.keySet()) {
			properties.putPOJO(key, propertyValues.get(key));
		}
		return requestedSchema;
	}
}
