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
package org.eclipse.agents.services.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.eclipse.agents.Tracer;
import org.eclipse.agents.chat.EnableMCPDialog;
import org.eclipse.agents.preferences.IPreferenceConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.agents.Activator;

/**
 * Service for integrating Claude Code using the Agent Client Protocol (ACP).
 * 
 * Claude Code ACP implementation: https://github.com/zed-industries/claude-code-acp
 * Install via npm: npm install @zed-industries/claude-code-acp
 * Run with: ANTHROPIC_API_KEY=sk-... claude-code-acp
 * 
 * This service manages the lifecycle of Claude Code ACP instances and handles
 * integration with Eclipse IDE over the Agent Client Protocol.
 */
public class ClaudeService extends AbstractService implements IPreferenceConstants {

    public ClaudeService() {
        // no-op constructor; do not force additional runtime dependencies here
    }

    @Override
    public String getName() {
        return "Claude Code ACP";
    }

    @Override
    public String getFolderName() {
        return "claude-code-acp";
    }

    @Override
    public String getId() {
        return "claude-code-acp";
    }

    @Override
    public void checkForUpdates(IProgressMonitor monitor) throws IOException {
        String startupDefault[] = getDefaultStartupCommand();
        String startup[] = getStartupCommand();

        if (Arrays.equals(startupDefault, startup)) {

            // If user has not customized the startup command, attempt to update the
            // installed Claude CLI using its updater command.

            monitor.subTask("Checking Claude Code ACP");

            // claude-code-acp version is managed via npm; no separate update needed here
            ProcessResult result = new ProcessResult();
            result.result = 0;

            if (Activator.getDefault().getPreferenceStore().getBoolean(P_ACP_PROMPT4MCP)) {
                if (!Activator.getDefault().getPreferenceStore().getBoolean(P_MCP_SERVER_ENABLED)) {
                    Activator.getDisplay().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            EnableMCPDialog dialog = new EnableMCPDialog(Activator.getDisplay().getActiveShell());
                            dialog.open();
                        }
                    });
                }
            }

            if (Activator.getDefault().getPreferenceStore().getBoolean(P_MCP_SERVER_ENABLED)) {

                String url = getMCPUrl();
                String name = getMCPName();

                boolean foundUrl = false;
                boolean foundName = false;

                monitor.subTask("Listing MCPs");

                ProcessResult listMCP = super.runProcess(listMCPCommand());
                String mcpLine = null;

                for (String line : listMCP.inputLines) {
                    if (line.contains(name)) {
                        foundName = true;
                    }
                    if (line.contains(url)) {
                        foundUrl = true;
                        mcpLine = line;
                    }
                }

                if (!foundUrl && foundName) {
                    monitor.subTask("Removing 'eclipse-ide MCP");
                    super.runProcess(removeMCPCommand());

                }

                if (!foundUrl) {
                    monitor.subTask("Adding 'eclipse-ide MCP");
                    super.runProcess(addMCPCommand());

                    monitor.subTask("Validating 'eclipse-ide' MCP");
                    listMCP = super.runProcess(listMCPCommand());

                    for (String line : listMCP.inputLines) {
                        if (line.contains(name)) {
                            foundName = true;
                        }
                        if (line.contains(url)) {
                            foundUrl = true;
                            mcpLine = line;
                        }
                    }

                    if (!foundName && !foundUrl) {
                        System.err.println("Failed to configure Claude CLI to use Eclipse IDE MCP");
                    }
                }

                if (mcpLine != null && mcpLine.contains("✗")) {
                    System.err.println(mcpLine);
                }
            }
        }
    }

    @Override
    public Process createProcess() throws IOException {
        String startup[] = getStartupCommand();

        Tracer.trace().trace(Tracer.ACP, String.join(", ", startup));

        ProcessBuilder pb = new ProcessBuilder(startup);
        Process process = pb.start();

        return process;
    }

    @Override
    public String[] getDefaultStartupCommand() {
        // Default to 'claude-code-acp' npm package; users can customize via preferences
        // Requires ANTHROPIC_API_KEY environment variable to be set
        return new String[] {"claude-code-acp"};
    }

    private String[] listMCPCommand() {
        return new String[] { "claude-code-acp", "mcp", "list" };
    }

    private String[] addMCPCommand() {
        return new String[] { "claude-code-acp", "mcp", "add", "--transport", "sse", getMCPName(), getMCPUrl() };
    }

    private String[] removeMCPCommand() {
        return new String[] { "claude-code-acp", "mcp", "remove", getMCPName() };
    }

    public String getVersion() {
        try {
            ProcessResult result = super.runProcess(new String[] { "claude-code-acp", "--version" });
            if (result.result == 0 && !result.inputLines.isEmpty()) {
                return result.inputLines.get(0);
            }
        } catch (Exception e) {
            Tracer.trace().trace(Tracer.ACP, "Error getting Claude version", e);
        }
        return "Not found";

    }

    private String getMCPName() {
        return "eclipse-ide";
    }

    private String getMCPUrl() {
        return "http://localhost:"
                + Activator.getDefault().getPreferenceStore().getString(P_MCP_SERVER_HTTP_PORT)
                + "/sse";
    }

}
