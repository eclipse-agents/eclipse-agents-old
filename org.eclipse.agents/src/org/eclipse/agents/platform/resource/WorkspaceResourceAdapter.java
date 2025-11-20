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
package org.eclipse.agents.platform.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.agents.MCPException;
import org.eclipse.agents.platform.resource.ResourceSchema.Children;
import org.eclipse.agents.platform.resource.ResourceSchema.DEPTH;
import org.eclipse.agents.platform.resource.ResourceSchema.File;
import org.eclipse.agents.resource.IResourceHierarchy;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ResourceLink;
import io.modelcontextprotocol.util.DefaultMcpUriTemplateManager;

/**
 * support for resource template: file://workspace/{relativePath}
 */
public class WorkspaceResourceAdapter implements IResourceHierarchy<IResource, File> {
	
	final String relativeTemplate = "file://workspace/{relativePath}";
	final String relativePrefix = relativeTemplate.substring(0, relativeTemplate.indexOf("{"));
	final String absolutePrefix = "file:/";

	IResource resource;
	
	public WorkspaceResourceAdapter() {}

	public WorkspaceResourceAdapter(IResource resource) {
		this.resource = resource;
	}
	
	@Override
	public boolean matches(String uri) {
		if (IResourceHierarchy.super.matches(uri)) {
			// uri has escaped slashes
			return true;
		} else if (uri.startsWith(relativePrefix)) {
			// uri has unescaped slashes
			return true;
		} else if (uri.startsWith(absolutePrefix)) {
			// uri has unescaped slashes
			return true;
		}
		return false;
	}

	public WorkspaceResourceAdapter(String uri) {
		
		DefaultMcpUriTemplateManager relative = new DefaultMcpUriTemplateManager(relativeTemplate);
		String relativePath = null, absolutePath = null;
		boolean isRelative = false;
		
		if (relative.matches(uri)) {
			Map<String, String> variables = relative.extractVariableValues(uri);
			relativePath = variables.get("relativePath");
			relativePath = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);
			isRelative = true;
		} else if (uri.startsWith(relativePrefix)) {
			relativePath = uri.substring(relativePrefix.length());
			isRelative = true;
		} else if (uri.equals("file://workspace")) {
				isRelative = true;
		} else if (uri.startsWith(absolutePrefix)) {
			absolutePath = uri;
		} 
		
		if (isRelative) {
			if (relativePath == null || relativePath.isBlank()) {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			} else {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				resource = workspace.getRoot().findMember(relativePath);
			}
		} else if (absolutePath != null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			
			IFile[] files = workspace.getRoot().findFilesForLocationURI(URI.create(absolutePath));
			if (files != null && files.length > 0 && files[0] != null) {
				resource = files[0];
			} else {
				IContainer[] containers = workspace.getRoot().findContainersForLocationURI(URI.create(absolutePath));
				if (containers != null && containers.length > 0 && containers[0] != null) {
					resource = containers[0];
				}
			}
		} else {
			throw new MCPException("Could not resolve file uri: " + uri);
		}
		
		if (resource == null) {
			throw new MCPException("Could not find matching resource: " + uri);
		}
	}

	@Override
	public String[] getTemplates() {
		return new String[] { 
			relativeTemplate
		};
	}
	
	@Override
	public WorkspaceResourceAdapter fromUri(String uri) {
		return new WorkspaceResourceAdapter(uri);
	}

	@Override
	public WorkspaceResourceAdapter fromModel(IResource console) {
		return new WorkspaceResourceAdapter(console);
	}

	@Override
	public Children<File> getChildren(DEPTH depth) {
		
		List<File> children = new ArrayList<File>();
		if (depth == null) {
			depth = DEPTH.CHILDREN;
		}
		
		if (resource instanceof IContainer) {
			try {
				for (IResource child: ((IContainer)resource).members()) {
					child.accept(new IResourceVisitor() {
						@Override
						public boolean visit(IResource child) throws CoreException {
							if (child != resource) {
								children.add(new WorkspaceResourceAdapter(child).toJson());
							}
							return true;
						}
					}, depth.value(), false);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		return new Children<File>(children.toArray(File[]::new), depth);
	}

	@Override
	public IResource getModel() {
		return resource;
	}

	@Override
	public File toJson() {
		return new File(resource.getName(), resource instanceof IContainer, toResourceLink());
	}

	@Override
	public ResourceLink toResourceLink() {
		McpSchema.ResourceLink.Builder builder =  McpSchema.ResourceLink.builder()
				.uri(toUri())
				.name(resource.getName());
				
		if (resource instanceof IFile) {
			builder.description("Eclipse workspace file");
			builder.mimeType("text/plain");

			try {
				IFileStore store = EFS.getStore(resource.getLocationURI());
				IFileInfo info = store.fetchInfo();
				builder.size(info.getLength());
				
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else if (resource instanceof IProject) {
			builder.description("Eclipse workspace project");
		} else if (resource instanceof IWorkspaceRoot) {
			builder.description("Eclipse workspace root");
		} else if (resource instanceof IFolder) {
			builder.description("Eclipse workspace folder");
		}

		return builder.build();
		
	}

	@Override
	public String toUri() {
		return resource.getLocationURI().toString();
	}

	@Override
	public String toContent() {
		
		String content = null;
		if (resource instanceof IFile) {
			try {
				InputStreamReader reader = new InputStreamReader(((IFile)resource).getContents());
				BufferedReader breader = new BufferedReader(reader);
				content = breader.lines().collect(Collectors.joining("\n")); //$NON-NLS-1$
				breader.close();
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return content;
	}
}
