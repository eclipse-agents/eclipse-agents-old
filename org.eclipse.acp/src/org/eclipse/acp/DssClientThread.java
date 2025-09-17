/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2022, 2025
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package org.eclipse.acp;

import java.io.IOException;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.ibm.db2.core.DssServer;
import com.ibm.jvm.Trace;

public abstract class DssClientThread extends Thread {

	private DssClientLauncher<DssServer> launcher;
	private Integer port;
	private Exception lastException;
	private Runnable runAfterProxyIsAvailable;
	private Runnable runIfProxyNotAvailable;

	public DssClientThread(Integer port, Runnable runAfterProxyIsAvailable, Runnable runIfProxyNotAvailable) {
		super(Messages.DssClientThread_name);
		this.port = port;
		this.runAfterProxyIsAvailable = runAfterProxyIsAvailable;
		this.runIfProxyNotAvailable = runIfProxyNotAvailable;
	}

	@Override
	public void run() {
		
		Tracer.trace(getClass(), Trace.FINE, "starting Db2 for z/OS tooling client on port " + port); //$NON-NLS-1$
		ContextStore<AcpAgent> contextStore = new ContextStore<>();
		// Create the server.
		AcpClient dssClient = new AcpClient(contextStore);

		String host = "localhost"; //$NON-NLS-1$

		// connect to the server
		int connectAttempt = 1;
		int connectDelay = 2;
		Socket socket = null;
		
		while (socket == null && connectAttempt <= 5) {
			try {
				socket = new Socket(host, port);
				Tracer.trace(this.getClass(), Trace.FINE, "Connection attempt " + connectAttempt + " succeeded"); //$NON-NLS-1$ //$NON-NLS-2$
			}catch (IOException e) {
				lastException = e;
				socket = null;
				Tracer.trace(this.getClass(), Trace.FINER, "Connection attempt " + connectAttempt + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$
				connectAttempt++;				
				try {
					Thread.sleep(1000 * connectDelay);
					connectDelay *= connectDelay;
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
		
		if (socket != null) {
			// open a JSON-RPC connection for the opened socket
			this.launcher = new DssClientLauncher<>(dssClient, DssServer.class, socket);
			lastException = null;
			/*
			 * Start listening for incoming message. When the JSON-RPC connection is closed, e.g. the server is died,
			 * the client process should exit.
			 */
			try {
				
				new Thread() {
					public void run() {
						statusChanged();
						
						// wait for the client's proxy to the server to be available
						int proxyAttempt = 1;
						int proxyDelay = 0;
						DssServer server = launcher.getRemoteProxy();
						
						while (server == null && proxyAttempt <= 5) {
							Tracer.trace(this.getClass(), Trace.FINER, "Remote Proxy Not Available after " + proxyAttempt + " attempts"); //$NON-NLS-1$ //$NON-NLS-2$
							try {
								proxyDelay += 1;
								proxyAttempt++;
								Thread.sleep(1000 * proxyDelay);
								server = launcher.getRemoteProxy();
							} catch (InterruptedException ie) {
								ie.printStackTrace();
							}
						}
						
						if (server != null) {
							Tracer.trace(this.getClass(), Trace.FINE, "Remote Proxy Available after " + proxyAttempt + " attempts"); //$NON-NLS-1$ //$NON-NLS-2$
							if (runAfterProxyIsAvailable != null) {
								Tracer.trace(getClass(), Trace.FINEST, "Invoking Runnable runAfterProxyIsAvailable"); //$NON-NLS-1$
								runAfterProxyIsAvailable.run();
							}
						} else {
							Tracer.trace(this.getClass(), Trace.FINE, "Remote Proxy Not Available After 5 Attempts"); //$NON-NLS-1$
							Tracer.trace(getClass(), Trace.FINEST, "Invoking Runnable runIfProxyNotAvailable"); //$NON-NLS-1$
							if (runIfProxyNotAvailable != null) {
								runIfProxyNotAvailable.run();
							}
						}
					}
				}.start();

				Tracer.trace(DssClientThread.class, Trace.FINEST, "Calling DssClientLauncher.startListening()"); //$NON-NLS-1$
				launcher.startListening().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				lastException = e;
			} catch (ExecutionException e) {
				e.printStackTrace();
				lastException = e;
			}

			if (lastException == null) {
				Tracer.trace(DssClientThread.class, Trace.FINE, "DssClientLauncher has stopped listening"); //$NON-NLS-1$
			} else {
				Tracer.trace(DssClientThread.class, Trace.FINE, "DssClientLauncher has stopped listening", lastException); //$NON-NLS-1$
			}

		} else {
			Tracer.trace(DssClientThread.class, Trace.FINE, "Failed to obtain socket after 5 attempts"); //$NON-NLS-1$
			System.err.println(Messages.DssClientThread_five_fail_attempts);
			
		}
		statusChanged();
	}

	public DssServer getDssServer() {
		if (launcher != null) {
			return launcher.getRemoteProxy();
		}
		return null;
	}
	
	public IStatus getStatus() {
		if (lastException != null) {
			return new Status(Status.ERROR, Activator.kPluginID, 
					MessageFormat.format(Messages.DssClientThread_generic_error, "" + port), lastException); //$NON-NLS-1$
		} else if (getDssServer() == null) {
			return new Status(Status.ERROR, Activator.kPluginID, 
					MessageFormat.format(Messages.DssClientThread_remote_proxy_error, "" + port)); //$NON-NLS-1$
		} else if (!this.isAlive()) {
			return new Status(Status.ERROR, Activator.kPluginID, 
					MessageFormat.format(Messages.DssClientThread_thread_stopped_error, "" + port)); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
	}
	
	public abstract void statusChanged();
}
