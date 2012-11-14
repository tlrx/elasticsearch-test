package com.github.tlrx.elasticsearch.test.request;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.client.Client;

import java.util.Arrays;

public class DeleteIndices implements Request<Void> {

    private String[] indices;

    public DeleteIndices(String... indices) {
        this.indices = indices;
    }

    @Override
    public Void execute(Client client) throws ElasticSearchException {
        client.admin().indices().prepareDelete(indices).execute().actionGet();
        return null;
    }

    @Override
    public String toString() {
        return "delete indices [" +
                "indices=" + (indices == null ? null : Arrays.asList(indices)) +
                ']';
    }
}
