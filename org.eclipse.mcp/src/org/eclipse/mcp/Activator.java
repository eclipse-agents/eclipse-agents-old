package org.eclipse.mcp;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.mcp.acp.GeminiService;
import org.eclipse.mcp.internal.ExtensionManager;
import org.eclipse.mcp.internal.Images;
import org.eclipse.mcp.internal.ServerManager;
import org.eclipse.mcp.internal.Tracer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.mcp"; //$NON-NLS-1$

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
		Tracer.setup(context);
		
		context.addBundleListener(new BundleListener() {
			@Override
			public void bundleChanged(BundleEvent event) {
				if (event.getBundle() == getBundle() && event.getType() == BundleEvent.STARTED) {
					Tracer.trace().trace(Tracer.DEBUG, event.getBundle().getBundleId() + " STARTED"); //$NON-NLS-1$
					extensionManager = new ExtensionManager();
					serverManager = new ServerManager();
				}
			}
		});
		
		// if not running headless unit tests
		if (PlatformUI.isWorkbenchRunning()) {

		} else {
			Tracer.trace().trace(Tracer.DEBUG, "Copy action cannot be initialized w/o a workbench"); //$NON-NLS-1$
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	public void requestServerRestart() {
		Tracer.trace().trace(Tracer.DEBUG, "MCP Server Restart Requested"); //$NON-NLS-1$
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

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		for (int i = 0; i < Images.imagelist.length; i++) {
			String key = Images.imagelist[i];
			reg.put(key, createImageDescriptor(key));
		}
	}

	protected ImageDescriptor createImageDescriptor(String relativePath) {
		Optional<ImageDescriptor> imageDescriptor = ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, relativePath);
		if (!imageDescriptor.isPresent()) {
			Tracer.trace().trace(Tracer.DEBUG, "Failed to load image: " + relativePath); //$NON-NLS-1$
			return ImageDescriptor.getMissingImageDescriptor();
		}
		return imageDescriptor.get();
	}
	
	public static Display getDisplay() {
		return Display.getCurrent() == null ? Display.getDefault() : Display.getCurrent();
	}
	
	// Behavior tested against OS X Voice Over and Windows 11 + JAWS 2022
	public static void addAccessibilityData(Control control, final String name, final String tooltip) {
//		control.getAccessible().addAccessibleListener(new AccessibleListener() {
//			@Override
//			public void getDescription(AccessibleEvent arg0) {
//				if (SystemUtils.IS_OS_MAC) {
//					arg0.result = name;
//					if (arg0.getSource() instanceof Accessible) {
//						// Mac Voice Over does not include name of surrounding swt group
//						Control c = ((Accessible)arg0.getSource()).getControl();
//						if (c.getParent() instanceof Group) {
//							arg0.result = MessageFormat.format(Messages.Accessibility_OSMAC_GROUPNAME_CONTROLNAME, ((Group)c.getParent()).getText(), name);
//						}
//					}
//				} else {
//					arg0.result = tooltip;
//				}
//			}
//
//			@Override
//			public void getHelp(AccessibleEvent arg0) {
//				if (SystemUtils.IS_OS_MAC) {
//					arg0.result = tooltip;
//				} else {
//					arg0.result = null;
//				}
//			}
//
//			@Override
//			public void getKeyboardShortcut(AccessibleEvent arg0) {
//				arg0.result = null;
//			}
//
//			@Override
//			public void getName(AccessibleEvent arg0) {
//				if (SystemUtils.IS_OS_MAC) {
//					arg0.result = null;
//				} else {
//					arg0.result = name;
//				}
//			}
//		});
	}
	
	public File getBundleFile(String bundlePath) throws IOException, URISyntaxException {
		Tracer.trace().trace(Tracer.DEBUG, "getBundleFile(): " + bundlePath); //$NON-NLS-1$
		URL pathUrl = FileLocator.find(getBundle(), new Path(bundlePath));
		Tracer.trace().trace(Tracer.DEBUG, "pathUrl: " + pathUrl); //$NON-NLS-1$
		URL fileUrl = FileLocator.toFileURL(pathUrl);
		Tracer.trace().trace(Tracer.DEBUG, "fileUrl: " + fileUrl); //$NON-NLS-1$
		URI fileUri = new URI(fileUrl.getProtocol(), fileUrl.getPath(), null);
		Tracer.trace().trace(Tracer.DEBUG, "fileUri: " + fileUri); //$NON-NLS-1$
		return new File(fileUri);
	}
	
	
	public IPreferenceStore getPreferenceStore() {

		// Create the preference store lazily.
		if (preferenceStore == null) {
			preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, getBundle().getSymbolicName());
		}
		return preferenceStore;
	}
}
