package com.github.tlrx.elasticsearch.test.request;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateIndex implements Request<Void> {

    private final CreateIndexRequest request;
    private final String index;
    private final List<String> bulks;

    public CreateIndex(String index) {
        this.index = index;
        request = new CreateIndexRequest(index);
        bulks = new ArrayList<String>();
    }

    public CreateIndex withSettings(String resourceName) {
        Settings settings = ImmutableSettings.settingsBuilder()
                .loadFromClasspath(resourceName)
                .build();
        withSettings(settings);
        return this;
    }

    public CreateIndex withSettings(Settings settings) {
        request.settings(settings);
        return this;
    }

    public CreateIndex withMapping(String type, String resourceName) {
        try {
            String mapping = Streams.copyToStringFromClasspath(getClass().getClassLoader(), resourceName);
            request.mapping(type, mapping);
        } catch (IOException e) {
            throw new EsSetupRuntimeException(e);
        }
        return this;
    }

    public CreateIndex withMapping(String type, Map mapping) {
        request.mapping(type, mapping);
        return this;
    }

    public CreateIndex withSource(String resourceName) {
        try {
            String source = Streams.copyToStringFromClasspath(getClass().getClassLoader(), resourceName);
            request.source(source);
        } catch (IOException e) {
            throw new EsSetupRuntimeException(e);
        }
        return this;
    }

    public CreateIndex withData(String bulkFile) {
        bulks.add(bulkFile);
        return this;
    }

    @Override
    public Void execute(final Client client) throws ElasticSearchException {
        BulkRequestBuilder bulkRequestBuilder = null;
        try {
            if ((bulks != null) && (!bulks.isEmpty())) {
                bulkRequestBuilder = client.prepareBulk();
                for (String bulk : bulks) {
                    String content = Streams.copyToStringFromClasspath(getClass().getClassLoader(), bulk);
                    bulkRequestBuilder.add(content.getBytes(), 0, content.length(), true, null, null);
                }
            }

            CreateIndexResponse response = client.admin().indices().create(request).get();
            if ((response.acknowledged()) && (bulkRequestBuilder != null)) {
                bulkRequestBuilder.setRefresh(true).execute().actionGet();
            }
        } catch (Exception e) {
            throw new EsSetupRuntimeException(e);
        }
        return null;
    }
}
