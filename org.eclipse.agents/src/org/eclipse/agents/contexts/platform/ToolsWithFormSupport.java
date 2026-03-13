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
package org.eclipse.agents.contexts.platform;

import org.eclipse.agents.MCPException;
import org.eclipse.agents.annotations.FormMetadata;
import org.eclipse.agents.contexts.platform.resource.MarkerAdapter;
import org.eclipse.agents.contexts.platform.resource.ResourceSchema.Problems;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

/**
 * Example of MCP Tools with Form support.
 * This class demonstrates how to use @FormMetadata annotations
 * to provide UI hints for tool parameters.
 */
public class ToolsWithFormSupport {

    /**
     * Example tool showing form support with a select widget for severity.
     */
    @McpTool(
        name = "listProblemsWithForm",
        description = "List Eclipse IDE compilation and configuration problems with form-based severity selection",
        annotations = @McpTool.McpAnnotations(
            title = "List Problems (Form Demo)"
        )
    )
    public Problems listProblemsWithForm(
            @McpToolParam(
                description = "Eclipse workspace file or editor URI"
            )
            @FormMetadata(
                widget = "text",
                placeholder = "eclipse://file/path/to/file",
                helpText = "Enter the URI of a workspace file or editor"
            )
            String resourceURI,

            @McpToolParam(
                description = "Severity level to filter problems",
                required = false
            )
            @FormMetadata(
                widget = "select",
                options = {"ERROR", "WARNING", "INFO"},
                helpText = "Select the minimum severity level for problems to display"
            )
            String severity) {

        // TODO: Implement severity filtering

        if (resourceURI == null || resourceURI.isEmpty()) {
            return MarkerAdapter.getProblems(ResourcesPlugin.getWorkspace().getRoot());
        } else {
            throw new MCPException("Resource URI resolution not implemented in this example");
        }
    }

    /**
     * Example tool showing numeric input with min/max constraints.
     */
    @McpTool(
        name = "setEditorLineLimit",
        description = "Configure editor line limit with numeric constraints",
        annotations = @McpTool.McpAnnotations(
            title = "Set Editor Line Limit"
        )
    )
    public boolean setEditorLineLimit(
            @McpToolParam(
                description = "Maximum number of lines to display"
            )
            @FormMetadata(
                widget = "number",
                min = 10,
                max = 10000,
                helpText = "Enter a value between 10 and 10,000"
            )
            int maxLines) {

        // Implementation would go here
        return true;
    }

    /**
     * Example tool showing textarea widget for multi-line input.
     */
    @McpTool(
        name = "addMultiLineComment",
        description = "Add a multi-line comment to a file",
        annotations = @McpTool.McpAnnotations(
            title = "Add Multi-line Comment"
        )
    )
    public boolean addMultiLineComment(
            @McpToolParam(
                description = "File path"
            )
            @FormMetadata(
                widget = "text",
                placeholder = "/path/to/file.java"
            )
            String filePath,

            @McpToolParam(
                description = "Comment text"
            )
            @FormMetadata(
                widget = "textarea",
                placeholder = "Enter your comment here...",
                helpText = "Multi-line comments are supported"
            )
            String comment) {

        // Implementation would go here
        return true;
    }

    /**
     * Example tool showing checkbox for boolean options.
     */
    @McpTool(
        name = "configureEditor",
        description = "Configure editor options",
        annotations = @McpTool.McpAnnotations(
            title = "Configure Editor"
        )
    )
    public boolean configureEditor(
            @McpToolParam(
                description = "Enable line numbers",
                required = false
            )
            @FormMetadata(
                widget = "checkbox",
                helpText = "Show or hide line numbers in the editor"
            )
            boolean showLineNumbers,

            @McpToolParam(
                description = "Enable auto-save",
                required = false
            )
            @FormMetadata(
                widget = "checkbox",
                helpText = "Automatically save files after changes"
            )
            boolean autoSave) {

        // Implementation would go here
        return true;
    }
}
