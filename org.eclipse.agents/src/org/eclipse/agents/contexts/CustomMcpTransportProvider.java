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

import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Utility class to create a legacy SSE MCP transport provider with a
 * lenient ObjectMapper that ignores unknown JSON properties.
 *
 * Uses HttpServletSseServerTransportProvider (legacy SSE) which is compatible
 * with Eclipse's bundled Jetty version. VS Code clients will first try
 * Streamable HTTP (POST), get a 404, then automatically fall back to
 * legacy SSE which works correctly.
 */
public class CustomMcpTransportProvider {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static abstract class IgnoreUnknownPropertiesMixIn {
    }

    /**
     * Creates an HttpServletSseServerTransportProvider with a lenient
     * ObjectMapper configured to ignore unknown properties.
     */
    public static HttpServletSseServerTransportProvider create(String messageEndpoint, String sseEndpoint) {
        ObjectMapper lenientMapper = createLenientObjectMapper();

        HttpServletSseServerTransportProvider provider =
                HttpServletSseServerTransportProvider.builder()
                        .jsonMapper(new JacksonMcpJsonMapper(lenientMapper))
                        .messageEndpoint(messageEndpoint)
                        .sseEndpoint(sseEndpoint)
                        .build();

        System.err.println("CustomMcpTransportProvider: Created SSE transport with lenient ObjectMapper.");
        return provider;
    }

    private static ObjectMapper createLenientObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            mapper.registerModule(new JavaTimeModule());
        } catch (Exception e) {
            System.err.println("CustomMcpTransportProvider: Could not register JavaTimeModule: " + e.getMessage());
        }

        // Add MixIn annotations for known problematic classes
        try { mapper.addMixIn(McpSchema.ClientCapabilities.class, IgnoreUnknownPropertiesMixIn.class); } catch (Exception e) { /* skip */ }
        try { mapper.addMixIn(McpSchema.ClientCapabilities.Elicitation.class, IgnoreUnknownPropertiesMixIn.class); } catch (Exception e) { /* skip */ }
        try { mapper.addMixIn(McpSchema.ClientCapabilities.Sampling.class, IgnoreUnknownPropertiesMixIn.class); } catch (Exception e) { /* skip */ }
        try { mapper.addMixIn(McpSchema.ClientCapabilities.RootCapabilities.class, IgnoreUnknownPropertiesMixIn.class); } catch (Exception e) { /* skip */ }
        try { mapper.addMixIn(McpSchema.InitializeRequest.class, IgnoreUnknownPropertiesMixIn.class); } catch (Exception e) { /* skip */ }

        return mapper;
    }
}
