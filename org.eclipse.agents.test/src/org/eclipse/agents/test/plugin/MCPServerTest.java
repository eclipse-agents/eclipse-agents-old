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
 
package org.eclipse.agents.test.plugin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.agents.IFactoryProvider;
import org.eclipse.agents.internal.MCPServer;
import org.eclipse.agents.platform.FactoryProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Assert;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.CompleteReference;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.InitializeResult;
import io.modelcontextprotocol.spec.McpSchema.ListResourceTemplatesResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceReference;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class MCPServerTest {

	MCPServer server;
	McpSyncClient client;

	@Test
	@Order(1)
	public void setup() throws CoreException, IOException {
		server = new MCPServer("junit", "junit", 3028, new IFactoryProvider[] {
				new FactoryProvider()
		});
		
		// Create a sync client with custom configuration
		HttpClientSseClientTransport transport = HttpClientSseClientTransport
				.builder("http://localhost:3028/sse")
				.jsonMapper(new JacksonMcpJsonMapper(new ObjectMapper()))
				.build();

		client = McpClient.sync(transport)
		    .requestTimeout(Duration.ofSeconds(10))
		    .capabilities(ClientCapabilities.builder()
//		        .roots(true)      // Enable roots capability
//		        .sampling()       // Enable sampling capability
//		        .elicitation()
		        .build())
//		    .sampling(request -> new CreateMessageResult(response))
//		    .elicitation(null)
		    .build();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
				
		final IProject project = workspace.getRoot().getProject("Project");
		try {
			IWorkspaceRunnable create = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					project.create(null, null);
					project.open(null);
				}
			};
			workspace.run(create, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		 IFile file = project.getFile("HelloWorld.java");
		
	    if (!file.exists()) {
	    	String content = "public class HelloWorld {\n" +
					"    public static void main(String[] args) {\n"  +
					"        System.out.println(\"Hello, World!\");\n"  +
					"    }\n" +
					"}\n";
	    	byte[] bytes = content.getBytes();
	    	
	    	ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
	        file.create(stream, true, null);
	        stream.close();
	        
	        project.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
	    }
		
		
		
		
		
		System.out.println(file.exists());
		
		
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IWorkbenchPart part = page.getActivePart();
					part.dispose();
		
					IEditorPart editor = IDE.openEditor(page, file, true);
					if (editor instanceof ITextEditor) {
						ITextEditor textEditor = (ITextEditor) editor;
						textEditor.selectAndReveal(7, 5);
						page.activate(textEditor);
					}
					Map attr = new HashMap();
					attr.put(IMarker.MESSAGE, "There is a problem");
					attr.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		
					attr.put(IMarker.CHAR_START, 7);
					attr.put(IMarker.CHAR_END, 12);
					attr.put(IMarker.LINE_NUMBER, 1);
		
					file.createMarker(IMarker.PROBLEM, attr);
		
					page.getActivePart();
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}

	@Test
	@Order(2)
	public void startServer() {
		server.start();

	}

	@Test
	@Order(3)
	public void initClient() {
		InitializeResult result = client.initialize();

		System.out.println(result.toString());
		System.out.println();

	}

	@Test
	@Order(4)
	public void listTemplates() {
		ListResourceTemplatesResult templates = client.listResourceTemplates();

		System.out.println(templates);
		System.out.println();

	}

	@Test
	@Order(5)
	public void listTools() {
		ListToolsResult tools = client.listTools();

		System.out.println(tools);
		System.out.println();
	}

	CallToolResult[] toolResult = new CallToolResult[3];

	@Test
	@Order(6)
	public void getCurrentSelection() throws JsonProcessingException {

		Thread t = new Thread() {
			public void run() {
				toolResult[0] = client.callTool(new CallToolRequest("currentSelection", Map.of()));
			}
		};
		
		t.start();
		
		while(t.isAlive()) {
			Display.getDefault().readAndDispatch();
		}
		

		Content content = toolResult[0].content().get(0);
		String result = ((TextContent) content).text();
		System.out.println(result);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode received = mapper.readTree(result);
		JsonNode expected = mapper.readTree("""
{
   "editor":{
      "name":"HelloWorld.java",
      "editor":{
         "type":"resource_link",
         "name":"HelloWorld.java",
         "uri":"eclipse://editor/HelloWorld.java",
         "description":"Content of an Eclipse IDE Editor",
         "mimeType":"text/plain",
         "size":124
      },
      "file":{
         "type":"resource_link",
         "name":"HelloWorld.java",
         "uri":"file://workspace/Project%2FHelloWorld.java",
         "description":"Eclipse workspace file",
         "mimeType":"text/plain",
         "size":124
      },
      "isActive":true,
      "isDirty":false
   },
   "textSelection":{
      "offset":7,
      "length":5,
      "startLine":0,
      "endLine":0,
      "text":"class"
   }
}""");
		Assert.assertEquals(expected, received);

	}

	@Test
	@Order(7)
	public void listEditors() throws JsonMappingException, JsonProcessingException {
		Thread t = new Thread() {
			public void run() {
				toolResult[1] = client.callTool(new CallToolRequest("listEditors", Map.of()));
			}
		};
		
		t.start();
		
		while(t.isAlive()) {
			Display.getDefault().readAndDispatch();
		}
		

		Content content = toolResult[1].content().get(0);
		String result = ((TextContent) content).text();

		System.out.println(result + "\n\n");

		ObjectMapper mapper = new ObjectMapper();

	
		JsonNode received = mapper.readTree(result);
		JsonNode expected = mapper.readTree("""
{
   "editors":[
     {
       "name":"HelloWorld.java",
       "editor":{
         "type":"resource_link",
         "name":"HelloWorld.java",
         "uri":"eclipse://editor/HelloWorld.java",
         "description":"Content of an Eclipse IDE Editor",
         "mimeType":"text/plain",
         "size":124
       },
       "file":{
         "type":"resource_link",
         "name":"HelloWorld.java",
         "uri":"file://workspace/Project%2FHelloWorld.java",
         "description":"Eclipse workspace file",
         "mimeType":"text/plain",
         "size":124
       },
       "isActive":true,
       "isDirty":false
     }
   ]
}""");

			Assert.assertEquals(expected, received);
		

	}

	@Test
	@Order(8)
	public void listConsoles() throws JsonMappingException, JsonProcessingException {

		toolResult[2] = client.callTool(new CallToolRequest("listConsoles", Map.of()));

		Content content = toolResult[2].content().get(0);
		String result = ((TextContent) content).text();
		System.out.println(result);

		JsonNode node = new ObjectMapper().readValue(result, JsonNode.class);

		JsonNode consoles = node.get("consoles");
		JsonNode console = consoles.get(0);

		testEquals("console.name", console.get("name").asText(), "z/OS");
		testEquals("console.type", console.get("type").asText(), "zosConsole");
	}

	@Test
	@Order(9)
	public void completeProjectTemplate() {

		boolean found = false;
		ListResourceTemplatesResult result = client.listResourceTemplates();
		for (ResourceTemplate rt : result.resourceTemplates()) {
			if (rt.name().equals("Eclipse Workspace File")) {
				CompleteReference ref = new ResourceReference(rt.uriTemplate());
				CompleteRequest.CompleteArgument arg = new CompleteRequest.CompleteArgument("project", "");
				CompleteRequest.CompleteContext context = new CompleteRequest.CompleteContext(
						new HashMap<String, String>());
				CompleteRequest cr = new CompleteRequest(ref, arg, context);
				CompleteResult res = client.completeCompletion(cr);

				CompleteResult.CompleteCompletion com = res.completion();

				Assert.assertTrue("Completions contains 'Project'", com.values().contains("Project"));
				found = true;

			}
		}
		Assert.assertTrue("Found template for \"Eclipse Workspace File\"", found);

	}

	@Test
	@Order(10)
	public void readTemplateContent() {

		ReadResourceResult result = client
				.readResource(new ReadResourceRequest("file://workspace/Project/HelloWorld.java"));
		System.err.println(result);

	}

	@Test
	@Order(11)
	public void disconnectClient() {
		boolean close = client.closeGracefully();
		System.out.println(close);
	}

	@Test
	@Order(12)
	public void stopServer() {
		server.stop();
	}

	public static void testEquals(String message, String left, String right) {
		System.out.println(message + ":: " + left + " == " + right);
		Assert.assertEquals(message, left, right);
	}

	public static void testArrayEquals(String message, String[] left, String[] right) {
		System.out.println(message + ":: " + Arrays.toString(left) + " == " + Arrays.toString(right));
		Assert.assertArrayEquals(message, left, right);
	}

}
