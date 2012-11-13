package com.github.tlrx.elasticsearch.test.request;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.client.Client;

public interface Request<T> {

    T execute(Client client) throws ElasticSearchException;

}
