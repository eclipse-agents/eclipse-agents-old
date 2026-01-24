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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Utility class for logging to Eclipse's Error Log view.
 * 
 * This class provides convenient methods for logging errors, warnings, and info messages
 * that will appear in Window → Show View → Error Log.
 * 
 * For debug/trace logging, use {@link Tracer} instead.
 */
public class LogHelper {
	
	private static final String PLUGIN_ID = Activator.PLUGIN_ID;
	
	/**
	 * Logs an error message with an exception to the Eclipse Error Log.
	 * 
	 * @param message the error message
	 * @param exception the exception that caused the error (can be null)
	 */
	public static void logError(String message, Throwable exception) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, exception);
		Activator.getDefault().getLog().log(status);
	}
	
	/**
	 * Logs an error message to the Eclipse Error Log.
	 * 
	 * @param message the error message
	 */
	public static void logError(String message) {
		logError(message, null);
	}
	
	/**
	 * Logs a warning message to the Eclipse Error Log.
	 * 
	 * @param message the warning message
	 */
	public static void logWarning(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message);
		Activator.getDefault().getLog().log(status);
	}
	
	/**
	 * Logs a warning message with an exception to the Eclipse Error Log.
	 * 
	 * @param message the warning message
	 * @param exception the exception (can be null)
	 */
	public static void logWarning(String message, Throwable exception) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message, exception);
		Activator.getDefault().getLog().log(status);
	}
	
	/**
	 * Logs an informational message to the Eclipse Error Log.
	 * 
	 * @param message the informational message
	 */
	public static void logInfo(String message) {
		IStatus status = new Status(IStatus.INFO, PLUGIN_ID, message);
		Activator.getDefault().getLog().log(status);
	}
	
	/**
	 * Checks if debug tracing is enabled for ACP protocol.
	 * 
	 * @return true if debug tracing is enabled
	 */
	public static boolean isDebugEnabled() {
		// Using the existing Tracer infrastructure
		return Tracer.trace() != null && !Tracer.disableTracing;
	}
	
	/**
	 * Logs a debug message using the Tracer infrastructure.
	 * Only logs if debug tracing is enabled.
	 * 
	 * @param option the trace option (use constants from Tracer class)
	 * @param message the debug message
	 */
	public static void logDebug(String option, String message) {
		Tracer.trace().trace(option, message);
	}
	
	/**
	 * Logs a debug message with exception using the Tracer infrastructure.
	 * Only logs if debug tracing is enabled.
	 * 
	 * @param option the trace option (use constants from Tracer class)
	 * @param message the debug message
	 * @param exception the exception
	 */
	public static void logDebug(String option, String message, Throwable exception) {
		Tracer.trace().trace(option, message, exception);
	}
}
