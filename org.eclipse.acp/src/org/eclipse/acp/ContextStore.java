package org.eclipse.acp;

public class ContextStore<T> {
    private ThreadLocal<Context<T>> store = new ThreadLocal<>();

    public void setContext(Context<T> context) {
        store.set(context);
    }

    public Context<T> getContext() {
        return store.get();
    }

    public void clear() {
        store.remove();
    }

    public static class Context<T> {
        T remoteProxy;

        public Context(T remoteProxy) {
            this.remoteProxy = remoteProxy;
        }

        public T getRemoteProxy() {
            return this.remoteProxy;
        }
    }
}