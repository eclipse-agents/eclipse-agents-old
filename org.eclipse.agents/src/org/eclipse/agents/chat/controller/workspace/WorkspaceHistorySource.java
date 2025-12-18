package org.eclipse.agents.chat.controller.workspace;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.team.ui.history.HistoryPageSource;
import org.eclipse.ui.part.Page;

public class WorkspaceHistorySource extends HistoryPageSource {

	@Override
	public boolean canShowHistoryFor(Object arg0) {
		return true;
	}

	@Override
	public Page createPage(Object input) {
		return new WorkspaceHistoryPage((DiffNode)input);
	}

}
