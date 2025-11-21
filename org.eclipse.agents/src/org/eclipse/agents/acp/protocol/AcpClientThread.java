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
package org.eclipse.agents.acp.protocol;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

import org.eclipse.agents.Activator;
import org.eclipse.agents.internal.Tracer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


public abstract class AcpClientThread extends Thread {

	private AcpClientLauncher launcher;
	private Exception lastException;

	public AcpClientThread(AcpClientLauncher launcher) {
		super("ACP-Client-Thread");
		this.launcher = launcher;
	}

	@Override
	public void run() {
		
		Tracer.trace().trace(Tracer.ACP, "Starting AcpClientThread");
		
		try {
			launcher.startListening().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			lastException = e;
		} catch (ExecutionException e) {
			e.printStackTrace();
			lastException = e;
		}

		if (lastException == null) {
			Tracer.trace().trace(Tracer.ACP, "AcpClientThread has stopped listening"); //$NON-NLS-1$
		} else {
			Tracer.trace().trace(Tracer.ACP, "AcpClientThread has stopped listening", lastException); //$NON-NLS-1$
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
