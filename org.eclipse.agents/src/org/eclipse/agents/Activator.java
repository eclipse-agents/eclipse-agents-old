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
package org.eclipse.agents;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.agents"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private ExtensionManager extensionManager = null;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Tracer.setup(context);
		
		context.addBundleListener(new BundleListener() {
			@Override
			public void bundleChanged(BundleEvent event) {
				if (event.getBundle() == getBundle() && event.getType() == BundleEvent.STARTED) {
					Tracer.trace().trace(Tracer.CONTEXTS, event.getBundle().getBundleId() + " STARTED"); //$NON-NLS-1$
					extensionManager = new ExtensionManager();
				}
			}
		});
		
		// if not running headless unit tests
		if (PlatformUI.isWorkbenchRunning()) {

		} else {
			
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
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

	
	public static Display getDisplay() {
		return Display.getCurrent() == null ? Display.getDefault() : Display.getCurrent();
	}
	
	
	public File getBundleFile(String bundlePath) throws IOException, URISyntaxException {
		Tracer.trace().trace(Tracer.CONTEXTS, "getBundleFile(): " + bundlePath); //$NON-NLS-1$
		URL pathUrl = FileLocator.find(getBundle(), new Path(bundlePath));
		Tracer.trace().trace(Tracer.CONTEXTS, "pathUrl: " + pathUrl); //$NON-NLS-1$
		URL fileUrl = FileLocator.toFileURL(pathUrl);
		Tracer.trace().trace(Tracer.CONTEXTS, "fileUrl: " + fileUrl); //$NON-NLS-1$
		URI fileUri = new URI(fileUrl.getProtocol(), fileUrl.getPath(), null);
		Tracer.trace().trace(Tracer.CONTEXTS, "fileUri: " + fileUri); //$NON-NLS-1$
		return new File(fileUri);
	}
}
