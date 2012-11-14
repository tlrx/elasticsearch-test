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

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.client.Client;

import java.util.Arrays;

/**
 * A {@link Request} used to delete one or more indices.
 */
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
