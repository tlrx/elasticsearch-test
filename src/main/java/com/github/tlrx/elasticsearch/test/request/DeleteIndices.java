package com.github.tlrx.elasticsearch.test.request;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.client.Client;

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
}
