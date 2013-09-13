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
package com.github.tlrx.elasticsearch.test.provider;


import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.File;

/**
 * LocalClientProvider instantiates a local node with in-memory index store type.
 */
public class LocalClientProvider implements ClientProvider {

    private Node node = null;
    private Client client = null;
    private Settings settings = null;

    public LocalClientProvider() {
    }

    public LocalClientProvider(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void open() {
        if (node == null || node.isClosed()) {
            // Build and start the node
            node = NodeBuilder.nodeBuilder().settings(buildNodeSettings()).node();

            // Get a client
            client = node.client();

            // Wait for Yellow status
            client.admin().cluster()
                    .prepareHealth()
                    .setWaitForYellowStatus()
                    .setTimeout(TimeValue.timeValueMinutes(1))
                    .execute()
                    .actionGet();
        }
    }

    @Override
    public Client client() {
        return client;
    }

    @Override
    public void close() {
        if (client() != null) {
            client.close();
        }

        if ((node != null) && (!node.isClosed())) {
            node.close();

            FileSystemUtils.deleteRecursively(new File("./target/elasticsearch-test/"), true);
        }
    }

    protected Settings buildNodeSettings() {
        // Build settings
        ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
                .put("node.name", "node-test-" + System.currentTimeMillis())
                .put("node.data", true)
                .put("cluster.name", "cluster-test-" + NetworkUtils.getLocalAddress().getHostName())
                .put("index.store.type", "mmapfs")
                .put("gateway.type", "none")
                .put("path.data", "./target/elasticsearch-test/data")
                .put("path.work", "./target/elasticsearch-test/work")
                .put("path.logs", "./target/elasticsearch-test/logs")
                .put("index.number_of_shards", "1")
                .put("index.number_of_replicas", "0")
                .put("cluster.routing.schedule", "50ms")
                .put("node.local", true);

        if (settings != null) {
            builder.put(settings);
        }

        return builder.build();
    }
}
