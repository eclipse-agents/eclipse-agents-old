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
package org.eclipse.mcp.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.mcp.Activator;
import org.eclipse.mcp.acp.AcpService;
import org.eclipse.mcp.acp.agent.AbstractService;
import org.eclipse.mcp.acp.agent.IAgentService;

public class PreferenceInitializer extends AbstractPreferenceInitializer implements IPreferenceConstants {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(P_SERVER_ENABLED, false);
		store.setDefault(P_SERVER_HTTP_PORT, 8673);
		
		for (IAgentService service: AcpService.instance().getAgents()) {
			if (service instanceof AbstractService) {
				store.setDefault(
						((AbstractService)service).getStartupCommandPreferenceId(),
						String.join("\n", service.getDefaultStartupCommand()));
			}				
		}
	}
}
