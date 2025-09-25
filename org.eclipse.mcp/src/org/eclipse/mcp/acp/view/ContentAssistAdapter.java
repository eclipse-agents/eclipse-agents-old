package org.eclipse.mcp.acp.view;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Text;

public class ContentAssistAdapter extends ContentProposalAdapter {

	public ContentAssistAdapter(Text text)  {
		super(text, new TextContentAdapter(), new IContentProposalProvider() {

			@Override
			public IContentProposal[] getProposals(String arg0, int arg1) {
				return new IContentProposal[] {
					new ContentProposal("@abc")
				};
			}
			
		}, null, new char[] {'#', '@', '/' });
	}
}
