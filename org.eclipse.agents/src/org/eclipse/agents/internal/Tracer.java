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
package org.eclipse.agents.internal;

import java.util.Hashtable;

import org.eclipse.agents.Activator;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;


public class Tracer implements DebugOptionsListener, DebugTrace {

	public static final String CONTEXTS = "/contexts";
	public static final String MCP = "/contexts/protocol";
	public static final String EXTENSIONS = "/contexts/extensions";
	public static final String PLATFORM = "/contexts/platform";
	public static final String THIRDPARTY ="/contexts/thirdparty";
	public static final String CHAT = "/chat";
	public static final String BROWSER = "/chat/browser";
	public static final String ACP = "/chat/protocol";
	
	public static final String[] OPTIONS = new String[] {
		CONTEXTS,
		MCP,
		EXTENSIONS,
		PLATFORM,
		THIRDPARTY,
		CHAT,
		BROWSER,
		ACP
	};
	
	private static DebugTrace trace = null;
    private static Tracer instance = null;
	private static DebugTrace nullTrace = new DebugTrace() {
		@Override
		public void trace(String arg0, String arg1) {}
		@Override
		public void trace(String arg0, String arg1, Throwable arg2) {}
		@Override
		public void traceDumpStack(String arg0) {}
		@Override
		public void traceEntry(String arg0) {}
		@Override
		public void traceEntry(String arg0, Object arg1) {}
		@Override
		public void traceEntry(String arg0, Object[] arg1) {}
		@Override
		public void traceExit(String arg0) {}
		@Override
		public void traceExit(String arg0, Object arg1) {}
	
	};

   private Tracer(BundleContext context) {
	   Hashtable<String, String> props = new Hashtable<String, String>(4);
       props.put(DebugOptions.LISTENER_SYMBOLICNAME, Activator.PLUGIN_ID);
       context.registerService(DebugOptionsListener.class.getName(), this, props);
	}

	public static void setup(BundleContext context) {
		if (instance == null) {
			instance = new Tracer(context);
		}
	}
	
	@Override
	public void optionsChanged(DebugOptions options) {
		trace = options.newDebugTrace(Activator.PLUGIN_ID);
		trace.trace(CONTEXTS, toString());
} 

	// used to bypass eclipse dependencies during unit testing
	public static boolean disableTracing = false;	
	
	public static DebugTrace trace() {
		if (trace == null || disableTracing) {
			return nullTrace;
		}
		return trace;
	}

	@Override
	public void trace(String option, String message) {
		trace().trace(option, message);
		if (MCP.equals(option)) {
			Activator.getDefault().getServerManager().log(message, null);
		}
	}
	@Override
	public void trace(String option, String message, Throwable error) {
		trace().trace(option, message, error);
		if (MCP.equals(option)) {
			Activator.getDefault().getServerManager().log(message, error);
		}
	}
	
	@Override
	public void traceDumpStack(String option) {
		trace().traceDumpStack(option);
	}
	
	@Override
	public void traceEntry(String option) {
		trace().traceEntry(option);
	}
	
	@Override
	public void traceEntry(String option, Object methodArgument) {
		trace().traceEntry(option, methodArgument);
	}
	
	@Override
	public void traceEntry(String option, Object[] methodArguments) {
		trace().traceEntry(option, methodArguments);
	}
	
	@Override
	public void traceExit(String option) {
		trace().traceExit(option);
	}
	
	@Override
	public void traceExit(String option, Object result) {
		
	}
}