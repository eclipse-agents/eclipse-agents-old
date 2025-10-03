package org.eclipse.mcp.acp.view.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mcp.acp.view.AcpView;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddToChatAction extends Action {

	public static final String ID = "org.eclipse.mcp.acp.cmd.addToChat";

	ExecutionEvent event;
	public AddToChatAction(ExecutionEvent event) {
		super();
		this.event = event;
	}

	@Override
	public void run() {
		System.out.println(event);
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			AcpView view = (AcpView) page.showView("org.eclipse.mcp.acp.view.AcpView", null, //$NON-NLS-1$
					IWorkbenchPage.VIEW_CREATE);

			IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
			for (Object o: selection.toArray()) {
				view.addContext(o);
			}
			event.getParameters();
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
