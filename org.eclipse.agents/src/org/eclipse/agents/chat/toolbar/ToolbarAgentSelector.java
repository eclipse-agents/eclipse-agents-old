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
import org.eclipse.agents.chat.actions.SelectAgentAction;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.jface.action.MenuManager;

public class ToolbarAgentSelector extends AbstractDynamicToolbarDropdown {

	List<SelectAgentAction> actions;
	
	public ToolbarAgentSelector(ChatView view) {
		super("Coding Agent...", "Select a coding agent", view);
		
		actions = new ArrayList<SelectAgentAction>();
		for (IAgentService agent: AgentController.instance().getAgents()) {
			actions.add(new SelectAgentAction(view, agent, this));
		}
	}

	@Override
	protected void fillMenu(MenuManager menuManager) {
		for (SelectAgentAction action: actions) {
			menuManager.add(action);
			action.setChecked(action.getAgent() ==  getView().getActiveAgent());
		}
	}
}
