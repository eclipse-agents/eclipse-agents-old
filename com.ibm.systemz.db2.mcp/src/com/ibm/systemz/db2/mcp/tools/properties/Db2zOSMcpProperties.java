/*******************************************************************************
 * IBM Confidential - OCO Source Materials
 * 
 * 5724-T07 IBM Rational Developer for System z Copyright IBM Corporation 2022, 2023
 * 
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U.S. Copyright Office.
 *******************************************************************************/

package com.ibm.systemz.db2.mcp.tools.properties;


import java.util.List;
import java.util.UUID;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ibm.systemz.db2.Activator;
import com.ibm.systemz.db2.ide.ConnectionEnvironment;
import com.ibm.systemz.db2.ide.ConnectionSummary;


public class Db2zOSMcpProperties  extends PropertyPage implements IWorkbenchPreferencePage, SelectionListener, IPreferenceConstants {

	
	Button enableWrites;
	Combo connectionCombo;
	List<ConnectionSummary> summaries;

	private Composite control;
	
	public Db2zOSMcpProperties() {
		super();
		setTitle("Db2 for z/OS");
	}
	
	@Override
	protected Control createContents(Composite parent) {
		
		summaries = ConnectionEnvironment.getLocationSummaries();
		
		control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(2, false));
		control.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, true));
		
		Label label = new Label(control, SWT.CHECK);
		label.setText("Use the following connection:");
		label.setLayoutData(new GridData());
		
		Combo connectionCombo = new Combo(control, SWT.READ_ONLY);
		connectionCombo.setText("Select one...");
		connectionCombo.setLayoutData(new GridData());
		connectionCombo.setItems(
				summaries.stream().
				map(summary -> summary.getName()).toArray(String[]::new));
		connectionCombo.addSelectionListener(this);
		
		Button enableWrites = new Button(control, SWT.CHECK);
		enableWrites.setText("Enable data modifications");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		enableWrites.setLayoutData(gd);
		
		control.setSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.systemz.db2.mcp.tools.properties.Db2zOSMcpProperties"); //$NON-NLS-1$

		loadPreferences();
		updateValidation();

		return control;
	}
	
	
	@Override
	protected void performApply() {
		savePreferences();
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
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getInstance().getPreferenceStore());
	}

	private void updateValidation() {
		String errorMessage = null;

		if (connectionCombo.getSelectionIndex() < 0) {
			errorMessage = "Select a Db2 for z/OS Connection";
		}

		setValid(errorMessage == null);
		setErrorMessage(errorMessage);

	}

	private void loadPreferences() {
		IPreferenceStore store = getPreferenceStore();

		
		enableWrites.setSelection("true".equals(store.getString(P_ENABLEWRITES)));
		
		String connectionId = store.getString(P_CONNECTIONID);
		if (connectionId != null && !connectionId.isBlank()) {
			UUID id = UUID.fromString(connectionId);
			for (int i = 0; i < summaries.size(); i++) {
				if (summaries.get(i).getId().equals(id)) {
					connectionCombo.select(i);
					break;
				}
			}
		}
	}

	private void savePreferences() {
		IPreferenceStore store = getPreferenceStore();

		store.setValue(P_ENABLEWRITES, enableWrites.getSelection() ? "true" : "false");
		if (connectionCombo.getSelectionIndex() > -1) {
			store.setValue(P_CONNECTIONID, summaries.get(connectionCombo.getSelectionIndex()).getId().toString());
		} else {
			store.setValue(P_CONNECTIONID, "");
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
		enableWrites.setSelection(false);
		connectionCombo.clearSelection();
		updateValidation();
	}

}
