/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2022, 2025
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package org.eclipse.mcp.acp;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;

public class AcpClientLauncher<T> implements Launcher<T> {

	private final Launcher<T> launcher;
	private boolean traceLsp4jJsonrpc = Boolean.getBoolean("org.eclipse.acp.trace.lsp4j.jsonrpc"); //$NON-NLS-1$
	
	public AcpClientLauncher(Object localService, Class<T> remoteInterface, InputStream is, OutputStream os) {
		try {
			
			PrintWriter tracer = traceLsp4jJsonrpc ? new PrintWriter(System.out) : null;
			
			this.launcher = new Builder<T>()
					.setLocalService(localService)
					.setRemoteInterface(remoteInterface)
					.setInput(is)
					.setOutput(os)
					.traceMessages(tracer)
					.create();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Future<Void> startListening() {
		return CompletableFuture.runAsync(() -> {
			try {
				this.launcher.startListening().get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}, Executors.newSingleThreadExecutor());
	}

	public T getRemoteProxy() {
		return this.launcher.getRemoteProxy();
	}

	@Override
	public RemoteEndpoint getRemoteEndpoint() {
		return null;
	}
}
