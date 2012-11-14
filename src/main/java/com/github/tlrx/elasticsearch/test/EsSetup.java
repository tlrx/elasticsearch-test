package com.github.tlrx.elasticsearch.test;

import com.github.tlrx.elasticsearch.test.provider.ClientProvider;
import com.github.tlrx.elasticsearch.test.provider.DefaultClientProvider;
import com.github.tlrx.elasticsearch.test.provider.LocalClientProvider;
import com.github.tlrx.elasticsearch.test.request.*;
import org.elasticsearch.client.Client;

public class EsSetup {

    private final ClientProvider provider;

    private EsSetup(ClientProvider clientProvider) {
        if (clientProvider == null) {
            throw new EsSetupRuntimeException("No client provider must not be null");
        }
        provider = clientProvider;
    }

    public EsSetup() {
        this(new LocalClientProvider());
    }

    public EsSetup(Client client) {
        this(new DefaultClientProvider(client, false));
    }

    public EsSetup(Client client, boolean closeClientOnTerminate) {
        this(new DefaultClientProvider(client, closeClientOnTerminate));
    }

    public EsSetup execute(Request... requests) {
        provider.open();
        for (Request request : requests) {
            doExecute(request);
        }
        return this;
    }

    private <T> T doExecute(Request request) {
        if (request == null) {
            throw new EsSetupRuntimeException("Request must not be null");
        }
        provider.open();
        return (T) request.execute(provider.client());
    }

    public void terminate() {
        provider.close();
    }

    public static CreateIndex createIndex(String index) {
        return new CreateIndex(index);
    }

    public static DeleteIndices deleteIndex(String index) {
        return new DeleteIndices(index);
    }

    public static DeleteIndices deleteIndices(String... indices) {
        return new DeleteIndices(indices);
    }

    public static DeleteIndices deleteAll() {
        return new DeleteIndices();
    }

    public Boolean exists(String index) {
        return doExecute(new Exists(index));
    }

    public Boolean exists(String index, String type, String id) {
        return doExecute(new Exists(index, type, id));
    }

    public Long countAll() {
        return doExecute(new Count());
    }

    public Long count(String... indices) {
        return doExecute(new Count(indices));
    }

    public static CreateTemplate createTemplate(String name) {
        return new CreateTemplate(name);
    }
}
