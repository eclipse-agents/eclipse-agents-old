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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.agents.chat.ChatView;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;

public class ToolbarSessionSelector extends AbstractDynamicToolbarDropdown {

	NewSessionAction newSessionAction = new NewSessionAction();
	
	public ToolbarSessionSelector(ChatView view) {
		super("Session", "Load or create a session", view);
		
		setEnabled(false);
	}

	@Override
	protected void fillMenu(MenuManager menuManager) {
		
//		List<Action> actions = new ArrayList<Action>();
		//TODO add load session actions, if supported
		
		menuManager.add(newSessionAction);
	}

	class NewSessionAction extends Action {
		
		public NewSessionAction() {
			super("New Session");
		}

		@Override
		public void run() {
			AgentController.instance();
//			AgentController.instance().setAcpService(getView(), agent);
//			ToolbarSessionSelector.this.updateText(agent.getName());
		}
		
		public IAgentService getAgent() {
			return agent;
		}
	}
}
