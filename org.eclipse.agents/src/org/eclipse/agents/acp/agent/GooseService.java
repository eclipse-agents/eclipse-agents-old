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
package org.eclipse.agents.acp.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

public class GooseService extends AbstractService {

	
	public GooseService() {
		
	}

	@Override
	public String getName() {
		return "Goose";
	}

	@Override
	public Process createProcess() throws IOException {
		String goose = "/Users/jflicke/.local/bin/goose"; 
	
		List<String> commandAndArgs = new ArrayList<String>();
		commandAndArgs.add(goose);
		commandAndArgs.add("acp");
		
		ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
		return pb.start();
		
	}

	@Override
	public String getId() {
		return "goose";
	}

	@Override
	public String[] getDefaultStartupCommand() {
		return new String[] {
				"/Users/jflicke/.local/bin/goose",
				"acp"
		};
	}

	@Override
	public void checkForUpdates() throws IOException {
		
	}
}
