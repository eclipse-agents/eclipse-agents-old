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
package org.eclipse.agents.contexts.platform;

import org.eclipse.agents.IFactoryProvider;
import org.eclipse.agents.IMCPServices;
import org.eclipse.agents.contexts.adapters.IResourceTemplate;
import org.eclipse.agents.contexts.platform.resource.ConsoleAdapter;
import org.eclipse.agents.contexts.platform.resource.EditorAdapter;
import org.eclipse.agents.contexts.platform.resource.WorkspaceResourceAdapter;

public class FactoryProvider implements IFactoryProvider {

	ResourceController editors;
	
	public FactoryProvider() {
		editors = new ResourceController();
	}


	@Override
	public IResourceTemplate<?, ?>[] createResourceTemplates() {
		return new IResourceTemplate[] {
			new ConsoleAdapter(),
			new EditorAdapter(),
			new WorkspaceResourceAdapter()
//			new AbsoluteFileAdapter()
		};
	}

	@Override
	public Object[] getAnnotatedObjects() {
		return new Object[] {
			new Tools(),
			new ResourceTemplates()
		};
	}


	@Override
	public void initialize(IMCPServices services) {
		editors.initialize(services);
	}

}
