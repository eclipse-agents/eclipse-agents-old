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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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


public class AcpGeneralPreferencePage extends PreferencePage
		implements IPreferenceConstants, IWorkbenchPreferencePage, ModifyListener {

	VerifyListener integerListener;
	Text cwd;
	Button prompt4MCP;
	
	public AcpGeneralPreferencePage() {
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
		instructions.setText("Chat with CLI agents like Gemini and Claude Code");
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(80);
		gd.horizontalSpan = 4;
		instructions.setLayoutData(gd);
		

		Label label = new Label(parent, SWT.NONE);
		label.setText("Working Directory");
		label.setToolTipText("Directory under which agents may read and write to the file system");
		label.setLayoutData(new GridData());
		
		cwd = new Text(parent, SWT.SINGLE | SWT.BORDER);
		cwd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridData)cwd.getLayoutData()).horizontalSpan = 3;
		cwd.addModifyListener(this);
		
		prompt4MCP = new Button(parent, SWT.CHECK);
		prompt4MCP.setText("Prompt to add 'Agent Contexts (MCP)' to chat sessions when not configured");
		prompt4MCP.setLayoutData(new GridData());
		
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"org.eclipse.agent.acp.preferences.AcpGeneralPreferencePage"); //$NON-NLS-1$

		loadPreferences();
		updateValidation();
		
		return parent;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	private void updateValidation() {
		String errorMessage = cwd.getText().length() == 0 ? "Enter working directory" : null;
		
		if (errorMessage == null) {
			File file = new File(cwd.getText());
			if (!file.exists() || !file.isDirectory()) {
				errorMessage = "Enter valid directory path";
			}
		}
		
		setValid(errorMessage == null);
		setErrorMessage(errorMessage);

	}

	private void loadPreferences() {
		IPreferenceStore store = getPreferenceStore();
		cwd.setText(store.getString(P_ACP_WORKING_DIR));
		prompt4MCP.setSelection(store.getBoolean(P_ACP_PROMPT4MCP));
	}

	private void savePreferences() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(P_ACP_WORKING_DIR, cwd.getText());
		store.setValue(P_ACP_PROMPT4MCP, prompt4MCP.getSelection());
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

		cwd.setText(store.getDefaultString(P_ACP_WORKING_DIR));
		prompt4MCP.setSelection(store.getDefaultBoolean(P_ACP_PROMPT4MCP));
		
		updateValidation();
	}

	@Override
	public void modifyText(ModifyEvent event) {
		updateValidation();
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}