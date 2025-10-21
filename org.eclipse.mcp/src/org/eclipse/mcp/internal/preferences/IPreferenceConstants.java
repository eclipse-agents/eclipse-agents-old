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
package org.eclipse.mcp.internal.preferences;

import org.eclipse.mcp.Activator;

public interface IPreferenceConstants {

	public static final String P_SERVER_ENABLED = Activator.PLUGIN_ID + ".default.server.enabled"; //$NON-NLS-1$

	public static final String P_SERVER_HTTP_PORT = Activator.PLUGIN_ID + ".default.server.http.port"; //$NON-NLS-1$

}
