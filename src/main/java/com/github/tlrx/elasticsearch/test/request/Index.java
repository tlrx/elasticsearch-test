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
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

/**
 * A {@link com.github.tlrx.elasticsearch.test.request.Request} used to index documents.
 */
public class Index implements Request<Void> {

    private final IndexRequest request;

    public Index(String index, String type) {
        request = new IndexRequest(index, type).refresh(true);
    }

    public Index(String index, String type, String id) {
        this(index, type);
        request.id(id);
    }

    public Index withSource(String source) {
        request.source(source);
        return this;
    }

    public Index withSource(JSONProvider jsonProvider) {
        request.source(jsonProvider.toJson());
        return this;
    }

    @Override
    public Void execute(final Client client) throws ElasticsearchException {
        try {
            IndexResponse response = client.index(request).get();
        } catch (Exception e) {
            throw new EsSetupRuntimeException(e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "index [" +
                "index='" + request.index() + "\'," +
                "type='" + request.type() + '\'' +
                ']';
    }
}
