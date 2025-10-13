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
package org.eclipse.mcp.acp.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.mcp.platform.resource.EditorAdapter;
import org.eclipse.mcp.platform.resource.ResourceSchema.Editor;

public class ContentAssistProvider implements IContentProposalProvider, IContentProposalListener {

	@Override
	public IContentProposal[] getProposals(String content, int caret) {
		for (int i = caret - 1; i >=0; i--) {
			switch(content.charAt(i)) {
			case '#':
				return getContexts(content, i, caret);
			case '/':
				return getCommands(content, i, caret);
			case '@':
				return getExtensions(content, i, caret);
			}
		};
		return new IContentProposal[0];
	}
	
	
	private IContentProposal[] getExtensions(String content, int i, int caret) {
		return new IContentProposal[0];
	}

	private IContentProposal[] getCommands(String content, int i, int caret) {
		return new IContentProposal[0];
	}

	private IContentProposal[] getContexts(String content, int i, int caret) {
		List<IContentProposal> result = new ArrayList<IContentProposal>();
		for (Editor editor: EditorAdapter.getEditors().editors()) {
			result.add(new ResourceProposal(editor.name(), editor.name(), editor.editor().uri()));
		}
		return result.toArray(IContentProposal[]::new);
		
	}
	
	class ResourceProposal extends ContentProposal {

		String uri;
		String name;
		public ResourceProposal(String content, String name, String uri) {
			super(content);
			this.uri = uri;
			this.name = name;
		}
	}

	@Override
	public void proposalAccepted(IContentProposal arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
