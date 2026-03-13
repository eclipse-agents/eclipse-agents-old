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
package org.eclipse.agents.test.schema;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.eclipse.agents.annotations.FormMetadata;
import org.eclipse.agents.schema.FormSchemaEnhancer;
import org.junit.Test;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests for form metadata schema enhancement.
 */
public class FormSchemaEnhancerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final FormSchemaEnhancer enhancer = new FormSchemaEnhancer();

    /**
     * Test class with form-annotated methods.
     */
    public static class TestTools {

        @McpTool(name = "testSelectWidget", description = "Test select widget")
        public String testSelectWidget(
                @McpToolParam(description = "Test parameter")
                @FormMetadata(
                    widget = "select",
                    options = {"OPTION1", "OPTION2", "OPTION3"},
                    helpText = "Choose an option"
                )
                String testParam) {
            return testParam;
        }

        @McpTool(name = "testNumberWidget", description = "Test number widget")
        public int testNumberWidget(
                @McpToolParam(description = "Numeric parameter")
                @FormMetadata(
                    widget = "number",
                    min = 0,
                    max = 100,
                    helpText = "Enter a number"
                )
                int value) {
            return value;
        }

        @McpTool(name = "testTextWidget", description = "Test text widget")
        public String testTextWidget(
                @McpToolParam(description = "Text parameter")
                @FormMetadata(
                    widget = "text",
                    placeholder = "Enter text...",
                    pattern = "[A-Za-z]+"
                )
                String text) {
            return text;
        }
    }

    @Test
    public void testSelectWidgetEnhancement() throws Exception {
        Method method = TestTools.class.getMethod("testSelectWidget", String.class);

        // Create a basic schema
        ObjectNode inputSchema = mapper.createObjectNode();
        ObjectNode properties = mapper.createObjectNode();
        ObjectNode testParam = mapper.createObjectNode();
        testParam.put("type", "string");
        testParam.put("description", "Test parameter");
        properties.set("testParam", testParam);
        inputSchema.set("properties", properties);

        // Enhance with form metadata
        JsonNode enhanced = enhancer.enhanceSchemaWithForm(method, inputSchema);

        // Verify form metadata was added
        JsonNode formNode = enhanced.get("properties").get("testParam").get("form");
        assertNotNull("Form node should be present", formNode);
        assertEquals("select", formNode.get("widget").asText());
        assertEquals(3, formNode.get("options").size());
        assertEquals("OPTION1", formNode.get("options").get(0).asText());
        assertEquals("Choose an option", formNode.get("helpText").asText());
    }

    @Test
    public void testNumberWidgetEnhancement() throws Exception {
        Method method = TestTools.class.getMethod("testNumberWidget", int.class);

        // Create a basic schema
        ObjectNode inputSchema = mapper.createObjectNode();
        ObjectNode properties = mapper.createObjectNode();
        ObjectNode valueParam = mapper.createObjectNode();
        valueParam.put("type", "integer");
        valueParam.put("description", "Numeric parameter");
        properties.set("value", valueParam);
        inputSchema.set("properties", properties);

        // Enhance with form metadata
        JsonNode enhanced = enhancer.enhanceSchemaWithForm(method, inputSchema);

        // Verify form metadata was added
        JsonNode formNode = enhanced.get("properties").get("value").get("form");
        assertNotNull("Form node should be present", formNode);
        assertEquals("number", formNode.get("widget").asText());
        assertEquals(0.0, formNode.get("minimum").asDouble(), 0.01);
        assertEquals(100.0, formNode.get("maximum").asDouble(), 0.01);
        assertEquals("Enter a number", formNode.get("helpText").asText());
    }

    @Test
    public void testTextWidgetEnhancement() throws Exception {
        Method method = TestTools.class.getMethod("testTextWidget", String.class);

        // Create a basic schema
        ObjectNode inputSchema = mapper.createObjectNode();
        ObjectNode properties = mapper.createObjectNode();
        ObjectNode textParam = mapper.createObjectNode();
        textParam.put("type", "string");
        textParam.put("description", "Text parameter");
        properties.set("text", textParam);
        inputSchema.set("properties", properties);

        // Enhance with form metadata
        JsonNode enhanced = enhancer.enhanceSchemaWithForm(method, inputSchema);

        // Verify form metadata was added
        JsonNode formNode = enhanced.get("properties").get("text").get("form");
        assertNotNull("Form node should be present", formNode);
        assertEquals("text", formNode.get("widget").asText());
        assertEquals("Enter text...", formNode.get("placeholder").asText());
        assertEquals("[A-Za-z]+", formNode.get("pattern").asText());
    }

    @Test
    public void testNoFormMetadataDoesNotModifySchema() throws Exception {
        // Method without FormMetadata annotation
        Method method = TestTools.class.getMethod("testSelectWidget", String.class);

        // Create a basic schema
        ObjectNode inputSchema = mapper.createObjectNode();
        ObjectNode properties = mapper.createObjectNode();
        ObjectNode plainParam = mapper.createObjectNode();
        plainParam.put("type", "string");
        plainParam.put("description", "Plain parameter");
        properties.set("plainParam", plainParam);
        inputSchema.set("properties", properties);

        // Try to enhance (should find no matching parameter name)
        JsonNode enhanced = enhancer.enhanceSchemaWithForm(method, inputSchema);

        // Verify no form metadata was added to non-existent parameter
        JsonNode formNode = enhanced.get("properties").get("plainParam").get("form");
        assertNull("Form node should not be present for non-matching parameter", formNode);
    }
}
