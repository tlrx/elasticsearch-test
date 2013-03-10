package com.github.tlrx.elasticsearch.test.annotations;

import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test class for {@link ElasticsearchTransportClient} annotation.
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchTransportClientAnnotationTest {

    @ElasticsearchNode(name = "node0", local = false, clusterName = "external", settings = @ElasticsearchSetting(name = "transport.tcp.port", value = "9500"))
    Node node0;

    @ElasticsearchTransportClient(clusterName = "external",
            hostnames = {"127.0.0.1"},
            ports = {9500})
    TransportClient client0;

    @Test
    @ElasticsearchIndex(indexName = "sites", nodeName = "node0")
    public void testTransportClient() {

        assertNotNull(node0);
        assertNotNull(client0);

        // Checks if the index has been created
        IndicesExistsResponse existResponse = client0.admin().indices()
                .prepareExists("sites")
                .execute().actionGet();
        assertTrue("Index must exist", existResponse.isExists());
    }

    @ElasticsearchNode(local = false)
    Node node1;

    @ElasticsearchTransportClient
    TransportClient client1;

    @Test
    @ElasticsearchIndex(indexName = "sites")
    public void testLocalTransportClient() {

        assertNotNull(node1);
        assertNotNull(client1);
        assertNotSame(node0, node1);
        assertNotSame(client0, client1);

        NodesInfoResponse nodeInfos = client1.admin().cluster().prepareNodesInfo().execute().actionGet();
        assertFalse(client1.connectedNodes().isEmpty());

        // Checks if the index has been created
        IndicesExistsResponse existResponse = client1.admin().indices()
                .prepareExists("sites")
                .execute().actionGet();
        assertTrue("Index must exist", existResponse.isExists());
    }
}
