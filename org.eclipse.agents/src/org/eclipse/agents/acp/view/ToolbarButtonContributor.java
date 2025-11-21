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
package org.eclipse.agents.acp.view;

import org.eclipse.agents.acp.AcpService;
import org.eclipse.agents.acp.agent.IAgentService;
import org.eclipse.agents.acp.view.actions.SetAcpModelAction;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;

public class ToolbarButtonContributor extends ContributionItem {

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	@Override
	public void fill(Menu menu, int index) {
		
		MenuManager manager = (MenuManager)menu.getData();
		manager.getId();
		
		if ("org.eclipse.agents.acp.cmd.model".equals(manager.getId())) {
			if (menu.getItemCount() == 0) {
				for (IAgentService agent: AcpService.instance().getAgents()) {
					MenuItem menuItem = new MenuItem(menu, SWT.PUSH, index++);
		            menuItem.setText(agent.getName());
		            menuItem.setData(new SetAcpModelAction(agent));
		            menuItem.addSelectionListener(new SelectionListener() {
		            	@Override
						public void widgetDefaultSelected(SelectionEvent arg0) {}

						@Override
						public void widgetSelected(SelectionEvent e) {
//							AcpService.instance().setAcpService(agent);
						}
		            });
				}
			}
		}
	}

	@Override
	public void fill(CoolBar parent, int index) {
		// TODO Auto-generated method stub
		super.fill(parent, index);
	}

	@Override
	public void fill(ToolBar parent, int index) {
		// TODO Auto-generated method stub
		super.fill(parent, index);
	}
	
}
