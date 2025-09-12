package org.eclipse.acp;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

@JsonSegment("client")
public interface IAcpClient {

	@JsonRequest
	String helloWorld(String firstName, String lastName);

	@JsonNotification
    void getDebuggerRunResult(String notice);

}

