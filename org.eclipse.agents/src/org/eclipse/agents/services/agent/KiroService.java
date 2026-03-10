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

import java.io.File;
import java.io.IOException;

import org.eclipse.agents.Tracer;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Agent service implementation for Kiro CLI.
 * Kiro is an AI coding assistant that communicates via the Agent Client Protocol (ACP).
 * Unlike GeminiService, KiroService does not manage installation or updates since
 * Kiro CLI is installed and managed externally.
 */
public class KiroService extends AbstractService {

    @Override
    public String getName() {
        return "Kiro";
    }

    @Override
    public String getId() {
        return "kiro";
    }

    @Override
    public String getFolderName() {
        return "kiro";
    }

    @Override
    public String[] getDefaultStartupCommand() {
        return new String[] { getKiroCliPath(), "acp" };
    }

    /**
     * Returns the default path to the Kiro CLI executable.
     * Uses the standard installation location based on the operating system:
     * - Windows: %USERPROFILE%\.kiro\bin\kiro-cli.exe
     * - macOS/Linux: ~/.local/bin/kiro-cli
     */
    private String getKiroCliPath() {
        String userHome = System.getProperty("user.home");
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        
        if (isWindows) {
            return userHome + File.separator + ".kiro" + File.separator + "bin" + File.separator + "kiro-cli.exe";
        } else {
            return userHome + "/.local/bin/kiro-cli";
        }
    }

    @Override
    public Process createProcess() throws IOException {
        String[] startup = getStartupCommand();
        Tracer.trace().trace(Tracer.ACP, String.join(", ", startup));
        ProcessBuilder pb = new ProcessBuilder(startup);
        return pb.start();
    }

    @Override
    public void checkForUpdates(IProgressMonitor monitor) throws IOException {
        // Kiro CLI is externally managed - no automatic updates required
    }
}
