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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.agents.annotations.FormMetadata;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Enhances MCP tool schemas with form metadata from @FormMetadata annotations.
 * This adds UI hints to the JSON schema for better user experience.
 */
public class FormSchemaEnhancer {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Enhances a tool's JSON schema with form metadata from annotations.
     *
     * @param method The annotated method
     * @param inputSchema The input schema to enhance
     * @return Enhanced schema with form metadata
     */
    public JsonNode enhanceSchemaWithForm(Method method, JsonNode inputSchema) {
        if (inputSchema == null || !inputSchema.isObject()) {
            return inputSchema;
        }

        ObjectNode schemaObj = (ObjectNode) inputSchema;
        JsonNode properties = schemaObj.get("properties");

        if (properties == null || !properties.isObject()) {
            return inputSchema;
        }

        ObjectNode propertiesObj = (ObjectNode) properties;
        Parameter[] parameters = method.getParameters();

        for (Parameter param : parameters) {
            FormMetadata formMeta = param.getAnnotation(FormMetadata.class);
            if (formMeta != null) {
                String paramName = getParameterName(param);
                JsonNode paramSchema = propertiesObj.get(paramName);

                if (paramSchema != null && paramSchema.isObject()) {
                    ObjectNode paramSchemaObj = (ObjectNode) paramSchema;
                    enhanceParameterSchema(paramSchemaObj, formMeta);
                }
            }
        }

        return schemaObj;
    }

    /**
     * Enhances a single parameter schema with form metadata.
     */
    private void enhanceParameterSchema(ObjectNode paramSchema, FormMetadata formMeta) {
        ObjectNode formNode = mapper.createObjectNode();

        // Add widget type
        if (!formMeta.widget().isEmpty() && !formMeta.widget().equals("text")) {
            formNode.put("widget", formMeta.widget());
        }

        // Add options for select/radio widgets
        if (formMeta.options().length > 0) {
            ArrayNode optionsArray = mapper.createArrayNode();
            for (String option : formMeta.options()) {
                optionsArray.add(option);
            }
            formNode.set("options", optionsArray);
        }

        // Add placeholder
        if (!formMeta.placeholder().isEmpty()) {
            formNode.put("placeholder", formMeta.placeholder());
        }

        // Add min/max for number inputs
        if (formMeta.min() != Double.MIN_VALUE) {
            formNode.put("minimum", formMeta.min());
        }
        if (formMeta.max() != Double.MAX_VALUE) {
            formNode.put("maximum", formMeta.max());
        }

        // Add pattern
        if (!formMeta.pattern().isEmpty()) {
            formNode.put("pattern", formMeta.pattern());
        }

        // Add help text
        if (!formMeta.helpText().isEmpty()) {
            formNode.put("helpText", formMeta.helpText());
        }

        // Only add form node if it has content
        if (formNode.size() > 0) {
            paramSchema.set("form", formNode);
        }
    }

    /**
     * Gets the parameter name from reflection.
     * Note: Requires -parameters compiler flag to get actual parameter names.
     */
    private String getParameterName(Parameter param) {
        // Simply return the parameter name from reflection
        // The -parameters compiler flag must be enabled for this to work
        return param.getName();
    }

    /**
     * Processes all annotated objects and enhances their schemas with form metadata.
     *
     * @param annotatedObjects Objects with @McpTool annotations
     * @return Map of tool names to enhanced schemas
     */
    public Map<String, JsonNode> enhanceAllSchemas(Object[] annotatedObjects) {
        Map<String, JsonNode> enhancedSchemas = new HashMap<>();

        for (Object obj : annotatedObjects) {
            for (Method method : obj.getClass().getDeclaredMethods()) {
                McpTool toolAnnotation = method.getAnnotation(McpTool.class);
                if (toolAnnotation != null) {
                    String toolName = toolAnnotation.name();
                    // Schema enhancement would happen here in integration with tool provider
                    // This is a placeholder for the actual integration point
                }
            }
        }

        return enhancedSchemas;
    }
}
