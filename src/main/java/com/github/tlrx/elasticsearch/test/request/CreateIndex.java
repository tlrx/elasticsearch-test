package com.github.tlrx.elasticsearch.test.request;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import com.github.tlrx.elasticsearch.test.provider.JSONProvider;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateIndex implements Request<Void> {

    private final CreateIndexRequest request;
    private final String index;
    private final List<JSONProvider> bulks;

    public CreateIndex(String index) {
        this.index = index;
        request = new CreateIndexRequest(index);
        bulks = new ArrayList<JSONProvider>();
    }

    public CreateIndex withSettings(Settings settings) {
        request.settings(settings);
        return this;
    }

    public CreateIndex withSettings(String source) {
        Settings settings = ImmutableSettings.settingsBuilder()
                .loadFromSource(source)
                .build();
        withSettings(settings);
        return this;
    }

    public CreateIndex withSettings(JSONProvider jsonProvider) {
        withSettings(jsonProvider.toJson());
        return this;
    }

    public CreateIndex withMapping(String type, Map mapping) {
        request.mapping(type, mapping);
        return this;
    }

    public CreateIndex withMapping(String type, String source) {
        request.mapping(type, source);
        return this;
    }

    public CreateIndex withMapping(String type, JSONProvider jsonProvider) {
        withMapping(type, jsonProvider.toJson());
        return this;
    }

    public CreateIndex withSource(String source) {
        request.source(source);
        return this;
    }

    public CreateIndex withSource(JSONProvider jsonProvider) {
        request.source(jsonProvider.toJson());
        return this;
    }

    public CreateIndex withData(JSONProvider jsonProvider) {
        bulks.add(jsonProvider);
        return this;
    }

    @Override
    public Void execute(final Client client) throws ElasticSearchException {
        BulkRequestBuilder bulkRequestBuilder = null;
        try {
            if ((bulks != null) && (!bulks.isEmpty())) {
                bulkRequestBuilder = client.prepareBulk();
                for (JSONProvider jsonProvider : bulks) {
                    String content = jsonProvider.toJson();
                    bulkRequestBuilder.add(content.getBytes(), 0, content.length(), true, null, null);
                }
            }

            CreateIndexResponse response = client.admin().indices().create(request).get();
            if ((response.acknowledged()) && (bulkRequestBuilder != null)) {
                BulkResponse bulkResponse = bulkRequestBuilder.setRefresh(true).execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    throw new EsSetupRuntimeException("Bulk request has failures");
                }
            }
        } catch (Exception e) {
            throw new EsSetupRuntimeException(e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "create index [" +
                "index='" + index + '\'' +
                ']';
    }
}
