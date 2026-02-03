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
package org.eclipse.agents.services.agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

public class CustomAgentService extends AbstractService {

	private final String id;
	private final String name;
	private final String[] command;
	private final String workingDirectory;

	public CustomAgentService(String id, String name, String[] command, String workingDirectory) {
		this.id = id;
		this.name = name;
		this.command = command;
		this.workingDirectory = workingDirectory;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getFolderName() {
		return "custom-" + id;
	}

	@Override
	public String[] getDefaultStartupCommand() {
		return command;
	}

	@Override
	public void checkForUpdates(IProgressMonitor monitor) throws IOException {
		// No-op for custom agents
	}

	@Override
	public Process createProcess() throws IOException {
		String[] startup = getStartupCommand();

		List<String> commandAndArgs = new ArrayList<String>();
		for (String arg : startup) {
			commandAndArgs.add(arg);
		}

		ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
		if (workingDirectory != null && !workingDirectory.isEmpty()) {
			File workDir = new File(workingDirectory);
			if (workDir.exists() && workDir.isDirectory()) {
				pb.directory(workDir);
			}
		}
		return pb.start();
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}
}
