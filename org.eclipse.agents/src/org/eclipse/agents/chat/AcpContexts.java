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
package org.eclipse.agents.chat;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.agents.Activator;
import org.eclipse.agents.contexts.adapters.IResourceTemplate;
import org.eclipse.agents.contexts.platform.resource.WorkspaceResourceAdapter;
import org.eclipse.agents.services.protocol.AcpSchema.Annotations;
import org.eclipse.agents.services.protocol.AcpSchema.ContentBlock;
import org.eclipse.agents.services.protocol.AcpSchema.EmbeddedResourceBlock;
import org.eclipse.agents.services.protocol.AcpSchema.ResourceLinkBlock;
import org.eclipse.agents.services.protocol.AcpSchema.Role;
import org.eclipse.agents.services.protocol.AcpSchema.TextResourceContents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import io.modelcontextprotocol.spec.McpSchema.ResourceLink;

public class AcpContexts extends Composite {

	Map<String, ContentBlock> contexts;
	Map<String, Chips> chips;
	
	public AcpContexts(Composite parent, int style) {
		super(parent, style);
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setLayout(new GridLayout(3, false));
		
		contexts = new HashMap<String, ContentBlock>();
		chips = new HashMap<String, Chips>();
	}
	
	public void addEmbeddedResourceContext(String name, String uri) {
		if (!contexts.containsKey(uri)) {
			IResourceTemplate<?, ?> resourceTemplate = Activator.getDefault().getServerManager().getResourceTemplate(uri);
			if (resourceTemplate != null) {
				ResourceLink link = resourceTemplate.toResourceLink();
				TextResourceContents contents = new TextResourceContents(
						null, 
						link.mimeType(),
						resourceTemplate.toContent(),
						uri);
				EmbeddedResourceBlock block = new EmbeddedResourceBlock(null, null, contents, "resource");
				contexts.put(uri, block);
				
				Chips chip = new Chips(this, SWT.CLOSE);
				chip.setText(name);
				chip.addCloseListener(e->{
					contexts.remove(uri);
					chip.dispose();
				});
				chips.put(uri, chip);
				
				getParent().getParent().layout(true, true);
			}
		}
	}
	
	public void addLinkedResourceContext(String name, String uri) {
		if (!contexts.containsKey(uri)) {
			WorkspaceResourceAdapter resourceAdapter = new WorkspaceResourceAdapter(uri);
			
			ResourceLink link = resourceAdapter.toResourceLink();

			List<Role> roles = new ArrayList<Role>();
			Double priority = null;
			Integer size = null;
			
			if (link.size() != null) {
				size = link.size().intValue();
			}
			if (link.annotations() != null) {
				for (int i =0; i<link.annotations().audience().size(); i++) {
					roles.add(Role.valueOf(link.annotations().audience().get(i).name()));
				}
				priority = link.annotations().priority();
			} else {
				roles.add(Role.assistant);
				roles.add(Role.user);
			}
			
			Map<String, Object> meta = new HashMap<String, Object>();
//			if (resourceAdapter.getModel() instanceof IFile) {
//				meta.put("icon", "fa-file");
//			} else if (resourceAdapter.getModel() instanceof IProject) {
//				meta.put("icon", "fa-folder-open");
//			} else if (resourceAdapter.getModel() instanceof IFolder) {
//				meta.put("icon", "fa-folder");
//			}
			
			long timestampMillis = resourceAdapter.getModel().getModificationStamp();
			Instant instant = Instant.ofEpochMilli(timestampMillis);
			String lastModified = DateTimeFormatter.ISO_INSTANT.format(instant);
			
			ResourceLinkBlock block = new ResourceLinkBlock(
					meta,
					new Annotations(null, roles.toArray(Role[]::new), lastModified, priority),
					link.description(),
					link.mimeType(),
					link.name(),
					size,
					link.title(),
					"resource_link",
					link.uri());
			
			
			contexts.put(uri, block);
			
			Chips chip = new Chips(this, SWT.CLOSE);
			chip.setText(name);
			chip.addCloseListener(e->{
				contexts.remove(uri);
				chip.dispose();
			});
			chips.put(uri, chip);
			
			getParent().getParent().layout(true, true);
		}
	}
	
	public Collection<ContentBlock> getContextBlocks() {
		return contexts.values();
	}
	
	public void clearAcpContexts() {
		for (Chips chip: chips.values()) {
			if (!chip.isDisposed()) {
				chip.dispose();
			}
		}
		chips.clear();
		contexts.clear();
		getParent().getParent().layout(true, true);
	}
	
}
