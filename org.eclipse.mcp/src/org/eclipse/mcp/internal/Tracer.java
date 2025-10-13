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
package org.eclipse.mcp.internal;

import java.util.Hashtable;

import org.eclipse.mcp.Activator;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;


public class Tracer implements DebugOptionsListener, DebugTrace {


	public static final String DEBUG = "/debug";
	public static final String EXTENTIONS = "/debug/extensions";
	public static final String PLATFORM = "/debug/extensions/platform";
	public static final String ACP = "/debug/acp";
	public static final String OTHERS = "/debug/extensions/others";
	
	public enum OPTION {
		DEBUG("/debug"),
		EXTENTIONS("/debug/extensions"),
		PLATFORM("/debug/extensions/platform"),
		ACP("/debug/acp"),
		OTHERS("org.eclipse.mcp/debug/extensions/others");
		
		private String location;
		private String fullPath;
		private boolean isActive;
		
	    private OPTION(String location) {
	    	this.location = location;
	    	fullPath = Activator.PLUGIN_ID + location;
	    	isActive = false;
	    }

		public boolean isActive() {
			return isActive;
		}

		private void setActive(boolean isActive) {
			this.isActive = isActive;
		}

		public String getLocation() {
			return location;
		}

		public String getFullPath() {
			return fullPath;
		}

		@Override
		public String toString() {
			return getLocation();
		}
	}
	
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

		for (OPTION level: OPTION.values()) {
			level.setActive(options.getBooleanOption(level.getFullPath(), false));
		}
		if (trace == null) {
			trace = options.newDebugTrace(Activator.PLUGIN_ID);
		}
		trace.trace(OPTION.DEBUG.getLocation(), toString());
} 

	// used to bypass eclipse dependencies during unit testing
	public static boolean disableTracing = false;	

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (OPTION level: OPTION.values()) {
			sb.append(level.getLocation() + ": " + level.isActive() + "; ");
		}
		return sb.toString();
		
	}
	
	public static DebugTrace trace() {
		if (trace == null || disableTracing) {
			return nullTrace;
		}
		return trace;
	}


	@Override
	public void trace(String option, String message) {
		trace().trace(option, message);
		Activator.getDefault().getServerManager().log(message, null);
	}
	@Override
	public void trace(String option, String message, Throwable error) {
		trace().trace(option, message, error);
		Activator.getDefault().getServerManager().log(message, error);
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