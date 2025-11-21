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
package org.eclipse.agents.chat.actions;

import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.AcpView;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddToChatAction extends Action {

	public static final String ID = "org.eclipse.agents.services.cmd.addToChat";

	ExecutionEvent event;
	public AddToChatAction(ExecutionEvent event) {
		super();
		this.event = event;
	}

	@Override
	public void run() {
		Tracer.trace().trace(Tracer.CHAT, event.toString());
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			AcpView view = (AcpView) page.showView(AcpView.ID, null, //$NON-NLS-1$
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
