package com.github.tlrx.elasticsearch.test.provider;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;

public class DefaultClientProvider implements ClientProvider {

    private final Client client;
    private final boolean closeOnTerminate;

    public DefaultClientProvider(Client client, boolean closeClientOnTerminate) {
        this.closeOnTerminate = closeClientOnTerminate;
        if (client == null) {
            throw new EsSetupRuntimeException("Client must not be null");
        }
        this.client = client;
    }

    @Override
    public void open() {
        // Wait for Yellow status
        client().admin().cluster()
                .prepareHealth()
                .setWaitForYellowStatus()
                .setTimeout(TimeValue.timeValueMinutes(1))
                .execute()
                .actionGet();
    }

    @Override
    public Client client() {
        return client;
    }

    @Override
    public void close() {
        if (closeOnTerminate) {
            client().close();
        }
    }
}
