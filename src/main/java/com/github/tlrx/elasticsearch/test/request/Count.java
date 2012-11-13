package com.github.tlrx.elasticsearch.test.request;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;

public class Count implements Request<Long> {

    private String[] indices;

    public Count() {
    }

    public Count(String... indices) {
        this.indices = indices;
    }

    @Override
    public Long execute(Client client) throws ElasticSearchException {
        CountResponse response = client.prepareCount(indices).execute().actionGet();
        return response.count();
    }
}
