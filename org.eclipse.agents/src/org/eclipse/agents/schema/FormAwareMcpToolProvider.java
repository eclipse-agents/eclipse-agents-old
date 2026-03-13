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
package org.eclipse.agents.schema;

import java.util.List;

import org.springaicommunity.mcp.provider.tool.SyncMcpToolProvider;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;

/**
 * Enhanced tool provider that adds form support to MCP tools.
 * This wraps the standard SyncMcpToolProvider.
 *
 * Note: Full form metadata enhancement is pending SDK support for
 * custom schema modifications. Currently delegates to standard provider.
 */
public class FormAwareMcpToolProvider {

    private final SyncMcpToolProvider delegate;

    public FormAwareMcpToolProvider(List<Object> annotatedObjects) {
        this.delegate = new SyncMcpToolProvider(annotatedObjects);
    }

    /**
     * Gets tool specifications.
     *
     * @return List of tool specifications
     */
    public List<SyncToolSpecification> getToolSpecifications() {
        // TODO: Add form metadata enhancement when SDK supports custom schema modifications
        // For now, just delegate to standard provider
        return delegate.getToolSpecifications();
    }
}
