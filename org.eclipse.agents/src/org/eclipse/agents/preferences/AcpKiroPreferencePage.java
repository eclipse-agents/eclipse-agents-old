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
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.agents.services.agent.KiroService;
import org.eclipse.agents.services.protocol.AcpSchema.InitializeResponse;
import org.eclipse.agents.services.protocol.AcpSchema.McpCapabilities;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Preference page for configuring Kiro CLI agent settings.
 * Provides UI for startup command configuration and agent lifecycle control.
 */
public class AcpKiroPreferencePage extends PreferencePage implements 
        IAgentServiceListener, IWorkbenchPreferencePage, SelectionListener {

    private Composite parent;
    private final String kiroPreferenceId;
    
    // Startup command
    private Text startupCommandText;
    
    // Runtime controls
    private Button startButton;
    private Button stopButton;
    private Text statusText;

    public AcpKiroPreferencePage() {
        super();
        kiroPreferenceId = new KiroService().getStartupCommandPreferenceId();
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
        
        // Instructions
        Label instructions = new Label(parent, SWT.WRAP);
        instructions.setText("Configure Kiro CLI agent settings. Kiro is an AI coding assistant that communicates via ACP.");
        GridData gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1);
        gd.widthHint = convertWidthInCharsToPixels(80);
        instructions.setLayoutData(gd);
        
        // Startup command label
        Label label = new Label(parent, SWT.NONE);
        label.setText("Startup Command:");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1));
        
        // Startup command text field
        startupCommandText = new Text(parent, SWT.MULTI | SWT.BORDER);
        startupCommandText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1));
        ((GridData)startupCommandText.getLayoutData()).minimumHeight = 30;

        
        // Start button
        startButton = new Button(parent, SWT.PUSH);
        startButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
        startButton.setText("Start");
        startButton.addSelectionListener(this);
        
        // Stop button
        stopButton = new Button(parent, SWT.PUSH);
        stopButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
        stopButton.setText("Stop");
        stopButton.addSelectionListener(this);
        
        // Spacer
        new Label(parent, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
        
        // Status label
        Label statusLabel = new Label(parent, SWT.NONE);
        statusLabel.setText("Status:");
        statusLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 4, 1));
        
        // Status text field
        statusText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
        statusText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1));
        ((GridData)statusText.getLayoutData()).minimumHeight = 100;
        
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "org.eclipse.agents.preferences.AcpKiroPreferencePage");
        
        loadPreferences();
        updateEnablement();
        updateStatus();
        
        return parent;
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        AgentController.instance().addAgentListener(this);
    }

    private void updateEnablement() {
        for (IAgentService service : AgentController.instance().getAgents()) {
            if (service instanceof KiroService) {
                if (!startButton.isDisposed() && !stopButton.isDisposed()) {
                    startButton.setEnabled(!service.isRunning() && !service.isScheduled());
                    stopButton.setEnabled(service.isRunning());
                }
            }
        }
    }

    private void updateStatus() {
        for (IAgentService service : AgentController.instance().getAgents()) {
            if (service instanceof KiroService) {
                if (service.isRunning() && service.getInitializeResponse() != null) {
                    InitializeResponse response = service.getInitializeResponse();
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("Kiro CLI Features:");
                    
                    buffer.append("\n  Load Prior Sessions: " + response.agentCapabilities().loadSession());
                    buffer.append("\n  Prompt Capabilities: ");
                    buffer.append("\n    Embedded Contexts: " + response.agentCapabilities().promptCapabilities().embeddedContext());
                    buffer.append("\n    Audio: " + response.agentCapabilities().promptCapabilities().audio());
                    buffer.append("\n    Images: " + response.agentCapabilities().promptCapabilities().image());
                    
                    McpCapabilities mcp = response.agentCapabilities().mcpCapabilities();
                    buffer.append("\n  MCP Autoconfiguration: ");
                    buffer.append("\n     MCP over SSE: " + (mcp == null ? false : mcp.sse()));
                    buffer.append("\n     MCP over HTTP: " + (mcp == null ? false : mcp.http()));
                    
                    statusText.setText(buffer.toString());
                    parent.layout(true);
                    
                } else if (service.isScheduled()) {
                    statusText.setText("Starting");
                } else {
                    statusText.setText("Stopped");
                }
            }
        }
    }

    private void loadPreferences() {
        IPreferenceStore store = getPreferenceStore();
        startupCommandText.setText(store.getString(kiroPreferenceId));
    }

    private void savePreferences() {
        IPreferenceStore store = getPreferenceStore();
        String preference = startupCommandText.getText();
        // Sync carriage returns with what is used for parsing and default preferences
        preference = preference.replaceAll("\r\n", "\n");
        store.setValue(kiroPreferenceId, preference);
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
        startupCommandText.setText(store.getDefaultString(kiroPreferenceId));
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent event) {
        widgetSelected(event);
    }

    @Override
    public void widgetSelected(SelectionEvent event) {
        if (event.getSource() == startButton) {
            for (IAgentService service : AgentController.instance().getAgents()) {
                if (service instanceof KiroService) {
                    service.schedule();
                    updateEnablement();
                }
            }
        } else if (event.getSource() == stopButton) {
            for (IAgentService service : AgentController.instance().getAgents()) {
                if (service instanceof KiroService) {
                    service.stop();
                    service.unschedule();
                    updateEnablement();
                }
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        AgentController.instance().removeAgentListener(this);
    }

    @Override
    public void agentStopped(IAgentService service) {
        if (service instanceof KiroService) {
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
        if (service instanceof KiroService) {
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
        if (service instanceof KiroService) {
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
        if (service instanceof KiroService) {
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
