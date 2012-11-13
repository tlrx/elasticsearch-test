package com.github.tlrx.elasticsearch.test.provider;


import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.File;

public class LocalClientProvider implements ClientProvider {

    private Node node = null;
    private Client client = null;

    @Override
    public void open() {
        if (node == null || node.isClosed()) {
            // Build and start the node
            node = NodeBuilder.nodeBuilder().settings(buildNodeSettings()).local(false).node();

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
        client().close();

        if (!node.isClosed()) {
            node.close();

            FileSystemUtils.deleteRecursively(new File("./target/elasticsearch-test/"), true);
        }
    }

    protected Settings buildNodeSettings() {
        // Build default settings
        ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
                .put("node.name", "node-test")
                .put("node.data", true)
                .put("cluster.name", "cluster-test")
                .put("index.store.type", "memory")
                .put("index.store.fs.memory.enabled", "true")
                .put("gateway.type", "none")
                .put("http.enabled", "true")
                .put("path.data", "./target/elasticsearch-test/data")
                .put("path.work", "./target/elasticsearch-test/work")
                .put("path.logs", "./target/elasticsearch-test/logs")
                .put("index.number_of_shards", "1")
                .put("index.number_of_replicas", "0");
        return builder.build();
    }
}
