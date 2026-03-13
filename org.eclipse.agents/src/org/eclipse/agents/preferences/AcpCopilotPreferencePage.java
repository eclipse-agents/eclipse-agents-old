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
package org.eclipse.agents.preferences;

import java.io.File;

import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.chat.controller.IAgentServiceListener;
import org.eclipse.agents.services.agent.CopilotService;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.McpCapabilities;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;


public class AcpCopilotPreferencePage extends PreferencePage implements
		IAgentServiceListener, IPreferenceConstants,
		IWorkbenchPreferencePage, SelectionListener, ModifyListener {

	Composite parent;
	VerifyListener integerListener;
	PreferenceManager preferenceManager;
	final String copilotPreferenceId = new CopilotService().getStartupCommandPreferenceId();

	// Startup Command
	Text input;
	Button start, stop;
	Text status;
	IStatus startupError = null;

	// GitHub CLI Configuration
	Text ghPath;
	Button autoResolve;
	Text installedVersion;

	// Direct-binary mode
	Button useDirectBinary;
	Text directBinaryPath;
	Button autoDetectBinary;
	Button browseBinary;

	// npm package mode  (@github/copilot)
	Button useNpmMode;
	Text npmCopilotPath;
	Button autoDetectNpm;

	public AcpCopilotPreferencePage() {
		super();
		integerListener = (VerifyEvent e) -> {
			String string = e.text;
			e.doit = string.matches("\\d*");
			return;
		};
	}

	@Override
	protected Control createContents(Composite ancestor) {

		parent = new Composite(ancestor, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData());

		Label instructions = new Label(parent, SWT.WRAP);
		instructions.setText("ACP lets you chat with the GitHub Copilot CLI.\n" +
				"Install the GitHub CLI (gh) from https://cli.github.com/ and then " +
				"the Copilot extension will be auto-installed.\n" +
				"Alternatively, enable 'Direct binary mode' below to use the gh-copilot " +
				"extension binary directly without the GitHub CLI.");
		GridData gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1);
		gd.widthHint = convertWidthInCharsToPixels(80);
		instructions.setLayoutData(gd);

		// GitHub CLI Configuration group
		Group ghConfig = new Group(parent, SWT.NONE);
		ghConfig.setText("GitHub CLI Configuration");
		layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		ghConfig.setLayout(layout);
		ghConfig.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1));

		autoResolve = new Button(ghConfig, SWT.CHECK);
		autoResolve.setText("Auto-detect 'gh' on PATH (recommended)");
		autoResolve.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1));
		autoResolve.addSelectionListener(this);

		Label ghLabel = new Label(ghConfig, SWT.NONE);
		ghLabel.setText("GitHub CLI path (if not on PATH):");
		ghLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));

		ghPath = new Text(ghConfig, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1);
		ghPath.setLayoutData(gd);
		ghPath.setEnabled(false);
		ghPath.addModifyListener(this);

		Label versionLabel = new Label(ghConfig, SWT.NONE);
		versionLabel.setText("GitHub Copilot CLI version:");
		versionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));

		installedVersion = new Text(ghConfig, SWT.READ_ONLY);
		installedVersion.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1));

		// ---- Direct binary group -------------------------------------------
		Group directGroup = new Group(parent, SWT.NONE);
		directGroup.setText("Direct Binary Mode (bypass GitHub CLI)");
		GridLayout directLayout = new GridLayout();
		directLayout.numColumns = 4;
		directLayout.marginHeight = 0;
		directLayout.marginWidth = 0;
		directGroup.setLayout(directLayout);
		directGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1));

		useDirectBinary = new Button(directGroup, SWT.CHECK);
		useDirectBinary.setText(
				"Use gh-copilot extension binary directly (no GitHub CLI required)");
		useDirectBinary.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1));
		useDirectBinary.addSelectionListener(this);

		Label directLabel = new Label(directGroup, SWT.NONE);
		directLabel.setText("Binary path:");
		directLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));

		directBinaryPath = new Text(directGroup, SWT.SINGLE | SWT.BORDER);
		directBinaryPath.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		directBinaryPath.setEnabled(false);
		directBinaryPath.addModifyListener(this);

		autoDetectBinary = new Button(directGroup, SWT.PUSH);
		autoDetectBinary.setText("Auto-detect");
		autoDetectBinary.setEnabled(false);
		autoDetectBinary.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		autoDetectBinary.addSelectionListener(this);

		browseBinary = new Button(directGroup, SWT.PUSH);
		browseBinary.setText("Browse...");
		browseBinary.setEnabled(false);
		browseBinary.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		browseBinary.addSelectionListener(this);

		Label directHint = new Label(directGroup, SWT.WRAP);
		directHint.setText(
				"Typical location (Windows): %APPDATA%\\GitHub CLI\\extensions\\gh-copilot\\gh-copilot_windows_amd64.exe\n" +
				"Typical location (macOS/Linux): ~/.local/share/gh/extensions/gh-copilot/gh-copilot_<os>_<arch>");
		gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1);
		gd.widthHint = convertWidthInCharsToPixels(80);
		directHint.setLayoutData(gd);
		// ---- End direct binary group ----------------------------------------

		// ---- npm package group (@github/copilot) ----------------------------
		Group npmGroup = new Group(parent, SWT.NONE);
		npmGroup.setText("npm Package Mode (@github/copilot)");
		GridLayout npmLayout = new GridLayout();
		npmLayout.numColumns = 3;
		npmLayout.marginHeight = 0;
		npmLayout.marginWidth = 0;
		npmGroup.setLayout(npmLayout);
		npmGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1));

		useNpmMode = new Button(npmGroup, SWT.CHECK);
		useNpmMode.setText(
				"Use npm @github/copilot package  (runs: copilot --acp)");
		useNpmMode.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 3, 1));
		useNpmMode.addSelectionListener(this);

		Label npmLabel = new Label(npmGroup, SWT.NONE);
		npmLabel.setText("copilot command / path:");
		npmLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));

		npmCopilotPath = new Text(npmGroup, SWT.SINGLE | SWT.BORDER);
		npmCopilotPath.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		npmCopilotPath.setEnabled(false);
		npmCopilotPath.addModifyListener(this);

		autoDetectNpm = new Button(npmGroup, SWT.PUSH);
		autoDetectNpm.setText("Auto-detect");
		autoDetectNpm.setEnabled(false);
		autoDetectNpm.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		autoDetectNpm.addSelectionListener(this);

		Label npmHint = new Label(npmGroup, SWT.WRAP);
		npmHint.setText(
				"Install with:  npm install -g @github/copilot\n"
				+ "Leave the path as \"copilot\" when the npm global bin directory is on your PATH.");
		gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 3, 1);
		gd.widthHint = convertWidthInCharsToPixels(80);
		npmHint.setLayoutData(gd);
		// ---- End npm package group ------------------------------------------

		Label label = new Label(parent, SWT.NONE);
		label.setText("Startup Command:");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1));

		input = new Text(parent, SWT.MULTI | SWT.BORDER);
		input.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1));
		((GridData)input.getLayoutData()).minimumHeight = 30;


		start = new Button(parent, SWT.PUSH);
		start.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		start.setText("Start");
		start.addSelectionListener(this);

		stop = new Button(parent, SWT.PUSH);
		stop.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		stop.setText("Stop");
		stop.addSelectionListener(this);

		status = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		status.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1));
		((GridData)status.getLayoutData()).minimumHeight = 100;

		for (IAgentService service: AgentController.instance().getAgents()) {
			if (service instanceof CopilotService) {
				if (service.isRunning()) {
					status.setText("Running");
				} else if (service.isScheduled()) {
					status.setText("Starting");
				} else {
					status.setText("Stopped");
				}
			}
		}

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"org.eclipse.agents.preferences.AcpCopilotPreferencePage"); //$NON-NLS-1$

		loadPreferences();
		updateValidation();
		updateEnablement();
		updateStatus();

		return parent;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		AgentController.instance().addAgentListener(this);
	}

	private void updateValidation() {
		String errorMessage = null;
		setValid(errorMessage == null);
		setErrorMessage(errorMessage);

	}

	private void updateEnablement() {
		for (IAgentService service: AgentController.instance().getAgents()) {
			if (service instanceof CopilotService) {
				if (!start.isDisposed() && !stop.isDisposed()) {
					start.setEnabled(!service.isRunning() && !service.isScheduled());
					stop.setEnabled(service.isRunning());
				}
			}
		}
		boolean npmMode    = useNpmMode.getSelection();
		boolean directMode = !npmMode && useDirectBinary.getSelection();

		// gh CLI group
		autoResolve.setEnabled(!npmMode && !directMode);
		ghPath.setEnabled(!npmMode && !directMode && !autoResolve.getSelection());

		// direct binary group
		useDirectBinary.setEnabled(!npmMode);
		directBinaryPath.setEnabled(directMode);
		autoDetectBinary.setEnabled(directMode);
		browseBinary.setEnabled(directMode);

		// npm group
		npmCopilotPath.setEnabled(npmMode);
		autoDetectNpm.setEnabled(npmMode);

		// startup command field – disabled when a managed mode is active
		input.setEnabled(!npmMode && !directMode && !autoResolve.getSelection());
	}

	private void updateStatus() {
		for (IAgentService service: AgentController.instance().getAgents()) {
			if (service instanceof CopilotService) {
				CopilotService copilotService = (CopilotService) service;
				if (service.isRunning() && service.getInitializeResponse() != null) {
					InitializeResponse response = service.getInitializeResponse();
					StringBuffer buffer = new StringBuffer();
					buffer.append("GitHub Copilot CLI Features:");

					buffer.append("\n  Load Prior Sessions: " + response.agentCapabilities().loadSession());
					buffer.append("\n  Prompt Capabilities: ");
					buffer.append("\n    Embedded Contexts: " + response.agentCapabilities().promptCapabilities().embeddedContext());
					buffer.append("\n    Audio: " + response.agentCapabilities().promptCapabilities().audio());
					buffer.append("\n    Images: " + response.agentCapabilities().promptCapabilities().image());

					McpCapabilities mcp = response.agentCapabilities().mcpCapabilities();
					buffer.append("\n  MCP Autoconfiguration: ");
					buffer.append("\n     MCP over SSE: " + (mcp == null ? false : mcp.sse()));
					buffer.append("\n     MCP over HTTP: " + (mcp == null ? false : mcp.http()));

					status.setText(buffer.toString());
					parent.layout(true);

				} else if (service.isScheduled()) {
					status.setText("Starting");
				} else if (startupError != null) {
					status.setText(startupError.toString());
					getControl().requestLayout();
				} else {
					status.setText("Stopped");
				}

				installedVersion.setText("...");
				Activator.getDisplay().asyncExec(() -> {
					installedVersion.setText(copilotService.getVersion());
				});
			}
		}
	}

	private void loadPreferences() {
		IPreferenceStore store = getPreferenceStore();
		input.setText(store.getString(copilotPreferenceId));
		ghPath.setText(store.getString(P_ACP_COPILOT_GH_PATH));
		autoResolve.setSelection(store.getString(P_ACP_COPILOT_GH_PATH).isEmpty());

		String directPath = store.getString(P_ACP_COPILOT_DIRECT_PATH);
		boolean directMode = directPath != null && !directPath.isBlank();
		useDirectBinary.setSelection(directMode);
		directBinaryPath.setText(directPath != null ? directPath : "");

		String npmPath = store.getString(P_ACP_COPILOT_NPM_PATH);
		boolean npmMode = npmPath != null && !npmPath.isBlank();
		useNpmMode.setSelection(npmMode);
		npmCopilotPath.setText(npmPath != null ? npmPath : "copilot");
	}

	private void savePreferences() {
		IPreferenceStore store = getPreferenceStore();

		String preference = input.getText().replaceAll("\r\n", "\n");
		store.setValue(copilotPreferenceId, preference);
		store.setValue(P_ACP_COPILOT_GH_PATH, autoResolve.getSelection() ? "" : ghPath.getText());

		if (useNpmMode.getSelection()) {
			store.setValue(P_ACP_COPILOT_NPM_PATH, npmCopilotPath.getText().trim());
			store.setValue(P_ACP_COPILOT_DIRECT_PATH, "");
		} else if (useDirectBinary.getSelection()) {
			store.setValue(P_ACP_COPILOT_DIRECT_PATH, directBinaryPath.getText().trim());
			store.setValue(P_ACP_COPILOT_NPM_PATH, "");
		} else {
			store.setValue(P_ACP_COPILOT_DIRECT_PATH, "");
			store.setValue(P_ACP_COPILOT_NPM_PATH, "");
		}
	}

	@Override
	public boolean performCancel() {
		return super.performCancel();
	}

	@Override
	public boolean performOk() {
		savePreferences();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		input.setText(store.getDefaultString(copilotPreferenceId));
		ghPath.setText(store.getDefaultString(P_ACP_COPILOT_GH_PATH));
		autoResolve.setSelection(true);
		useDirectBinary.setSelection(false);
		directBinaryPath.setText("");
		useNpmMode.setSelection(false);
		npmCopilotPath.setText("copilot");
		updateValidation();
		updateEnablement();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		widgetSelected(event);
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.getSource() == start) {
			for (IAgentService service: AgentController.instance().getAgents()) {
				if (service instanceof CopilotService) {
					service.schedule();
					updateEnablement();
				}
			}
		} else if (event.getSource() == stop) {
			for (IAgentService service: AgentController.instance().getAgents()) {
				if (service instanceof CopilotService) {
					service.stop();
					service.unschedule();
					updateEnablement();
				}
			}
		} else if (event.getSource() == autoResolve) {
			updateEnablement();
			if (autoResolve.getSelection()) {
				input.setText(getPreferenceStore().getDefaultString(copilotPreferenceId));
			}
			parent.layout(true);
		} else if (event.getSource() == useDirectBinary) {
			if (useDirectBinary.getSelection()) {
				useNpmMode.setSelection(false); // mutually exclusive
			}
			updateEnablement();
			if (useDirectBinary.getSelection() && directBinaryPath.getText().isBlank()) {
				File found = CopilotService.findGhCopilotExtensionBinary();
				if (found != null) {
					directBinaryPath.setText(found.getAbsolutePath());
				}
			}
			if (!useDirectBinary.getSelection()) {
				input.setText(getPreferenceStore().getDefaultString(copilotPreferenceId));
			}
			parent.layout(true);
		} else if (event.getSource() == autoDetectBinary) {
			File found = CopilotService.findGhCopilotExtensionBinary();
			if (found != null) {
				directBinaryPath.setText(found.getAbsolutePath());
			} else {
				directBinaryPath.setText("");
				setMessage(
						"Could not auto-detect the gh-copilot binary. "
						+ "Please install it via 'gh extension install github/gh-copilot' "
						+ "or browse to the binary manually.",
						WARNING);
			}
		} else if (event.getSource() == browseBinary) {
			FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
			dialog.setText("Select gh-copilot binary");
			String current = directBinaryPath.getText().trim();
			if (!current.isBlank()) {
				File f = new File(current);
				dialog.setFilterPath(f.getParent());
				dialog.setFileName(f.getName());
			}
			String selected = dialog.open();
			if (selected != null) {
				directBinaryPath.setText(selected);
			}
		} else if (event.getSource() == useNpmMode) {
			// Mutually exclusive with direct binary mode
			if (useNpmMode.getSelection()) {
				useDirectBinary.setSelection(false);
				// Auto-detect copilot on PATH
				if (npmCopilotPath.getText().isBlank() || npmCopilotPath.getText().equals("copilot")) {
					String found = CopilotService.findNpmCopilotOnPath();
					npmCopilotPath.setText(found != null ? found : "copilot");
				}
			}
			updateEnablement();
			parent.layout(true);
		} else if (event.getSource() == autoDetectNpm) {
			String found = CopilotService.findNpmCopilotOnPath();
			if (found != null) {
				npmCopilotPath.setText(found);
				setMessage(null); // clear any previous warning
			} else {
				npmCopilotPath.setText("copilot");
				setMessage(
						"'copilot' was not found on PATH. "
						+ "Run: npm install -g @github/copilot  "
						+ "and ensure the npm global bin directory is on your PATH.",
						WARNING);
			}
		}

		updateValidation();
	}

	@Override
	public void modifyText(ModifyEvent event) {
		updateValidation();
	}

	@Override
	public void dispose() {
		super.dispose();
		AgentController.instance().removeAgentListener(this);
	}

	@Override
	public void agentStopped(IAgentService service) {
		if (service instanceof CopilotService) {
			Activator.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateEnablement();
					updateStatus();
				}
			});
		}
	}

	@Override
	public void agentScheduled(IAgentService service) {
		if (service instanceof CopilotService) {
			Activator.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateEnablement();
					updateStatus();
				}
			});
		}
	}

	@Override
	public void agentStarted(IAgentService service) {
		if (service instanceof CopilotService) {
			Activator.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateEnablement();
					updateStatus();
				}
			});
		}
	}

	@Override
	public void agentFailed(IAgentService service) {
		if (service instanceof CopilotService) {
			Activator.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateEnablement();
					updateStatus();
				}
			});
		}
	}
}
