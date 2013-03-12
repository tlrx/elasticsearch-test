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
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;

/**
 * A {@link Request} used to check if a document or index exists.
 */
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
            return response.isExists();

        } else {

            // Check if index exists
            IndicesExistsResponse response = client.admin().indices().prepareExists(index).execute().actionGet();
            return response.isExists();
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
