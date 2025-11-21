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
 
package org.eclipse.agents.test;

import org.eclipse.agents.acp.protocol.AcpSchema.BlobResourceContents;
import org.eclipse.agents.acp.protocol.AcpSchema.ContentBlock;
import org.eclipse.agents.acp.protocol.AcpSchema.EmbeddedResourceBlock;
import org.eclipse.agents.acp.protocol.AcpSchema.PromptRequest;
import org.eclipse.agents.acp.protocol.AcpSchema.ResourceLinkBlock;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionAgentMessageChunk;
import org.eclipse.agents.acp.protocol.AcpSchema.SessionAgentThoughtChunk;
import org.eclipse.agents.acp.protocol.AcpSchema.TextBlock;
import org.eclipse.agents.acp.protocol.AcpSchema.TextResourceContents;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AcpMessageGenerator {

	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		
		System.out.println(mapper.writeValueAsString(new PromptRequest(
				null,
				new ContentBlock[] {
						
					new ResourceLinkBlock(
							null, null, "description", "text/html", "sample.html", 123456, 
							"sample.html", "resource_link", "file:///no/where/dot/sample"),
					new EmbeddedResourceBlock(null, null, new TextResourceContents(
							null, "text/html", "<html><body></body></html>", "file:///no/where/dot/snippet.html"
							), "resource"),
							
					new TextBlock(
							null, null, "Using sample.html and the snippet of selected xml code, compare them to ", "text"),
					new ResourceLinkBlock(
							null, null, "description", "text/xml", "mystery.xml", 654321, 
							"mystery.xml", "resource_link", "file:///no/where/dot/mystery"),
					new TextBlock(
							null, null, "and see if the comparison matches ", "text"),
					new EmbeddedResourceBlock(null, null, new BlobResourceContents(
							null, "text/xml", "<xml><body></body></xml>", "file:///no/where/dot/snippet.xml"), "resource"),
					new TextBlock(
							null, null, ". if not ask me for additional details", "text"),
				},
				"session1")));

		System.out.println();
		
		System.out.println(mapper.writeValueAsString(new SessionAgentThoughtChunk(
				new TextBlock(
							null, null, "**Im Thinking About**\n"
									+ "- one thing\n"
									+ "- another thing", "text"),
				"session1")));
		
		System.out.println();
		
		System.out.println(mapper.writeValueAsString(new SessionAgentThoughtChunk(
				new TextBlock(
							null, null, "**Im Also Thinking About**\n"
									+ "- one thing\n"
									+ "- another thing", "text"),
				"session1")));
		
		System.out.println();
		
		

			
		System.out.println(mapper.writeValueAsString(new SessionAgentMessageChunk(
				new TextBlock(null, null, "Here is what i came up with:\n"
									+ "\\`\\`\\`json\n"
									+ " \"a\": {\n"
									+ "\"B\": \"C\"`", "text"),
				"session1")));
		
		System.out.println();
		
		System.out.println(mapper.writeValueAsString(new SessionAgentMessageChunk(
				new TextBlock(
							null, null, "}}\n"
									+ "			\\`\\`\\`\n"
									+ "			Anything else?", "text"),
				"session1")));
		
		System.out.println(mapper.writeValueAsString(new SessionAgentMessageChunk(
				new TextBlock(
				null, null, "Using sample.html and the snippet of selected xml code, compare them to ", "text"),
				"session1")));
		
		System.out.println();
		
		System.out.println(mapper.writeValueAsString(new SessionAgentMessageChunk(
				new ResourceLinkBlock(
				null, null, "description", "text/xml", "mystery.xml", 654321, 
				"mystery.xml", "resource_link", "file:///no/where/dot/mystery"),
				"session1")));
		
		System.out.println();
		
		System.out.println(mapper.writeValueAsString(new SessionAgentMessageChunk(
		new TextBlock(
				null, null, "and see if the comparison matches ", "text"),
				"session1")));
		
		System.out.println();
		
		System.out.println(mapper.writeValueAsString(new SessionAgentMessageChunk(
		new EmbeddedResourceBlock(null, null, new BlobResourceContents(
				null, "text/xml", "<xml><body></body></xml>", "file:///no/where/dot/snippet.xml"), "resource"),
				"session1")));
		
		System.out.println();

		System.out.println(mapper.writeValueAsString(new SessionAgentMessageChunk(
			new TextBlock(
				null, null, ". if not ask me for additional details", "text"),
				"session1")));
	}

}
