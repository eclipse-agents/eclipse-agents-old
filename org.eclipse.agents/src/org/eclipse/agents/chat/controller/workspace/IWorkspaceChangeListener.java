/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.agents.chat.controller.workspace;

public interface IWorkspaceChangeListener {

	/**
	 * Called the first time a workspace change is made for a path
	 * @param sessionId
	 * @param change
	 */
	public void changeAdded(String sessionId, WorkspaceChange change);
	
	/**
	 * Called if additional writes are made to a path, 
	 * modifying the current contents of the path
	 * @param sessionID
	 * @param change
	 */
	public void changeModified(String sessionID, WorkspaceChange change);
	
	/**
	 * Called when the user activity warrants removing a change from review.
	 * May be due to user reverting a change, or accepting the changes.
	 * @param sessionId
	 * @param change
	 */
	public void changeRemoved(String sessionId, WorkspaceChange change);

}
