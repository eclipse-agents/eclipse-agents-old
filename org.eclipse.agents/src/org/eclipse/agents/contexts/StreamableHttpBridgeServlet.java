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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementing the MCP Streamable HTTP transport protocol.
 *
 * Eclipse Copilot uses Streamable HTTP (POST with JSON-RPC body) rather
 * than legacy SSE. The SDK's HttpServletStreamableServerTransportProvider
 * requires a newer Jetty than Eclipse 4.37 bundles.
 *
 * This servlet directly handles JSON-RPC requests using the tool/resource
 * specifications from MCPServer, providing a lightweight Streamable HTTP
 * endpoint that works with Eclipse's bundled Jetty.
 *
 * Registered at /mcp alongside the SSE transport at /* so:
 * - Eclipse Copilot: POST /mcp (Streamable HTTP)
 * - VS Code: GET /sse + POST / (legacy SSE)
 */
public class StreamableHttpBridgeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Use the SDK's own ObjectMapper obtained from the MCPServer so that
    // McpSchema.Tool and its inputSchema are serialized with the correct
    // Jackson configuration (module registrations, property names etc.)
    private ObjectMapper objectMapper;
    private final MCPServer mcpServer;

    public StreamableHttpBridgeServlet(MCPServer mcpServer) {
        this.mcpServer = mcpServer;
        // Configure ObjectMapper the same way as LenientMcpJsonMapperSupplier so
        // McpSchema types (Tool, inputSchema etc.) serialize with correct field names.
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try { this.objectMapper.registerModule(new JavaTimeModule()); } catch (Exception e) { /* skip */ }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        String requestBody = body.toString();
        System.err.println("StreamableHttpBridge: POST " + requestBody);

        try {
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            String method = jsonNode.has("method") ? jsonNode.get("method").asText() : null;
            JsonNode idNode = jsonNode.get("id");
            JsonNode params = jsonNode.get("params");

            if ("initialize".equals(method)) {
                handleInitialize(idNode, resp);
            } else if ("notifications/initialized".equals(method)) {
                // Notification — respond with 200 and empty JSON body.
                // Some clients (GitHub Copilot LS) require a well-formed HTTP 200
                // response even for notifications, otherwise they treat the
                // connection as broken and save invalid MCP state.
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write("{}");
                resp.getWriter().flush();
            } else if ("tools/list".equals(method)) {
                handleToolsList(idNode, resp);
            } else if ("tools/call".equals(method)) {
                handleToolsCall(params, idNode, resp);
            } else if ("resources/list".equals(method)) {
                handleResourcesList(idNode, resp);
            } else if ("ping".equals(method)) {
                sendResult(resp, idNode, objectMapper.createObjectNode());
            } else {
                sendError(resp, idNode, -32601, "Method not found: " + method);
            }
        } catch (Exception e) {
            System.err.println("StreamableHttpBridge: Error: " + e.getMessage());
            e.printStackTrace();
            sendError(resp, null, -32603, "Internal error: " + e.getMessage());
        }
    }

    private void handleInitialize(JsonNode idNode, HttpServletResponse resp) throws IOException {
        String sessionId = UUID.randomUUID().toString();

        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", "2024-11-05");

        ObjectNode serverInfo = objectMapper.createObjectNode();
        serverInfo.put("name", mcpServer.name);
        serverInfo.put("version", mcpServer.version);
        result.set("serverInfo", serverInfo);

        ObjectNode capabilities = objectMapper.createObjectNode();
        ObjectNode toolsCap = objectMapper.createObjectNode();
        toolsCap.put("listChanged", true);
        capabilities.set("tools", toolsCap);
        ObjectNode resourcesCap = objectMapper.createObjectNode();
        resourcesCap.put("subscribe", true);
        resourcesCap.put("listChanged", true);
        capabilities.set("resources", resourcesCap);
        capabilities.set("logging", objectMapper.createObjectNode());
        result.set("capabilities", capabilities);

        // instructions field expected by GitHub Copilot LS
        result.put("instructions", "");

        resp.setHeader("Mcp-Session-Id", sessionId);
        sendResult(resp, idNode, result);
    }

    private void handleToolsList(JsonNode idNode, HttpServletResponse resp) throws IOException {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode toolsArray = objectMapper.createArrayNode();

        List<SyncToolSpecification> tools = mcpServer.tools;
        System.err.println("StreamableHttpBridge: tools/list - mcpServer.tools size: "
                + (tools != null ? tools.size() : "null"));

        if (tools != null) {
            for (SyncToolSpecification spec : tools) {
                if (mcpServer.removedTools == null || !mcpServer.removedTools.contains(spec)) {
                    McpSchema.Tool tool = spec.tool();
                    // Manually construct tool JSON to ensure correct field names and structure
                    ObjectNode toolNode = objectMapper.createObjectNode();
                    toolNode.put("name", tool.name());
                    if (tool.description() != null) {
                        toolNode.put("description", tool.description());
                    }
                    // inputSchema is required by MCP spec — must be a valid JSON Schema.
                    // GitHub Copilot LS strictly validates this: missing 'type' or
                    // 'properties' causes McpAutoApproveService to reject the entire
                    // tool list and reset config to empty.
                    ObjectNode inputSchema = objectMapper.createObjectNode();
                    inputSchema.put("type", "object");
                    if (tool.inputSchema() != null) {
                        try {
                            String schemaJson = objectMapper.writeValueAsString(tool.inputSchema());
                            JsonNode parsedSchema = objectMapper.readTree(schemaJson);
                            // Merge all fields from the generated schema
                            parsedSchema.fields().forEachRemaining(entry ->
                                inputSchema.set(entry.getKey(), entry.getValue()));
                        } catch (Exception e) {
                            // keep the minimal schema below
                        }
                    }
                    // Always ensure 'properties' exists (required by Copilot LS schema validation)
                    if (!inputSchema.has("properties")) {
                        inputSchema.set("properties", objectMapper.createObjectNode());
                    }
                    toolNode.set("inputSchema", inputSchema);
                    System.err.println("StreamableHttpBridge:   tool: " + tool.name());
                    toolsArray.add(toolNode);
                }
            }
        }

        result.set("tools", toolsArray);
        System.err.println("StreamableHttpBridge: tools/list response: " + result);
        sendResult(resp, idNode, result);
    }

    private void handleToolsCall(JsonNode params, JsonNode idNode, HttpServletResponse resp) throws IOException {
        try {
            String toolName = params.get("name").asText();
            JsonNode arguments = params.get("arguments");

            // Convert arguments to Map
            @SuppressWarnings("unchecked")
            Map<String, Object> argsMap = arguments != null
                    ? objectMapper.convertValue(arguments, Map.class)
                    : Map.of();

            // Route tool call through the McpSyncServer's own transport handler.
            // This avoids calling spec.call() directly which can be null when OSGi
            // classloader split prevents SyncMcpToolMethodCallback from being built.
            McpSchema.CallToolResult callResult = mcpServer.callTool(toolName, argsMap);

            JsonNode resultNode = objectMapper.valueToTree(callResult);
            sendResult(resp, idNode, resultNode);

        } catch (Exception e) {
            System.err.println("StreamableHttpBridge: Tool call error: " + e.getMessage());
            e.printStackTrace();
            sendError(resp, idNode, -32603, "Error calling tool: " + e.getMessage());
        }
    }

    private void handleResourcesList(JsonNode idNode, HttpServletResponse resp) throws IOException {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode resourcesArray = objectMapper.createArrayNode();

        if (mcpServer.resources != null) {
            for (SyncResourceSpecification spec : mcpServer.resources) {
                JsonNode resNode = objectMapper.valueToTree(spec.resource());
                resourcesArray.add(resNode);
            }
        }

        result.set("resources", resourcesArray);
        sendResult(resp, idNode, result);
    }

    private void sendResult(HttpServletResponse resp, JsonNode idNode, JsonNode result) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (idNode != null) {
            response.set("id", idNode);
        }
        response.set("result", result);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        PrintWriter writer = resp.getWriter();
        writer.write(objectMapper.writeValueAsString(response));
        writer.flush();
    }

    private void sendError(HttpServletResponse resp, JsonNode idNode, int code, String message) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (idNode != null) {
            response.set("id", idNode);
        }
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        response.set("error", error);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        PrintWriter writer = resp.getWriter();
        writer.write(objectMapper.writeValueAsString(response));
        writer.flush();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // MCP Streamable HTTP spec: GET /mcp opens an SSE stream for server->client
        // messages. We return a minimal SSE stream that stays open, allowing
        // Eclipse Copilot's MCP client to complete the handshake.
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");
        resp.setHeader("X-Accel-Buffering", "no");
        // Keep the stream open - just flush so headers are sent
        resp.flushBuffer();
        // The stream stays open until the client disconnects or times out.
        // We don't need to send anything here since all responses go back
        // via the POST channel in stateless Streamable HTTP mode.
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
