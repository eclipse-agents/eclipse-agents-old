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
package org.eclipse.mcp.acp.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mcp.Activator;
import org.eclipse.mcp.internal.preferences.IPreferenceConstants;

public class GeminiService extends AbstractService {

	
	public GeminiService() {
		
	}

	@Override
	public String getName() {
		return "Gemini CLI";
	}

	@Override
	public Process createProcess() throws IOException {
		String node = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.P_ACP_NODE); 
		String gemini = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.P_ACP_GEMINI);

	
		List<String> commandAndArgs = new ArrayList<String>();
//		commandAndArgs.add("gemini");
		commandAndArgs.add(node);
		commandAndArgs.add(gemini);
		commandAndArgs.add("--experimental-acp");
//		commandAndArgs.add("--debug");
		
		ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
		return pb.start();
		
	}

}
