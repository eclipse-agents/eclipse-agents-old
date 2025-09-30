package org.eclipse.mcp.acp.view;

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
		System.out.println(proposal);
	}
}
