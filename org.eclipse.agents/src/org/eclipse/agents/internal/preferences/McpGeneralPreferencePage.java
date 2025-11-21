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
package org.eclipse.agents.internal.preferences;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.eclipse.agents.Activator;
import org.eclipse.agents.internal.ServerManager.IServerListener;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;


public class McpGeneralPreferencePage extends PreferencePage
		implements IPreferenceConstants, IWorkbenchPreferencePage, SelectionListener, ModifyListener, IServerListener {

	VerifyListener integerListener;
	PreferenceManager preferenceManager;
	
	Button serverEnable;
	Text serverPort;
	Text messages;
	
	public McpGeneralPreferencePage() {
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

		Link link = new Link(parent, SWT.NONE);
		link.setText("To change the availability of categories of MCP services, click <a>Capabilities</a>.");
		link.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), "org.eclipse.sdk.capabilities", //$NON-NLS-1$
						null, null);
			}
		});
		link.setLayoutData(new GridData());
		((GridData)link.getLayoutData()).horizontalSpan = 4;

		Label instructions = new Label(parent, SWT.WRAP);
		instructions.setText("Eclipse's built-in Model Context Protocol (MCP) server enables interactivity between the Eclipse IDE and Large Language Model services");
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(80);
		gd.horizontalSpan = 4;
		instructions.setLayoutData(gd);
		
		serverEnable = new Button(parent, SWT.CHECK);
		serverEnable.setText("Enable MCP HTTP Server");
		serverEnable.setLayoutData(new GridData());
		((GridData)serverEnable.getLayoutData()).horizontalSpan = 1;
		
		Label label = new Label(parent, SWT.NONE);
		label.setText("HTTP Port:");
		label.setLayoutData(new GridData());
		
		serverPort = new Text(parent, SWT.SINGLE | SWT.BORDER);
		serverPort.setLayoutData(new GridData());
		serverPort.addVerifyListener(integerListener);

		Button pathCopy = new Button(parent, SWT.PUSH);
		pathCopy.setLayoutData(new GridData());
		pathCopy.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));
		pathCopy.setToolTipText("Copy to clipboard");
		pathCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringSelection strSelection = new StringSelection("http://localhost:" + serverPort.getText() + "/sse");
				Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				systemClipboard.setContents(strSelection, null);
			}
		});
		
		messages = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);
		messages.setLayoutData(new GridData(GridData.FILL_BOTH));
		((GridData)messages.getLayoutData()).horizontalSpan = 4;
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"org.eclipse.agents.internal.preferences.McpGeneralPreferencePage"); //$NON-NLS-1$

		loadPreferences();
		updateValidation();
		
		Activator.getDefault().getServerManager().addServerListener(this);
		if (Activator.getDefault().getServerManager().isRunning()) {
			messages.setText(Activator.getDefault().getServerManager().getServerContentsDescription());
		}
		
		return parent;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	private void updateValidation() {
		String errorMessage = null;

		if (serverEnable.getSelection() && serverPort.getText().isEmpty()) {
			errorMessage = "Enter an HTTP Port";
		}

		setValid(errorMessage == null);
		setErrorMessage(errorMessage);

	}

	private void loadPreferences() {
		IPreferenceStore store = getPreferenceStore();
		serverEnable.setSelection(store.getBoolean(P_SERVER_ENABLED));
		serverPort.setText("" + store.getInt(P_SERVER_HTTP_PORT));
	}

	private void savePreferences() {
		IPreferenceStore store = getPreferenceStore();

		boolean restartServer = false;
		if (store.getBoolean(P_SERVER_ENABLED) != serverEnable.getSelection()) {
			restartServer = true;
		} else if (serverEnable.getSelection() && 
				!serverPort.getText().equals("" + store.getInt(P_SERVER_HTTP_PORT))) {
			restartServer = true;
		}
				
		store.setValue(P_SERVER_ENABLED, serverEnable.getSelection());
		store.setValue(P_SERVER_HTTP_PORT, Integer.parseInt(serverPort.getText()));;

		if (restartServer) {
			Activator.getDefault().requestServerRestart();
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

		serverEnable.setSelection(store.getDefaultBoolean(P_SERVER_ENABLED));
		serverPort.setText("" + store.getDefaultInt(P_SERVER_HTTP_PORT));
		
		updateValidation();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		widgetSelected(event);
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		updateValidation();
	}

	@Override
	public void modifyText(ModifyEvent event) {
		updateValidation();
	}

	@Override
	public void dispose() {
		super.dispose();
		Activator.getDefault().getServerManager().removeServerListener(this);
	}

	@Override
	public void serverStarted(String contents) {
		messages.setText(contents);
	}

	@Override
	public void serverStopped() {
		messages.setText("");
	}
}