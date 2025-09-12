package org.eclipse.acp;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageProducer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor;

public class SocketLauncher<T> implements Launcher<T> {

    private final Launcher<T> launcher;

    /**
     * Create the entry point for LSP4J applications.
     *
     * @param localService    the object that receives method calls from the remote service
     * @param remoteInterface an interface on which RPC methods are looked up
     * @param socket          the socket used for listening and sending messages
     */
    public SocketLauncher(Object localService, Class<T> remoteInterface, Socket socket,
            ContextStore<T> store) {
        try {
            this.launcher = createLauncher(createBuilder(store), localService, remoteInterface,
                    socket.getInputStream(), socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Copy the createLauncher method, but pass in a custom builder
     */
    private static <T> Launcher<T> createLauncher(Builder<T> builder, Object localService,
            Class<T> remoteInterface, InputStream in, OutputStream out) {
        return builder.setLocalService(localService).setRemoteInterface(remoteInterface)
                .setInput(in).setOutput(out).create();
    }

    /*
     * The custom builder to be used when creating a launcher
     */
    private static <T> Builder<T> createBuilder(ContextStore<T> store) {
        return new Builder<T>() {
            @Override
            protected ConcurrentMessageProcessor createMessageProcessor(MessageProducer reader,
                    MessageConsumer messageConsumer, T remoteProxy) {
                return new ExtendedConcurrentMessageProcessor<T>(reader, messageConsumer,
                        remoteProxy, store);
            }
        };
    }

    @Override
    public CompletableFuture<Void> startListening() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.launcher.startListening().get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, Executors.newSingleThreadExecutor());
    }

    @Override
    public T getRemoteProxy() {
        return this.launcher.getRemoteProxy();
    }

    @Override
    public RemoteEndpoint getRemoteEndpoint() {
        return this.launcher.getRemoteEndpoint();
    }
}