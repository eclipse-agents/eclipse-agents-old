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
package org.eclipse.mcp.resource;

import org.eclipse.mcp.platform.resource.ResourceSchema.Children;
import org.eclipse.mcp.platform.resource.ResourceSchema.DEPTH;

public interface IResourceHierarchy<T, U> extends IResourceTemplate<T, U> {

	public Children<U> getChildren(DEPTH depth);
}
