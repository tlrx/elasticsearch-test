package com.github.tlrx.elasticsearch.test.request;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;

public class Exists implements Request<Boolean> {

    private String index;
    private String type;
    private String id;

    public Exists(String index) {
        this.index = index;
    }

    public Exists(String index, String type, String id) {
        this(index);
        this.type = type;
        this.id = id;
    }

    @Override
    public Boolean execute(Client client) throws ElasticSearchException {
        if ((index != null) && (type != null) && (id != null)) {

            // Check if a document exists
            GetResponse response = client.prepareGet(index, type, id).setRefresh(true).execute().actionGet();
            return response.exists();

        } else {

            // Check if index exists
            IndicesExistsResponse response = client.admin().indices().prepareExists(index).execute().actionGet();
            return response.exists();
        }
    }

    @Override
    public String toString() {
        return "exists [" +
                "index='" + index + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                ']';
    }
}
