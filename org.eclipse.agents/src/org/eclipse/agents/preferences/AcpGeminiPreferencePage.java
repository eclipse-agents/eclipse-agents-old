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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;


public class AcpGeminiPreferencePage extends PreferencePage implements 
		IAgentServiceListener, IPreferenceConstants, 
		IWorkbenchPreferencePage, SelectionListener, ModifyListener {

	VerifyListener integerListener;
	PreferenceManager preferenceManager;
	final String geminiPreferenceId = new GeminiService().getStartupCommandPreferenceId();
	
	Text input;
	Button start, stop;
	Text status;
	
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

		Composite parent = new Composite(ancestor, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		Label instructions = new Label(parent, SWT.WRAP);
		instructions.setText("ACP lets you chat with CLI agents like Gemini and Claude Code");
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(80);
		gd.horizontalSpan = 4;
		instructions.setLayoutData(gd);

		Label label = new Label(parent, SWT.NONE);
		label.setText("Startup Command:");
		label.setLayoutData(new GridData());
		
		input = new Text(parent, SWT.MULTI | SWT.BORDER);
		input.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridData)input.getLayoutData()).minimumHeight = 30;
		((GridData)input.getLayoutData()).horizontalSpan = 3;
		
		start = new Button(parent, SWT.PUSH);
		start.setLayoutData(new GridData());
		start.setText("Start");
		((GridData)start.getLayoutData()).horizontalSpan = 2;
		start.addSelectionListener(this);
		
		stop = new Button(parent, SWT.PUSH);
		stop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		stop.setText("stop");
		((GridData)stop.getLayoutData()).horizontalSpan = 2;
		stop.addSelectionListener(this);
		
		status = new Text(parent, SWT.MULTI | SWT.BORDER);
		status.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridData)status.getLayoutData()).minimumHeight = 50;
		((GridData)status.getLayoutData()).horizontalSpan = 3;
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"org.eclipse.agents.preferences.AcpGeminiPreferencePage"); //$NON-NLS-1$

		loadPreferences();
		updateValidation();
		
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

	private void loadPreferences() {
		IPreferenceStore store = getPreferenceStore();
		input.setText(store.getString(geminiPreferenceId));
	}

	private void savePreferences() {
		IPreferenceStore store = getPreferenceStore();

		String preference = input.getText();
		// Handle when line delimiters contain a carriage return character
		preference = preference.replaceAll("\r\n", "\n");
		
		store.setValue(geminiPreferenceId, preference);
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
				service.schedule();
				start.setEnabled(false);
			}
		} else if (event.getSource() == stop) {
			for (IAgentService service: AgentController.instance().getAgents()) {
				if (service.isRunning() || service.isScheduled()) {
					service.stop();
					stop.setEnabled(false);
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
			status.setText("");
		}
		start.setEnabled(true);
		stop.setEnabled(false);
		
	}

	@Override
	public void agentScheduled(IAgentService service) {
		start.setEnabled(false);
		stop.setEnabled(true);
	}

	@Override
	public void agentStarted(IAgentService service) {
		start.setEnabled(false);
		stop.setEnabled(true);

		if (service instanceof GeminiService) {
			InitializeResponse response = service.getInitializeResponse();
			StringBuffer buffer = new StringBuffer();
			buffer.append("Gemini CLI Features:");
			
			buffer.append("  Load Prior Sessions: " + response.agentCapabilities().loadSession());
			buffer.append("  Prompt Capabilities: ");
			buffer.append("    Embedded Contexts: " + response.agentCapabilities().promptCapabilities().embeddedContext());
			buffer.append("    Audio: " + response.agentCapabilities().promptCapabilities().embeddedContext());
			buffer.append("    Images: " + response.agentCapabilities().promptCapabilities().embeddedContext());
			
			buffer.append("  MCP Autoconfiguration: ");
			buffer.append("     MCP over SSE: " + response.agentCapabilities().mcpCapabilities().sse());
			buffer.append("     MCP over HTTP: " + response.agentCapabilities().mcpCapabilities().http());

			status.setText(buffer.toString());
		}
	}

	@Override
	public void agentFailed(IAgentService service, IStatus istatus) {
		if (service instanceof GeminiService) {
			status.setText(istatus.getMessage());
		}
	}
}