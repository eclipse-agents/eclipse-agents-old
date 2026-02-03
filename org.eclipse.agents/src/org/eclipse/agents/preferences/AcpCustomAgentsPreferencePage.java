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
import org.eclipse.agents.services.agent.CustomAgentService;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AcpCustomAgentsPreferencePage extends PreferencePage implements
		IAgentServiceListener, IPreferenceConstants, IWorkbenchPreferencePage {

	private Table agentTable;
	private Button addButton;
	private Button editButton;
	private Button removeButton;
	private Button startButton;
	private Button stopButton;
	private Text statusText;

	// Working copy of agent configs (edited in UI, saved on OK)
	private JsonArray agentConfigs;

	public AcpCustomAgentsPreferencePage() {
		super();
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		AgentController.instance().addAgentListener(this);
	}

	@Override
	protected Control createContents(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		Label instructions = new Label(parent, SWT.WRAP);
		instructions.setText("Add and manage custom third-party ACP agents.");
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1);
		gd.widthHint = convertWidthInCharsToPixels(80);
		instructions.setLayoutData(gd);

		// Agent table
		agentTable = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		agentTable.setHeaderVisible(true);
		agentTable.setLinesVisible(true);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 6);
		gd.heightHint = 150;
		agentTable.setLayoutData(gd);

		TableColumn nameColumn = new TableColumn(agentTable, SWT.NONE);
		nameColumn.setText("Name");
		nameColumn.setWidth(200);

		TableColumn commandColumn = new TableColumn(agentTable, SWT.NONE);
		commandColumn.setText("Command");
		commandColumn.setWidth(300);

		agentTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
				updateStatus();
			}
		});

		// Buttons
		addButton = new Button(parent, SWT.PUSH);
		addButton.setText("Add...");
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAdd();
			}
		});

		editButton = new Button(parent, SWT.PUSH);
		editButton.setText("Edit...");
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleEdit();
			}
		});

		removeButton = new Button(parent, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemove();
			}
		});

		startButton = new Button(parent, SWT.PUSH);
		startButton.setText("Start");
		startButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleStart();
			}
		});

		stopButton = new Button(parent, SWT.PUSH);
		stopButton.setText("Stop");
		stopButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleStop();
			}
		});

		// Status area
		Label statusLabel = new Label(parent, SWT.NONE);
		statusLabel.setText("Status:");
		statusLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1));

		statusText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1);
		gd.heightHint = 60;
		statusText.setLayoutData(gd);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"org.eclipse.agents.preferences.AcpCustomAgentsPreferencePage"); //$NON-NLS-1$

		loadPreferences();
		refreshTable();
		updateEnablement();

		return parent;
	}

	private void loadPreferences() {
		IPreferenceStore store = getPreferenceStore();
		String json = store.getString(P_ACP_CUSTOM_AGENTS);
		if (json == null || json.isEmpty()) {
			json = "[]";
		}
		Gson gson = new Gson();
		agentConfigs = gson.fromJson(json, JsonArray.class);
		if (agentConfigs == null) {
			agentConfigs = new JsonArray();
		}
	}

	private void refreshTable() {
		agentTable.removeAll();
		for (JsonElement element : agentConfigs) {
			JsonObject obj = element.getAsJsonObject();
			TableItem item = new TableItem(agentTable, SWT.NONE);
			item.setText(0, obj.get("name").getAsString());
			JsonArray cmdArray = obj.getAsJsonArray("command");
			StringBuilder cmdStr = new StringBuilder();
			for (int i = 0; i < cmdArray.size(); i++) {
				if (i > 0) cmdStr.append(" ");
				cmdStr.append(cmdArray.get(i).getAsString());
			}
			item.setText(1, cmdStr.toString());
			item.setData(obj.get("id").getAsString());
		}
	}

	private void updateEnablement() {
		int selIndex = agentTable.getSelectionIndex();
		boolean hasSelection = selIndex >= 0;
		editButton.setEnabled(hasSelection);
		removeButton.setEnabled(hasSelection);

		if (hasSelection) {
			String agentId = (String) agentTable.getItem(selIndex).getData();
			IAgentService service = findRunningService(agentId);
			startButton.setEnabled(service == null || (!service.isRunning() && !service.isScheduled()));
			stopButton.setEnabled(service != null && (service.isRunning() || service.isScheduled()));
		} else {
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
		}
	}

	private void updateStatus() {
		int selIndex = agentTable.getSelectionIndex();
		if (selIndex < 0) {
			statusText.setText("");
			return;
		}

		String agentId = (String) agentTable.getItem(selIndex).getData();
		IAgentService service = findRunningService(agentId);
		if (service == null) {
			statusText.setText("Stopped");
		} else if (service.isRunning()) {
			statusText.setText("Running");
		} else if (service.isScheduled()) {
			statusText.setText("Starting");
		} else {
			statusText.setText("Stopped");
		}
	}

	private IAgentService findRunningService(String agentId) {
		for (IAgentService service : AgentController.instance().getAgents()) {
			if (service instanceof CustomAgentService && service.getId().equals(agentId)) {
				return service;
			}
		}
		return null;
	}

	private void handleAdd() {
		CustomAgentDialog dialog = new CustomAgentDialog(getShell());
		if (dialog.open() == Window.OK) {
			String id = "agent-" + System.currentTimeMillis();
			String name = dialog.getAgentName();
			String command = dialog.getCommand();
			String workingDir = dialog.getWorkingDirectory();

			JsonObject obj = new JsonObject();
			obj.addProperty("id", id);
			obj.addProperty("name", name);
			String[] cmdParts = command.split("\n");
			JsonArray cmdArray = new JsonArray();
			for (String part : cmdParts) {
				String trimmed = part.trim();
				if (!trimmed.isEmpty()) {
					cmdArray.add(trimmed);
				}
			}
			obj.add("command", cmdArray);
			if (workingDir != null && !workingDir.isEmpty()) {
				obj.addProperty("workingDirectory", workingDir);
			}
			agentConfigs.add(obj);
			refreshTable();
			updateEnablement();
		}
	}

	private void handleEdit() {
		int selIndex = agentTable.getSelectionIndex();
		if (selIndex < 0) return;

		JsonObject obj = agentConfigs.get(selIndex).getAsJsonObject();
		String currentName = obj.get("name").getAsString();
		JsonArray cmdArray = obj.getAsJsonArray("command");
		StringBuilder cmdStr = new StringBuilder();
		for (int i = 0; i < cmdArray.size(); i++) {
			if (i > 0) cmdStr.append("\n");
			cmdStr.append(cmdArray.get(i).getAsString());
		}
		String currentWorkDir = "";
		if (obj.has("workingDirectory") && !obj.get("workingDirectory").isJsonNull()) {
			currentWorkDir = obj.get("workingDirectory").getAsString();
		}

		CustomAgentDialog dialog = new CustomAgentDialog(getShell(), currentName, cmdStr.toString(), currentWorkDir);
		if (dialog.open() == Window.OK) {
			obj.addProperty("name", dialog.getAgentName());
			String command = dialog.getCommand();
			String[] cmdParts = command.split("\n");
			JsonArray newCmdArray = new JsonArray();
			for (String part : cmdParts) {
				String trimmed = part.trim();
				if (!trimmed.isEmpty()) {
					newCmdArray.add(trimmed);
				}
			}
			obj.add("command", newCmdArray);
			String workingDir = dialog.getWorkingDirectory();
			if (workingDir != null && !workingDir.isEmpty()) {
				obj.addProperty("workingDirectory", workingDir);
			} else {
				obj.remove("workingDirectory");
			}
			refreshTable();
			agentTable.setSelection(selIndex);
			updateEnablement();
		}
	}

	private void handleRemove() {
		int selIndex = agentTable.getSelectionIndex();
		if (selIndex < 0) return;

		String name = agentTable.getItem(selIndex).getText(0);
		boolean confirm = MessageDialog.openConfirm(getShell(), "Remove Custom Agent",
				"Are you sure you want to remove the agent '" + name + "'?");
		if (confirm) {
			String agentId = (String) agentTable.getItem(selIndex).getData();
			// Stop the agent if running
			IAgentService service = findRunningService(agentId);
			if (service != null) {
				AgentController.instance().removeAgent(service);
			}
			agentConfigs.remove(selIndex);
			refreshTable();
			updateEnablement();
			statusText.setText("");
		}
	}

	private void handleStart() {
		int selIndex = agentTable.getSelectionIndex();
		if (selIndex < 0) return;

		String agentId = (String) agentTable.getItem(selIndex).getData();
		IAgentService service = findRunningService(agentId);
		if (service != null) {
			service.schedule();
		} else {
			// Agent not yet added to controller - need to save first, then sync
			savePreferences();
			AgentController.instance().refreshCustomAgents();
			service = findRunningService(agentId);
			if (service != null) {
				service.schedule();
			}
		}
		updateEnablement();
		updateStatus();
	}

	private void handleStop() {
		int selIndex = agentTable.getSelectionIndex();
		if (selIndex < 0) return;

		String agentId = (String) agentTable.getItem(selIndex).getData();
		IAgentService service = findRunningService(agentId);
		if (service != null) {
			service.stop();
			service.unschedule();
		}
		updateEnablement();
		updateStatus();
	}

	private void savePreferences() {
		IPreferenceStore store = getPreferenceStore();
		Gson gson = new Gson();
		store.setValue(P_ACP_CUSTOM_AGENTS, gson.toJson(agentConfigs));
	}

	@Override
	public boolean performOk() {
		savePreferences();
		AgentController.instance().refreshCustomAgents();
		return super.performOk();
	}

	@Override
	public boolean performCancel() {
		return super.performCancel();
	}

	@Override
	protected void performDefaults() {
		agentConfigs = new JsonArray();
		refreshTable();
		updateEnablement();
		statusText.setText("");
	}

	@Override
	public void dispose() {
		super.dispose();
		AgentController.instance().removeAgentListener(this);
	}

	@Override
	public void agentStarted(IAgentService service) {
		if (service instanceof CustomAgentService) {
			Activator.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!agentTable.isDisposed()) {
						updateEnablement();
						updateStatus();
					}
				}
			});
		}
	}

	@Override
	public void agentStopped(IAgentService service) {
		if (service instanceof CustomAgentService) {
			Activator.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!agentTable.isDisposed()) {
						updateEnablement();
						updateStatus();
					}
				}
			});
		}
	}

	@Override
	public void agentScheduled(IAgentService service) {
		if (service instanceof CustomAgentService) {
			Activator.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!agentTable.isDisposed()) {
						updateEnablement();
						updateStatus();
					}
				}
			});
		}
	}

	@Override
	public void agentFailed(IAgentService service) {
		if (service instanceof CustomAgentService) {
			Activator.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!agentTable.isDisposed()) {
						updateEnablement();
						updateStatus();
					}
				}
			});
		}
	}
}
