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
package org.eclipse.mcp.acp.view.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.view.AcpView;

public class ToolbarModeSelector extends AbstractDynamicToolbarDropdown {

	List<ModelAction> actions;
	
	public ToolbarModeSelector(AcpView view) {
		super("Mode", "Select a mode", view);
		
		actions = new ArrayList<ModelAction>();
		for (IAgentService agent: AcpService.instance().getAgents()) {
			actions.add(new ModelAction(agent));
		}
		setEnabled(false);
	}

	@Override
	protected void fillMenu(MenuManager menuManager) {
//		for (ModelAction action: actions) {
//			menuManager.add(action);
//			action.setChecked(action.getAgent() ==  AcpService.instance().getAgentService());
//		}
	}

	class ModelAction extends Action {
		IAgentService agent;
		
		public ModelAction(IAgentService agent) {
			super(agent.getName());
			this.agent = agent;
		}

		@Override
		public void run() {
			AcpService.instance().setAcpService(getView(), agent);
			ToolbarModeSelector.this.updateText(agent.getName());
		}
		
		public IAgentService getAgent() {
			return agent;
		}
	}
}
