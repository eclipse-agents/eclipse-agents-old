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
package org.eclipse.mcp;

import org.eclipse.mcp.resource.IResourceTemplate;

/**
 * Convenience for creating multiple factories programmatically
 */
public interface IFactoryProvider {

	/**
	 * Adapters supporting passthrough between tools and resource templates.
	 * Enables the platform tools "getResourceChildren" and "readResource" to
	 * resolve URIs to json and resource content
	 * @return
	 */
	public IResourceTemplate<?, ?>[] createResourceTemplates();
	
	/**
	 * Return objects annotated with annotations from "mcp-annotations'
	 * @return
	 */
	public Object[] getAnnotatedObjects();
	
	/**
	 * Called on server start and restart, use it to populate the initial conditions
	 * for resource additions and tool visibility
	 * @param services
	 */
	public abstract void initialize(IMCPServices services);

}
