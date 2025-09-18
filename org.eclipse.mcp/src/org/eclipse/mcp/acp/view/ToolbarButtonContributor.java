package org.eclipse.mcp.acp.view;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.mcp.internal.Tracer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class ToolbarButtonContributor extends ContributionItem {

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	@Override
	public void fill(Menu menu, int index) {
		
		try {
			Command regenerateCommand = null;
			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IWorkbenchPart part = activePage.getActivePart();
			if (part instanceof AcpView) {
				AcpView codeExplanationView = (AcpView)part;
				
			}
			
			if (regenerateCommand != null) {
				for (MenuItem item: menu.getItems()) {
					boolean selected = false;
					Object contribution = item.getData();;
					if (contribution instanceof ContributionItem) {
						String id = ((ContributionItem)contribution).getId();
//						if (regenerateCommand.getCommandId().equals(id)) {
//							selected = true;
//						}
					}
					item.setSelection(selected);
				}
			}
		} catch (Exception e) {
			Tracer.trace().trace(Tracer.ACP, e.getMessage(), e);
		}
	}
}
