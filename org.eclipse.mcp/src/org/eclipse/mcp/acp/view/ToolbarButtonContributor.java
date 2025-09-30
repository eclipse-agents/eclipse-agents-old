package org.eclipse.mcp.acp.view;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.agent.IAgentService;
import org.eclipse.mcp.acp.view.actions.SetAcpModelAction;
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
		
		if (Command.MODE.getCommandId().equals((manager.getId()))) {
//			AcpService.instance().
		} else if (Command.MODEL.getCommandId().equals(manager.getId())) {
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
							AcpService.instance().setAcpService(agent);
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
