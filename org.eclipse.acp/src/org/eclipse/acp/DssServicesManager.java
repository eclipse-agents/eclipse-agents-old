/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2023, 2025
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package org.eclipse.acp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

import com.ibm.db2.core.DssServer;
import com.ibm.db2.server.DssLauncher;
import com.ibm.jvm.Trace;
import com.ibm.systemz.db2.Tracer;
import com.ibm.systemz.db2.ide.Db2ToolingUnavailableException;
import com.ibm.systemz.db2.ide.preferences.Db2ToolsPortIterator;
import com.ibm.systemz.db2.ide.preferences.IPreferenceConstants;
import com.ibm.systemz.db2.spsql.debug.DebugTarget;

public class DssServicesManager {

	private Process agentProcess = null;
	private Integer port = null;
	private DssClientThread dssClientThread = null;
	private String dssNoFreePortError = null;
	private List<IStatusListener> statusListeners = new ArrayList<IStatusListener>();
	private static DssServicesManager instance = null;
	private Throwable lastException = null;
	
	

	private DssServicesManager() {

	}
	
	public static DssServicesManager getInstance() {
		if (instance == null) {
			instance = new DssServicesManager();
		}
		return instance;
	}

	public void start() {
		start(null, null, null);
	}
	
	public void start(Runnable runAfterProxyAvailable, Runnable runIfProxyNotAvailable, Runnable runIfNoPortAvailable) {
		
		List<String> commandAndArgs = new ArrayList<String>();
		commandAndArgs.add("gemini");
		commandAndArgs.add("--experimental-acp");
		
		ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
		agentProcess = pb.start();

		agentProcess.onExit().thenRun(new Runnable() {
			@Override
			public void run() {
				int exitValue = agentProcess.exitValue();
				String output = null;
				String errorString = null;

				System.out.println("Gemini Exit:" + exitValue);
			}
		});;
	}
	
	public void stop() {
		if (agentProcess != null && agentProcess.isAlive()) {
			agentProcess.destroyForcibly();
		}
	}
	
	
	public IStatus getStatus(boolean includePreferencesLocation) {
		
		if (agentProcess != null && agentProcess.isAlive()) {
			return Status.OK_STATUS;
		}
		return null;
	}
	
	public DssServer getDssServer() throws Db2ToolingUnavailableException {
		
		IStatus status = getStatus(true);
		
		if (status.getSeverity() == IStatus.ERROR && dssClientThread!= null && dssClientThread.isAlive()) {
			// In case of auto-connect requesting server shortly after activation
			// First two client connect attempts separated by 2 seconds
			Tracer.trace(getClass(), Trace.FINE, "DssServer is null, sleeping for 3 seconds"); //$NON-NLS-1$
			try {
				if (Thread.currentThread() == Activator.getDisplay().getThread()) {
					Tracer.trace(getClass(), Trace.FINEST, "getDssServer() dispatching UI events"); //$NON-NLS-1$
					Activator.getDisplay().readAndDispatch();
				}
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		status = this.getStatus(true);
		
		if (status.getSeverity() == IStatus.ERROR) {
			Tracer.trace(getClass(), Trace.FINE, "getDssServer() throwing Db2ToolingUnavailableException"); //$NON-NLS-1$
			throw new Db2ToolingUnavailableException(status);
		}
		Tracer.trace(getClass(), Trace.FINEST, "getDssServer() returning server"); //$NON-NLS-1$

		return dssClientThread.getDssServer();
	}
	
	public interface IStatusListener {
		public void statusChanged(IStatus status);
		public boolean includePreferencesLocation();
	}
	
	public void addDssServiceStatusListener(IStatusListener listener) {
		statusListeners.add(listener);
	}
	
	public void removeDssServiceStatusListener(IStatusListener listener) {
		statusListeners.remove(listener);
	}
	
	// PRIVATE METHODS

	
	
	public Integer getPort() {
		return port;
	}

	
	
	private IStatus getServerStatus() {
		if (lastException != null) {
			return new Status(Status.ERROR, Activator.kPluginID, 
					Messages.DssServicesManager_exception, lastException); //$NON-NLS-2$
		} else if (serverProcess == null || !serverProcess.isAlive()) {
			return new Status(Status.ERROR, Activator.kPluginID, 
					Messages.DssServicesManager_server_missing);
		}
		
		return Status.OK_STATUS;
	}
	
	private IStatus getClientStatus() {
		return dssClientThread != null ? dssClientThread.getStatus() : new Status(Status.ERROR, Activator.kPluginID, Messages.DssServicesManager_client_missing);
	}
	

	public void notifyListeners() {
		final IStatusListener[] listeners = statusListeners.toArray(new IStatusListener[0]);
		for (IStatusListener listener: listeners) {
			IStatus status = getStatus(listener.includePreferencesLocation());
			listener.statusChanged(status);
		}
	}
}
