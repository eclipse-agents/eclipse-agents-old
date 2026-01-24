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

import java.io.IOException;

import org.eclipse.agents.Activator;
import org.eclipse.agents.chat.controller.AgentController;
import org.eclipse.agents.chat.controller.IAgentServiceListener;
import org.eclipse.agents.services.agent.GeminiService;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.McpCapabilities;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status; 	
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
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
	
	// Track if version check is in progress to avoid multiple concurrent jobs
	private volatile boolean versionCheckInProgress = false;
	
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
		useLocalInstall.addSelectionListener(this);
		useLocalInstall.setVisible(false);

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
		updateEnablement();
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
				if (!start.isDisposed() && !stop.isDisposed()) {
					start.setEnabled(!service.isRunning() && !service.isScheduled());
					stop.setEnabled(service.isRunning());
				}
			}
		}
		input.setEnabled(!useLocalInstall.getSelection());}
	
	private void updateStatus() {
		for (IAgentService service: AgentController.instance().getAgents()) {
			if (service instanceof GeminiService) {
				final GeminiService geminiService = (GeminiService) service;
				if (service.isRunning() && service.getInitializeResponse() != null) {
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
				
				// Update installed version asynchronously in a background job
				// Only spawn a new job if one isn't already running
				if (!versionCheckInProgress) {
					installVersion.setText("...");
					versionCheckInProgress = true;
					
					Job versionJob = new Job("Fetching Gemini CLI version") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								final String version = geminiService.getVersion();
								
								Activator.getDisplay().asyncExec(() -> {
									if (!installVersion.isDisposed()) {
										installVersion.setText(version);
									}
								});
								
								return Status.OK_STATUS;
							} finally {
								versionCheckInProgress = false;
							}
						}
					};
					versionJob.setSystem(true); // Don't show in progress view
					versionJob.schedule();
				}
			}
		}
	}
	
	private void startGeminiService() {
		for (IAgentService service: AgentController.instance().getAgents()) {
			if (service instanceof GeminiService) {
				service.schedule();
				updateEnablement();
			}
		}
	}
	
	private void stopGeminiService() {
		for (IAgentService service: AgentController.instance().getAgents()) {
			if (service instanceof GeminiService) {
				service.stop();
				service.unschedule();
				updateEnablement();
			}
		}
	}
	
	private void handleInstallButton() {
		final String version = validateVersion();
		if (version == null) {
			return;
		}
		
		for (IAgentService service : AgentController.instance().getAgents()) {
			if (service instanceof GeminiService) {
				final GeminiService geminiService = (GeminiService) service;
				
				// Stop the service if running before installing
				if (service.isRunning()) {
					service.stop();
					service.unschedule();
				}
				
				installButton.setEnabled(false);
				uninstallButton.setEnabled(false);
				
				// Create and schedule installation job
				Job installJob = createInstallJob(geminiService, version);
				installJob.schedule();
				break;
			}
		}
	}
	
	private void handleUninstallButton() {
		// Confirm uninstall action
		boolean confirmed = MessageDialog.openConfirm(getShell(), "Uninstall Gemini CLI",
				"Are you sure you want to uninstall Gemini CLI?\n\n" +
				"This will remove the installation from:\n" + installLocation.getText());
		
		if (!confirmed) {
			return;
		}
		
		for (IAgentService service : AgentController.instance().getAgents()) {
			if (service instanceof GeminiService) {
				final GeminiService geminiService = (GeminiService) service;
				
				// Stop the service if running before uninstalling
				if (service.isRunning()) {
					service.stop();
					service.unschedule();
				}
				
				// Disable buttons while job is running
				installButton.setEnabled(false);
				uninstallButton.setEnabled(false);
				
				Job uninstallJob = createUninstallJob(geminiService);
				uninstallJob.schedule();
				break;
			}
		}
	}
	
	private void handleUseLocalInstallToggle() {
		if (useLocalInstall.getSelection()) {
			input.setText(getPreferenceStore().getDefaultString(geminiPreferenceId));
		} else {
			input.setText("gemini\n--experimental-acp");
		}
		parent.layout(true);
	}
	
	private String validateVersion() {
		final String version = targetVersion.getText().trim();
		if (version.isEmpty()) {
			MessageDialog.openError(getShell(), "Install Gemini CLI", "Please enter a target version.");
			return null;
		}
		return version;
	}
	
	private Job createInstallJob(final GeminiService geminiService, final String version) {
		Job installJob = new Job("Installing Gemini CLI") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Installing version " + version, IProgressMonitor.UNKNOWN);
				try {
					geminiService.installGemini(version, monitor);
					
					// Refresh UI on the display thread
					Activator.getDisplay().asyncExec(() -> {
						if (!parent.isDisposed()) {
							updateStatus();
							updateEnablement();
						}
					});
					
					return Status.OK_STATUS;
				} catch (IOException e) {
					System.out.println("User-initiated installation failed for Gemini CLI v" + version);
					e.printStackTrace();
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
							"Installation failed: " + e.getMessage(), e);
				} finally {
					monitor.done();
					// Re-enable buttons on the display thread
					Activator.getDisplay().asyncExec(() -> {
						if (!installButton.isDisposed()) {
							installButton.setEnabled(true);
						}
						if (!uninstallButton.isDisposed()) {
							uninstallButton.setEnabled(true);
						}
					});
				}
			}
		};
		installJob.setUser(true);
		return installJob;
	}
	
	private Job createUninstallJob(final GeminiService geminiService) {
		Job uninstallJob = new Job("Uninstalling Gemini CLI") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Uninstalling version ", IProgressMonitor.UNKNOWN);
				try {
					geminiService.uninstallGemini(monitor);
					
					// Refresh UI on the display thread
					Activator.getDisplay().asyncExec(() -> {
						if (!parent.isDisposed()) {
							updateStatus();
							updateEnablement();
						}
					});
					
					return Status.OK_STATUS;
				} catch (IOException e) {
					System.out.println("User-initiated uninstallation failed for Gemini CLI");
					e.printStackTrace();
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
							"Uninstallation failed: " + e.getMessage(), e);
				} finally {
					monitor.done();
					// Re-enable buttons on the display thread
					Activator.getDisplay().asyncExec(() -> {
						if (!installButton.isDisposed()) {
							installButton.setEnabled(true);
						}
						if (!uninstallButton.isDisposed()) {
							uninstallButton.setEnabled(true);
						}
					});
				}
			}
		};
		uninstallJob.setUser(true);
		return uninstallJob;
	}

	private void loadPreferences() {
		IPreferenceStore store = getPreferenceStore();
		input.setText(store.getString(geminiPreferenceId));
		useLocalInstall.setSelection(store.getString(geminiPreferenceId).equals(store.getDefaultString(geminiPreferenceId)));
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
		useLocalInstall.setSelection(true);
		updateValidation();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		widgetSelected(event);
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.getSource() == start) {
			startGeminiService();
		} else if (event.getSource() == stop) {
			stopGeminiService();
		} else if (event.getSource() == installButton) {
			handleInstallButton();
		} else if (event.getSource() == uninstallButton) {
			handleUninstallButton();
		} else if (event.getSource() == useLocalInstall) {
			handleUseLocalInstallToggle();
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