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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CustomAgentDialog extends TitleAreaDialog {

	private Text nameText;
	private Text commandText;
	private Text workingDirText;

	private String name = "";
	private String command = "";
	private String workingDirectory = "";

	public CustomAgentDialog(Shell parentShell) {
		super(parentShell);
	}

	public CustomAgentDialog(Shell parentShell, String name, String command, String workingDirectory) {
		super(parentShell);
		this.name = name != null ? name : "";
		this.command = command != null ? command : "";
		this.workingDirectory = workingDirectory != null ? workingDirectory : "";
	}

	@Override
	public void create() {
		super.create();
		setTitle("Custom Agent Configuration");
		setMessage("Configure a custom ACP agent by specifying its name and startup command.");
		validate();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);

		// Name field
		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText("Name:");
		nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		nameText = new Text(container, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		nameText.setText(name);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		// Command field
		Label commandLabel = new Label(container, SWT.NONE);
		commandLabel.setText("Startup Command:");
		commandLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 1));

		commandText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		GridData commandGd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		commandGd.heightHint = 80;
		commandText.setLayoutData(commandGd);
		commandText.setText(command);
		commandText.setToolTipText("Enter the startup command, one argument per line");
		commandText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		// Working Directory field
		Label workDirLabel = new Label(container, SWT.NONE);
		workDirLabel.setText("Working Directory:");
		workDirLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		workingDirText = new Text(container, SWT.SINGLE | SWT.BORDER);
		workingDirText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		workingDirText.setText(workingDirectory);
		workingDirText.setToolTipText("Optional working directory for the agent process");

		Button browseButton = new Button(container, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setText("Select Working Directory");
				if (!workingDirText.getText().isEmpty()) {
					dialog.setFilterPath(workingDirText.getText());
				}
				String selected = dialog.open();
				if (selected != null) {
					workingDirText.setText(selected);
				}
			}
		});

		return area;
	}

	private void validate() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton == null) {
			return;
		}

		String nameValue = nameText.getText().trim();
		String commandValue = commandText.getText().trim();

		if (nameValue.isEmpty()) {
			setErrorMessage("Name must not be empty");
			okButton.setEnabled(false);
			return;
		}

		if (commandValue.isEmpty()) {
			setErrorMessage("Startup command must not be empty");
			okButton.setEnabled(false);
			return;
		}

		setErrorMessage(null);
		okButton.setEnabled(true);
	}

	@Override
	protected void okPressed() {
		name = nameText.getText().trim();
		command = commandText.getText().trim();
		// Normalize line endings
		command = command.replaceAll("\r\n", "\n");
		workingDirectory = workingDirText.getText().trim();
		super.okPressed();
	}

	public String getAgentName() {
		return name;
	}

	public String getCommand() {
		return command;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}
}
