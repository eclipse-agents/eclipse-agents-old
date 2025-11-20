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

/**
 * Supports the transformation between URIs, Eclipse objects, resource links and resource content
 * Supports the built-in tool readResource and getChildResources
 * 
 * @param <T> the type of Eclipse object the adapter can transform URIs into
 * @param <U> the type of JSON record the adapter can transform URIs into
 */
public interface IResourceAdapter<U> {

	public U toJson();

}
