package org.eclipse.agents.chat.controller.workspace;

public interface IWorkspaceChangeListener {

	public void changeAdded(String sessionId, WorkspaceChange change);
	public void changeModified(String sessionID, WorkspaceChange change);
	public void changeRemoved(String sessionId, WorkspaceChange change);

}
