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

import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import io.modelcontextprotocol.spec.McpError;

/**
 * TODO
 */
public class MCPException extends McpError {

	private static final long serialVersionUID = 1L;

	public MCPException(String message) {
		super(new JSONRPCError(ErrorCodes.INTERNAL_ERROR, message, null));
	}

	public MCPException(Exception e) {
		super(new JSONRPCError(ErrorCodes.INTERNAL_ERROR, e.getLocalizedMessage(), e));
	}
	
	public MCPException(IStatus status) {
		super(new JSONRPCError(ErrorCodes.INTERNAL_ERROR, status.getMessage(), status.getException()));
	}
}
