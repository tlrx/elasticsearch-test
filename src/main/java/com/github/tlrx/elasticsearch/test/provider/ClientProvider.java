package com.github.tlrx.elasticsearch.test.provider;

import org.elasticsearch.client.Client;

public interface ClientProvider {

    void open();

    Client client();

    void close();
}
