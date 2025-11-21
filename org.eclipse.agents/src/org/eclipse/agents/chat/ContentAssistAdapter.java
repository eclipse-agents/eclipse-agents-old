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

import org.eclipse.agents.Tracer;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Text;

public class ContentAssistAdapter extends ContentProposalAdapter implements IContentProposalListener {

	
	public ContentAssistAdapter(Text text)  {
		super(text, new TextContentAdapter(), new ContentAssistProvider(), null, new char[] {'#', '@', '/' });
		addContentProposalListener(this);
	}

	@Override
	public void proposalAccepted(IContentProposal proposal) {
		Tracer.trace().trace(Tracer.CHAT, proposal.getLabel());
	}
}
