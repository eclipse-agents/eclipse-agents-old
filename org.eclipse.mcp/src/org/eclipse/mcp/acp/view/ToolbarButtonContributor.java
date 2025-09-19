package org.eclipse.mcp.acp.view;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;

public class ToolbarButtonContributor extends ContributionItem {

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	@Override
	public void fill(Menu menu, int index) {
		
		MenuManager manager = (MenuManager)menu.getData();
		manager.getId();
		
		if (Command.MODE.equals(manager.getId())) {
			
		} else if (Command.MODEL.equals(manager.getId())) {
			
		}
	}
}
