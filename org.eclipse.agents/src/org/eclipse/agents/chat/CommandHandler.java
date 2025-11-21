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
package org.eclipse.agents.chat;


import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.actions.AddToChatAction;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
* Handles command invocations and routes to 
*/
public class CommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Tracer.trace().trace(Tracer.CHAT, event.toString());
		if (AddToChatAction.ID.equals(event.getCommand().getId())) {
			new AddToChatAction(event).run();
		}
		return null;
	}
}
