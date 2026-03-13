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
package org.eclipse.agents.contexts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.McpJsonMapperSupplier;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Custom McpJsonMapperSupplier that provides a JacksonMcpJsonMapper configured
 * to ignore unknown JSON properties. This fixes compatibility with VS Code's
 * MCP client, which sends a "form" field in ClientCapabilities.Elicitation
 * that the current SDK doesn't recognize.
 *
 * This is registered via Java SPI (ServiceLoader) in:
 * META-INF/services/io.modelcontextprotocol.json.McpJsonMapperSupplier
 */
public class LenientMcpJsonMapperSupplier implements McpJsonMapperSupplier {

    /**
     * MixIn to tell Jackson to ignore unknown properties on any class it's applied to.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static abstract class IgnoreUnknownMixIn {
    }

    @Override
    public McpJsonMapper get() {
        // Create a lenient ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Register JavaTimeModule for JSR-310 date/time types
        try {
            objectMapper.registerModule(new JavaTimeModule());
        } catch (Exception e) {
            System.err.println("LenientMcpJsonMapperSupplier: Could not register JavaTimeModule: " + e.getMessage());
        }

        // Add MixIn annotations for known problematic classes
        try {
            objectMapper.addMixIn(McpSchema.ClientCapabilities.class, IgnoreUnknownMixIn.class);
        } catch (Exception e) { /* skip */ }
        try {
            objectMapper.addMixIn(McpSchema.ClientCapabilities.Elicitation.class, IgnoreUnknownMixIn.class);
        } catch (Exception e) { /* skip */ }
        try {
            objectMapper.addMixIn(McpSchema.ClientCapabilities.Sampling.class, IgnoreUnknownMixIn.class);
        } catch (Exception e) { /* skip */ }
        try {
            objectMapper.addMixIn(McpSchema.ClientCapabilities.RootCapabilities.class, IgnoreUnknownMixIn.class);
        } catch (Exception e) { /* skip */ }
        try {
            objectMapper.addMixIn(McpSchema.InitializeRequest.class, IgnoreUnknownMixIn.class);
        } catch (Exception e) { /* skip */ }

        System.err.println("LenientMcpJsonMapperSupplier: Created lenient JacksonMcpJsonMapper (FAIL_ON_UNKNOWN_PROPERTIES=false)");

        return new JacksonMcpJsonMapper(objectMapper);
    }
}
