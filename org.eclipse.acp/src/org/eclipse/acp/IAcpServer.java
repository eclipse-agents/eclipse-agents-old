package org.eclipse.acp;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

@JsonSegment("server")
public interface IAcpServer {

    @JsonRequest
    String[] getNames(String userId);

    
    @JsonNotification
    void setName(String name);

}
