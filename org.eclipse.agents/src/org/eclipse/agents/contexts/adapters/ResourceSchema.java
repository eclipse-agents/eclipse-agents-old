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
package org.eclipse.agents.contexts.adapters;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

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
}
