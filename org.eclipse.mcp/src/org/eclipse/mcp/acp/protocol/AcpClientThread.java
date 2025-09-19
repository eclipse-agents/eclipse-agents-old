/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2022, 2025
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package org.eclipse.mcp.acp.protocol;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mcp.Activator;
import org.eclipse.mcp.internal.Tracer;


public abstract class AcpClientThread extends Thread {

	private AcpClientLauncher launcher;
	private Exception lastException;

	public AcpClientThread(AcpClientLauncher launcher) {
		super("ACP-Client-Thread");
		this.launcher = launcher;
	}

	@Override
	public void run() {
		
		Tracer.trace().trace(Tracer.ACP, "starting Db2 for z/OS tooling client on port ");
		
		try {
			Tracer.trace().trace(Tracer.ACP, "Calling DssClientLauncher.startListening()"); //$NON-NLS-1$
			launcher.startListening().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			lastException = e;
		} catch (ExecutionException e) {
			e.printStackTrace();
			lastException = e;
		}

		if (lastException == null) {
			Tracer.trace().trace(Tracer.ACP, "DssClientLauncher has stopped listening"); //$NON-NLS-1$
		} else {
			Tracer.trace().trace(Tracer.ACP, "DssClientLauncher has stopped listening", lastException); //$NON-NLS-1$
		}

		
		statusChanged();
	}

	public IAcpAgent getAgent() {
		if (launcher != null) {
			return launcher.getRemoteProxy();
		}
		return null;
	}
	
	public IStatus getStatus() {
		if (lastException != null) {
			return new Status(Status.ERROR, Activator.PLUGIN_ID, 
					MessageFormat.format("Messages.DssClientThread_generic_error", ""), lastException); //$NON-NLS-1$
		} else if (getAgent() == null) {
			return new Status(Status.ERROR, Activator.PLUGIN_ID, 
					MessageFormat.format("Messages.DssClientThread_remote_proxy_error", "")); //$NON-NLS-1$
		} else if (!this.isAlive()) {
			return new Status(Status.ERROR, Activator.PLUGIN_ID, 
					MessageFormat.format("Messages.DssClientThread_thread_stopped_error", "")); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
	}
	
	public abstract void statusChanged();
}
