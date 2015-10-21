/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.tlrx.elasticsearch.test.request;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import com.github.tlrx.elasticsearch.test.provider.JSONProvider;
import org.elasticsearch.ElasticsearchException;
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

/**
 * A {@link Request} used to create indices.
 */
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
    public Void execute(final Client client) throws ElasticsearchException {
        BulkRequestBuilder bulkRequestBuilder = null;
        try {
            if ((bulks != null) && (!bulks.isEmpty())) {
                bulkRequestBuilder = client.prepareBulk();
                for (JSONProvider jsonProvider : bulks) {
                    byte[] content = jsonProvider.toJson().getBytes("UTF-8");
                    bulkRequestBuilder.add(content, 0, content.length);
                }
            }

            CreateIndexResponse response = client.admin().indices().create(request).get();
            if ((response.isAcknowledged()) && (bulkRequestBuilder != null)) {
                BulkResponse bulkResponse = bulkRequestBuilder.setRefresh(true).execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    throw new EsSetupRuntimeException("Bulk request has failures: "+bulkResponse.buildFailureMessage());
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
