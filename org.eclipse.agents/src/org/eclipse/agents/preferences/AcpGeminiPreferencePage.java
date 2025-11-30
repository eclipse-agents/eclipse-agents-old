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

import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.chat.controller.IAgentServiceListener;
import org.eclipse.agents.services.agent.GeminiService;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;


public class AcpGeminiPreferencePage extends PreferencePage implements 
		IAgentServiceListener, IPreferenceConstants, 
		IWorkbenchPreferencePage, SelectionListener, ModifyListener {

	Composite parent;
	VerifyListener integerListener;
	PreferenceManager preferenceManager;
	final String geminiPreferenceId = new GeminiService().getStartupCommandPreferenceId();
	
	Button useLocalInstall;
	
	// Local Install
	Text installLocation;
	Button openInstallLocation, installButton, uninstallButton;
	Text targetVersion;
	Text installVersion;
	
	// Runtime
	Text input;
	Button start, stop;
	Text status;
	IStatus startupError = null;
	
	public AcpGeminiPreferencePage() {
		super();

		integerListener = (VerifyEvent e) -> {
			String string = e.text;
			e.doit = string.matches("\\d*"); //$NON-NLS-1$
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
		instructions.setText("ACP lets you chat with CLI agents like Gemini and Claude Code");
		GridData gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1);
		gd.widthHint = convertWidthInCharsToPixels(80);
		instructions.setLayoutData(gd);
		
		useLocalInstall = new Button(parent, SWT.CHECK);
		useLocalInstall.setText("Use Eclipse-dedicated installation");
		useLocalInstall.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1));
		
		Group installation = new Group(parent, SWT.NONE);
		installation.setText("Local Installation");
		layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		installation.setLayout(layout);
		installation.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1));
		
		Label location = new Label(installation, SWT.NONE);
		location.setText("Location:");
		location.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		
		installLocation = new Text(installation, SWT.READ_ONLY);
		installLocation.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1));
		
		Label version = new Label(installation, SWT.NONE);
		version.setText("Target Version:");
		version.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		
		targetVersion = new Text(installation, SWT.NONE);
		targetVersion.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1));
		
		installButton = new Button(installation, SWT.PUSH);
		installButton.setText("Install");
		installButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		installButton.addSelectionListener(this);
		
		uninstallButton = new Button(installation, SWT.PUSH);
		uninstallButton.setText("Uninstall");
		uninstallButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 1));
		uninstallButton.addSelectionListener(this);

		version = new Label(installation, SWT.NONE);
		version.setText("Installed Version:");
		version.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		
		installVersion = new Text(installation, SWT.READ_ONLY);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 3;
		installVersion.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1));

		Label label = new Label(parent, SWT.NONE);
		label.setText("Startup Command:");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1));
		
		input = new Text(parent, SWT.MULTI | SWT.BORDER);
		input.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1));
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
			if (service instanceof GeminiService) {
				if (service.isRunning()) {
					status.setText("Starting");
				} else if (service.isScheduled()) {
					status.setText("Running");
				} else {
					status.setText("Stopped");
				}
				
			}
		}
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"org.eclipse.agents.preferences.AcpGeminiPreferencePage"); //$NON-NLS-1$

		loadPreferences();
		updateValidation();
		updateStatus();
		
		for (IAgentService service: AgentController.instance().getAgents()) {
			if (service instanceof GeminiService) {
				installLocation.setText(((GeminiService)service).getAgentsNodeDirectory().getAbsolutePath());
			}
		}
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
			if (service instanceof GeminiService) {
				start.setEnabled(!service.isRunning() && !service.isScheduled());
				stop.setEnabled(service.isRunning());	
			}
		}
	}
	
	private void updateStatus() {
		for (IAgentService service: AgentController.instance().getAgents()) {
			if (service instanceof GeminiService) {
				if (service.isRunning()) {
					InitializeResponse response = service.getInitializeResponse();
					StringBuffer buffer = new StringBuffer();
					buffer.append("Gemini CLI Features:");
					
					buffer.append("\n  Load Prior Sessions: " + response.agentCapabilities().loadSession());
					buffer.append("\n  Prompt Capabilities: ");
					buffer.append("\n    Embedded Contexts: " + response.agentCapabilities().promptCapabilities().embeddedContext());
					buffer.append("\n    Audio: " + response.agentCapabilities().promptCapabilities().embeddedContext());
					buffer.append("\n    Images: " + response.agentCapabilities().promptCapabilities().embeddedContext());
					
					
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
			}
		}
	}

	private void loadPreferences() {
		IPreferenceStore store = getPreferenceStore();
		input.setText(store.getString(geminiPreferenceId));
		targetVersion.setText(store.getString(P_ACP_GEMINI_VERSION));
	}

	private void savePreferences() {
		IPreferenceStore store = getPreferenceStore();

		String preference = input.getText();
		// Sync carriage returns with what is used for parsing and default preferences
		preference = preference.replaceAll("\r\n", "\n");
		
		store.setValue(geminiPreferenceId, preference);
		store.setValue(P_ACP_GEMINI_VERSION, targetVersion.getText());
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
		input.setText(store.getDefaultString(geminiPreferenceId));
		targetVersion.setText(store.getDefaultString(P_ACP_GEMINI_VERSION));
		updateValidation();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		widgetSelected(event);
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.getSource() == start) {
			for (IAgentService service: AgentController.instance().getAgents()) {
				if (service instanceof GeminiService) {
					service.schedule();
					updateEnablement();
				}
			}
		} else if (event.getSource() == stop) {
			for (IAgentService service: AgentController.instance().getAgents()) {
				if (service instanceof GeminiService) {
					service.stop();
					service.unschedule();
					updateEnablement();
				}
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
		if (service instanceof GeminiService) {
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
		if (service instanceof GeminiService) {
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
		if (service instanceof GeminiService) {
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
		if (service instanceof GeminiService) {
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