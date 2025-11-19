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
package org.eclipse.agents.contexts.platform;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.agents.contexts.platform.resource.EditorAdapter;
import org.eclipse.agents.contexts.platform.resource.WorkspaceResourceAdapter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.springaicommunity.mcp.annotation.McpComplete;
import org.springaicommunity.mcp.annotation.McpResource;

import io.modelcontextprotocol.spec.McpSchema.CompleteRequest;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest.CompleteArgument;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest.CompleteContext;

public class ResourceTemplates {

	public ResourceTemplates() {
		
	}

    @McpResource(
    		uri = "eclipse://editor/{name}", 
            name = "Eclipse IDE Text Editor", 
            description = "Content of an Eclipse Text Editor")
	public String getEditorContent(String name) {
    	
    	EditorAdapter adapter = new EditorAdapter("eclipse://editor/" + name);
		return adapter.toContent();

	}
    
    @McpComplete(uri="eclipse://editor/{name}")
	public List<String> completeName(CompleteRequest request) {
    	
    	CompleteArgument argument = request.argument();
    	CompleteContext context = request.context();

    	List<String> result = new ArrayList<String>();
		for (IWorkbenchWindow ww: PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page: ww.getPages()) {
				for (IEditorReference reference: page.getEditorReferences()) {
					result.add(reference.getName());
				}
			}
		}
    	return result;
	}
    
    @McpResource (
    		uri = "file://workspace/{project}/{projectRelativePath}",
    		name = "Eclipse Workspace File",
    		description = "Content of an file in an Eclipse workspace")
    public String getWorkspaceFileContent(String project, String projectRelativePath) {
    	
    	// condense from 2 variables to 1 variable
    	String uri = "file://workspace/" + 
    			URLEncoder.encode(project + "/" + projectRelativePath, StandardCharsets.UTF_8);
    	WorkspaceResourceAdapter adapter = new WorkspaceResourceAdapter(uri);
    	return adapter.toContent();
	}
    
    @McpComplete(uri = "file://workspace/{project}/{projectRelativePath}")
   	public List<String> completeRelativePath(CompleteRequest request) {
    	
    	CompleteArgument argument = request.argument();
    	CompleteContext context = request.context();

    	List<String> result = new ArrayList<String>();
		if (argument.name().equals("project")) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			for (IProject project: workspace.getRoot().getProjects()) {
				if (project.getName().toUpperCase().contains(argument.value().toUpperCase())) {
					result.add(project.getName());
				}
			}
		} else{
			
			String projectName = context.arguments().get("project");
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProject project = workspace.getRoot().getProject(projectName);
			if (project != null && argument.value() != null) {
				List<IFile> files = new ArrayList<IFile>();
				IResourceVisitor  visitor = new IResourceVisitor() {
					@Override
					public boolean visit(IResource resource) throws CoreException {
						if (resource instanceof IFile && resource.getName().toUpperCase().contains(argument.value().toUpperCase())) {							
							IFile file = (IFile)resource;
							if (file.isAccessible() && !file.isHidden() && file.exists() && !file.isPhantom()) {
								IContentDescription desc = file.getContentDescription();
								if (desc != null) {
									IContentType contentType = desc.getContentType();
									if (contentType != null && contentType.isKindOf(
											Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT))) {
										files.add(file);
									}
								} else if (file.getName().startsWith(".")) {
									files.add(file);
								}
								
							}
						}
						if (resource instanceof IContainer && files.size() < 50) {
							return true; //!container.isHidden() && !container.isPhantom();
						}
						return false;
					}
				};
				try {
					project.accept(visitor, IResource.DEPTH_INFINITE, false);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
				for (IFile file: files) {
					result.add(file.getProjectRelativePath().toPortableString());
				}
			}
			
		}
		return result;
   	}

}
