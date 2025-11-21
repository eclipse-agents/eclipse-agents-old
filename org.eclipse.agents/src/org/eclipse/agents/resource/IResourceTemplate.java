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
package org.eclipse.agents.resource;

import io.modelcontextprotocol.spec.McpSchema.ResourceLink;
import io.modelcontextprotocol.util.DefaultMcpUriTemplateManager;

public interface IResourceTemplate<T, U> extends IResourceAdapter<U> {

	public IResourceTemplate<T, U> fromUri(String uri);
	
	public IResourceTemplate<T, U> fromModel(T object);
	
	public String[] getTemplates();
	
	public T getModel();
	
	public ResourceLink toResourceLink();
	
	public String toUri();
	
	public String toContent();
	
	public default boolean matches(String uri) {
		for (String template: getTemplates()) {
			if (new DefaultMcpUriTemplateManager(template).matches(uri)) {
				return true;
			}
		}
		return false;
	}
}
