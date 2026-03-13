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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.agents.Activator;
import org.eclipse.agents.Tracer;
import org.eclipse.agents.preferences.IPreferenceConstants;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * ACP agent service for the GitHub Copilot CLI.
 *
 * <p>Three launch modes are supported:
 * <ol>
 *   <li><b>GitHub CLI mode (default):</b> {@code gh copilot acp}</li>
 *   <li><b>Direct binary mode:</b> {@code <gh-copilot-binary> acp}<br>
 *       Set {@code P_ACP_COPILOT_DIRECT_PATH} to the gh-copilot extension binary path.</li>
 *   <li><b>npm package mode:</b> {@code copilot --acp}<br>
 *       Requires {@code npm install -g @github/copilot}.
 *       Set {@code P_ACP_COPILOT_NPM_PATH} to {@code "copilot"} (or the full path).</li>
 * </ol>
 */
public class CopilotService extends AbstractService implements IPreferenceConstants {

	public CopilotService() {
	}

	@Override
	public String getName() { return "GitHub Copilot CLI"; }

	@Override
	public String getId() { return "copilot"; }

	@Override
	public String getFolderName() { return "copilot"; }

	// -------------------------------------------------------------------------
	// OS helper
	// -------------------------------------------------------------------------

	private static boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase().contains("win");
	}

	/**
	 * Builds a command array, wrapping with {@code cmd /c} on Windows so that
	 * {@code .cmd} and {@code .bat} wrappers installed by npm are found and
	 * executed correctly by {@link ProcessBuilder}.
	 */
	static String[] buildCommand(String binary, String... args) {
		if (isWindows()) {
			String[] cmd = new String[3 + args.length]; // cmd /c binary ...args
			cmd[0] = "cmd"; cmd[1] = "/c"; cmd[2] = binary;
			System.arraycopy(args, 0, cmd, 3, args.length);
			return cmd;
		}
		String[] cmd = new String[1 + args.length];
		cmd[0] = binary;
		System.arraycopy(args, 0, cmd, 1, args.length);
		return cmd;
	}

	// -------------------------------------------------------------------------
	// Default startup command
	// -------------------------------------------------------------------------

	@Override
	public String[] getDefaultStartupCommand() {
		return new String[] { "gh", "copilot", "acp" };
	}

	// -------------------------------------------------------------------------
	// Direct-binary auto-detection (gh-copilot extension binary)
	// -------------------------------------------------------------------------

	/**
	 * Tries to locate the {@code gh-copilot} extension binary in the standard
	 * installation directories used by the GitHub CLI extension system.
	 *
	 * @return the binary {@link File}, or {@code null} when not found
	 */
	public static File findGhCopilotExtensionBinary() {
		String os   = System.getProperty("os.name", "").toLowerCase();
		String arch = System.getProperty("os.arch", "").toLowerCase();

		List<String> dirs  = new ArrayList<>();
		List<String> names = new ArrayList<>();

		if (os.contains("win")) {
			String archSuffix = (arch.contains("aarch64") || arch.contains("arm")) ? "arm64" : "amd64";
			String appdata = System.getenv("APPDATA");
			if (appdata != null) {
				dirs.add(appdata + "\\GitHub CLI\\extensions\\gh-copilot\\");
			}
			dirs.add(System.getProperty("user.home") + "\\.local\\share\\gh\\extensions\\gh-copilot\\");
			names.add("gh-copilot_windows_" + archSuffix + ".exe");
			names.add("gh-copilot.exe");
		} else if (os.contains("mac") || os.contains("darwin")) {
			String archSuffix = (arch.contains("aarch64") || arch.contains("arm")) ? "arm64" : "amd64";
			dirs.add(System.getProperty("user.home") + "/.local/share/gh/extensions/gh-copilot/");
			names.add("gh-copilot_darwin_" + archSuffix);
			names.add("gh-copilot");
		} else {
			String archSuffix = (arch.contains("aarch64") || arch.contains("arm")) ? "arm64" : "amd64";
			dirs.add(System.getProperty("user.home") + "/.local/share/gh/extensions/gh-copilot/");
			names.add("gh-copilot_linux_" + archSuffix);
			names.add("gh-copilot");
		}

		for (String dir : dirs) {
			for (String name : names) {
				File candidate = new File(dir + name);
				if (candidate.isFile() && candidate.canExecute()) {
					return candidate;
				}
			}
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// npm copilot detection (@github/copilot)
	// -------------------------------------------------------------------------

	/**
	 * Checks whether the {@code copilot} command installed by
	 * {@code npm install -g @github/copilot} is available on the PATH.
	 *
	 * @return {@code "copilot"} if found on PATH, otherwise {@code null}
	 */
	public static String findNpmCopilotOnPath() {
		try {
			String[] whereCmd = isWindows()
					? new String[] { "cmd", "/c", "where", "copilot" }
					: new String[] { "which", "copilot" };
			ProcessBuilder pb = new ProcessBuilder(whereCmd);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			int exitCode = p.waitFor();
			if (exitCode == 0) {
				return "copilot";
			}
		} catch (Exception e) {
			// not found
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// Startup command – npm > direct-binary > gh CLI priority
	// -------------------------------------------------------------------------

	/**
	 * Returns the effective startup command applying mode priority:
	 * npm mode &gt; direct-binary mode &gt; GitHub CLI mode.
	 */
	@Override
	public String[] getStartupCommand() {
		// Priority 1: npm package mode  (copilot --acp)
		String npmPath = resolveNpmCopilotPath();
		if (npmPath != null) {
			return buildCommand(npmPath, "--acp");
		}
		// Priority 2: direct gh-copilot extension binary  (binary acp)
		String directPath = resolveDirectBinaryPath();
		if (directPath != null) {
			return new String[] { directPath, "acp" };
		}
		// Priority 3: GitHub CLI mode
		return super.getStartupCommand();
	}

	// -------------------------------------------------------------------------
	// Install / update
	// -------------------------------------------------------------------------

	@Override
	public void checkForUpdates(IProgressMonitor monitor) throws IOException {

		// --- npm package mode --------------------------------------------------
		String npmPath = resolveNpmCopilotPath();
		if (npmPath != null) {
			monitor.subTask("Checking @github/copilot npm package");
			ProcessResult ver = runProcess(buildCommand(npmPath, "--version"));
			if (ver.result != 0) {
				throw new RuntimeException(
						"The 'copilot' command was not found (configured as \"" + npmPath + "\").\n"
						+ "Please run the following command and then restart Eclipse:\n"
						+ "  npm install -g @github/copilot");
			}
			String versionLine = ver.inputLines.isEmpty() ? "" : ver.inputLines.get(0);
			Tracer.trace().trace(Tracer.ACP, "npm copilot ready: " + versionLine);
			return;
		}

		// --- Direct gh-copilot binary mode ------------------------------------
		String directPath = resolveDirectBinaryPath();
		if (directPath != null) {
			monitor.subTask("Checking gh-copilot binary");
			File binary = new File(directPath);
			if (!binary.isFile()) {
				throw new RuntimeException(
						"The configured gh-copilot binary was not found at:\n  " + directPath + "\n"
						+ "Please update the path in the agent preferences (Agents > GitHub Copilot CLI).");
			}
			Tracer.trace().trace(Tracer.ACP, "Using gh-copilot binary directly: " + directPath);
			return;
		}

		// --- GitHub CLI mode --------------------------------------------------
		String[] startup        = super.getStartupCommand();
		String[] startupDefault = getDefaultStartupCommand();

		boolean isDefault = startup.length == startupDefault.length;
		if (isDefault) {
			for (int i = 0; i < startup.length; i++) {
				if (!startup[i].equals(startupDefault[i])) {
					isDefault = false;
					break;
				}
			}
		}
		if (!isDefault) {
			return; // custom command – skip auto-install
		}

		monitor.subTask("Checking gh CLI");
		String gh = resolveGh();
		ProcessResult ghCheck = runProcess(new String[] { gh, "--version" });
		if (ghCheck.result != 0) {
			throw new RuntimeException(
					"GitHub CLI (gh) not found. "
					+ "Please install it from https://cli.github.com/ and re-run.\n\n"
					+ "Alternatively, install the npm package instead:\n"
					+ "  npm install -g @github/copilot\n"
					+ "then enable 'npm package mode' in Preferences > Agents > GitHub Copilot CLI.");
		}

		boolean isGhCli = false;
		for (String line : ghCheck.inputLines) {
			if (line != null && line.startsWith("gh version")) {
				isGhCli = true;
				break;
			}
		}
		if (!isGhCli) {
			throw new RuntimeException(
					"The configured 'gh' executable (\"" + gh + "\") does not appear to be "
					+ "the GitHub CLI.\n"
					+ "Please install the GitHub CLI from https://cli.github.com/.\n\n"
					+ "Alternatively, install the npm package:\n"
					+ "  npm install -g @github/copilot\n"
					+ "then enable 'npm package mode' in Preferences > Agents > GitHub Copilot CLI.");
		}

		monitor.subTask("Upgrading GitHub Copilot CLI extension");
		ProcessResult upgrade = runProcess(new String[] { gh, "extension", "upgrade", "gh-copilot" });

		if (upgrade.result != 0) {
			monitor.subTask("Installing GitHub Copilot CLI extension");
			ProcessResult install = runProcess(
					new String[] { gh, "extension", "install", "github/gh-copilot" });
			if (install.result != 0) {
				String errorText = String.join("\n", install.errorLines).toLowerCase();
				if (errorText.contains("already installed")) {
					Tracer.trace().trace(Tracer.ACP, "GitHub Copilot CLI extension is already installed.");
				} else {
					throw new RuntimeException(
							"Failed to install the GitHub Copilot CLI extension.\n"
							+ "Run manually:  gh extension install github/gh-copilot\n\n"
							+ "Error details:\n" + String.join("\n", install.errorLines));
				}
			}
		}

		Tracer.trace().trace(Tracer.ACP, "GitHub Copilot CLI extension is ready.");
	}

	// -------------------------------------------------------------------------
	// Process creation
	// -------------------------------------------------------------------------

	@Override
	public Process createProcess() throws IOException {
		String[] startup = getStartupCommand();
		Tracer.trace().trace(Tracer.ACP, String.join(", ", startup));
		ProcessBuilder pb = new ProcessBuilder(startup);
		return pb.start();
	}

	// -------------------------------------------------------------------------
	// Version query (used by the preference page)
	// -------------------------------------------------------------------------

	public String getVersion() {
		ProcessResult result;
		String npmPath = resolveNpmCopilotPath();
		if (npmPath != null) {
			result = runProcess(buildCommand(npmPath, "--version"));
		} else if (resolveDirectBinaryPath() != null) {
			result = runProcess(new String[] { resolveDirectBinaryPath(), "--version" });
		} else {
			result = runProcess(new String[] { resolveGh(), "copilot", "--version" });
		}
		if (result.result == 0 && !result.inputLines.isEmpty()) {
			return result.inputLines.get(0);
		}
		for (String line : result.errorLines) {
			Tracer.trace().trace(Tracer.ACP, line);
		}
		return "Not found";
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private String resolveGh() {
		String pref = Activator.getDefault().getPreferenceStore().getString(P_ACP_COPILOT_GH_PATH);
		return (pref == null || pref.isBlank()) ? "gh" : pref;
	}

	/** Returns the direct gh-copilot binary path, or {@code null} when not configured. */
	public String resolveDirectBinaryPath() {
		String pref = Activator.getDefault().getPreferenceStore().getString(P_ACP_COPILOT_DIRECT_PATH);
		return (pref == null || pref.isBlank()) ? null : pref.trim();
	}

	/**
	 * Returns the npm copilot binary name/path (for {@code copilot --acp} mode),
	 * or {@code null} when npm mode is not configured.
	 */
	public String resolveNpmCopilotPath() {
		String pref = Activator.getDefault().getPreferenceStore().getString(P_ACP_COPILOT_NPM_PATH);
		return (pref == null || pref.isBlank()) ? null : pref.trim();
	}
}
