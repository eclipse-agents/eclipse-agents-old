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

import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.AcpView;
import org.eclipse.agents.contexts.Images;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ToolbarSessionStartStop extends Action {

	AcpView view;
	boolean promptTurnInProgress = false;
	
	public ToolbarSessionStartStop(AcpView view) {
		super("Send", Activator.getDefault().getImageRegistry().getDescriptor(Images.IMG_PLAY));
		this.view = view;
		setEnabled(false);
	}

	public void prompTurnStarted() {
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_STOP));
		promptTurnInProgress = true;
		setText("Stop");
	}
	
	public void prompTurnEnded() {
		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Images.IMG_PLAY));
		promptTurnInProgress = false;
		setText("Send");
	}

	@Override
	public void run() {
		if (promptTurnInProgress) {
			view.stopPromptTurn();
		} else {
			view.startPromptTurn();
		}
	}
}
