/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2022, 2025
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package org.eclipse.acp;

import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.ibm.db2.debug.core.psmd.PSMDJavaStackFrame;
import com.ibm.db2.debug.core.psmd.PSMDStackFrame;
import com.ibm.ftt.common.tracing.Trace;
import com.ibm.systemz.db2.Tracer;

public class DssClientLauncher<T> implements Launcher<T> {

	private final Launcher<T> launcher;
	private boolean traceLsp4jJsonrpc = Boolean.getBoolean("com.ibm.systemz.db2.trace.lsp4j.jsonrpc"); //$NON-NLS-1$
	
	public DssClientLauncher(Object localService, Class<T> remoteInterface, Socket socket) {
		try {
			
			PrintWriter tracer = traceLsp4jJsonrpc ? new PrintWriter(System.out) : null;
			

			this.launcher = new Builder<T>()
					.setLocalService(localService)
					.setRemoteInterface(remoteInterface)
					.setInput(socket.getInputStream())
					.setOutput(socket.getOutputStream())
					.traceMessages(tracer)
					.configureGson(gsonBuilder -> {
						gsonBuilder.registerTypeAdapter(PSMDStackFrame.class, new PSMDStackFrameAdapter());
					})
					.create();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Future<Void> startListening() {
		Tracer.trace(getClass(), Trace.FINEST, "startListening()"); //$NON-NLS-1$
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
	
	/**
	 * Custom deserializer to control whether to create a PSMDJavaStackFrame or PSMDStackFrame-
	 */
	private class PSMDStackFrameAdapter implements JsonDeserializer<PSMDStackFrame> {
		@Override
		public PSMDStackFrame deserialize(JsonElement element, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (element.getAsJsonObject().has("fJVMDebugIP")) { //$NON-NLS-1$
				return context.deserialize(element, PSMDJavaStackFrame.class);
			}
			return context.deserialize(element, PSMDStackFrame2.class);
		}
	};
	
	/**
	 * Used to prevent infinite deserialization of PSMDStackFrame in non-java case
	 */
	private class PSMDStackFrame2 extends PSMDStackFrame {
		public PSMDStackFrame2(String connectionId, String rid, int stackframe) {
			super(connectionId, rid, stackframe);
		}
	}
}
