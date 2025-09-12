package org.eclipse.acp;

import org.eclipse.acp.ContextStore.Context;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageProducer;
import org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor;

public class ExtendedConcurrentMessageProcessor<T> extends ConcurrentMessageProcessor {

	private T remoteProxy;
    private final ContextStore<T> threadMap;

    public ExtendedConcurrentMessageProcessor(MessageProducer messageProducer,
            MessageConsumer messageConsumer, T remoteProxy, ContextStore<T> threadMap) {
        super(messageProducer, messageConsumer);
        this.remoteProxy = remoteProxy;
        this.threadMap = threadMap;
    }

    @Override
    protected void processingStarted() {
        super.processingStarted();
        if (threadMap != null) {
            threadMap.setContext(new Context<T>(remoteProxy));
        }
    }

    @Override
    protected void processingEnded() {
        super.processingEnded();
        if (threadMap != null) {
            threadMap.clear();
        }
    }
}
