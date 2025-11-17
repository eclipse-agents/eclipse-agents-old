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
package org.eclipse.agents.contexts;

import org.eclipse.agents.Tracer;
import org.eclipse.agents.contexts.server.ExtensionManager;
import org.eclipse.agents.contexts.server.ServerManager;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;



/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.agents.contexts"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private ScopedPreferenceStore preferenceStore = null;
	private ExtensionManager extensionManager = null;
	private ServerManager serverManager = null;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		context.addBundleListener(new BundleListener() {
			@Override
			public void bundleChanged(BundleEvent event) {
				if (event.getBundle() == getBundle() && event.getType() == BundleEvent.STARTED) {
					Tracer.trace().trace(Tracer.CONTEXTS, event.getBundle().getBundleId() + " STARTED"); //$NON-NLS-1$
					extensionManager = new ExtensionManager();
					serverManager = new ServerManager();
				}
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	public void requestServerRestart() {
		Tracer.trace().trace(Tracer.CONTEXTS, "MCP Server Restart Requested"); //$NON-NLS-1$
		serverManager.forceRestart();
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	

	public ExtensionManager getExtensionManager() {
		return extensionManager;
	}

	public ServerManager getServerManager() {
		return serverManager;
	}
	
	public IPreferenceStore getPreferenceStore() {

		// Create the preference store lazily.
		if (preferenceStore == null) {
			preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, getBundle().getSymbolicName());
		}
		return preferenceStore;
	}
}
