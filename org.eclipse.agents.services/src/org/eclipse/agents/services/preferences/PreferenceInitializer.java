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
package org.eclipse.agents.services.preferences;

import org.eclipse.agents.contexts.Activator;
import org.eclipse.agents.services.acp.AcpService;
import org.eclipse.agents.services.acp.agents.AbstractService;
import org.eclipse.agents.services.acp.agents.IAgentService;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		
		for (IAgentService service: AcpService.instance().getAgents()) {
			if (service instanceof AbstractService) {
				store.setDefault(
						((AbstractService)service).getStartupCommandPreferenceId(),
						String.join("\n", service.getDefaultStartupCommand()));
			}				
		}
	}
}
