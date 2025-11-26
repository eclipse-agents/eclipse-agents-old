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
package org.eclipse.agents.chat.toolbar;

import org.eclipse.agents.chat.ChatView;
import org.eclipse.agents.chat.actions.NewSessionAction;
import org.eclipse.jface.action.MenuManager;

public class ToolbarSessionSelector extends AbstractDynamicToolbarDropdown {

	NewSessionAction newSessionAction;
	
	public ToolbarSessionSelector(ChatView view) {
		super("Session", "Load or create a session", view);
		newSessionAction = new NewSessionAction(view, this);
		setEnabled(false);
	}

	@Override
	protected void fillMenu(MenuManager menuManager) {
		
//		List<Action> actions = new ArrayList<Action>();
		//TODO add load session actions, if supported
		
		menuManager.add(newSessionAction);
	}
}
