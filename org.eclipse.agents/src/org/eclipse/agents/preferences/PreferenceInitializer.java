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
package org.eclipse.agents.preferences;

import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.services.agent.AbstractService;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer implements IPreferenceConstants {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(P_MCP_SERVER_ENABLED, false);
		store.setDefault(P_MCP_SERVER_HTTP_PORT, 8673);
		store.setDefault(P_ACP_WORKING_DIR, ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString());		
		store.setDefault(P_ACP_GEMINI_VERSION, "latest");
		store.setDefault(P_ACP_FILE_READ, true);
		store.setDefault(P_ACP_FILE_WRITE, true);
		store.setDefault(P_ACP_PROMPT4MCP, true);

		for (IAgentService service: AgentController.instance().getAgents()) {
			if (service instanceof AbstractService) {
				store.setDefault(
						((AbstractService)service).getStartupCommandPreferenceId(),
						String.join("\n", service.getDefaultStartupCommand()));
			}				
		}
	}
}
