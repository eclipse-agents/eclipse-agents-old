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
package org.eclipse.agents.chat.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.agents.services.AcpService;
import org.eclipse.agents.services.agent.IAgentService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;


public class ModelToolbarSelector extends WorkbenchWindowControlContribution implements SelectionListener {

	private static boolean handleEvents = true;
	private CCombo combo;
	
	private static String NO_SELECTION = "Model...";
	private static Set<ModelToolbarSelector> selectors = new HashSet<ModelToolbarSelector>();
	

	@Override
	protected Control createControl(Composite parent) {

		// Menu Window > New Window to create multiple workbench windows and multiple
		// controls

		// -- Configure Listeners --


		// -- Configure Widget --
		combo = new CCombo(parent, SWT.READ_ONLY);
		combo.setBackground(parent.getBackground());
		combo.setForeground(parent.getForeground());
		combo.setText("Pick Me");
		combo.addSelectionListener(this);

		selectors.add(this);
		return combo;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (handleEvents) {
			if (combo.getSelectionIndex() > -1) {
				combo.getSelectionIndex();
			}
		}
	}

	public static void refresh() {
		handleEvents = false;

		try {

			IAgentService[] services = AcpService.instance().getAgents();
			String[] items = new String[services.length];
			for (int i = 0; i < services.length; i++) {
				items[i] = services[i].getName();
			}
			
			for (ModelToolbarSelector selector : selectors.toArray(new ModelToolbarSelector[0])) {
				if (selector.combo != null) {
					if (selector.combo.isDisposed()) {
						selectors.remove(selector);
					} else {
						selector.combo.setItems(items);
						selector.combo.setText(NO_SELECTION);
					
						IAgentService service = AcpService.instance().getAgentService();
						if (service != null) {
							for (int i = 0; i < services.length; i++) {
								if (services[i] == service) {
									selector.combo.select(i);
									selector.combo.setToolTipText(service.getName());
								}
							}
						}
					}		
				}
			}
		} finally {
			handleEvents = true;
		}
	}
}
