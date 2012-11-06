package com.github.tlrx.elasticsearch.test.annotations;

import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * Test class for {@link ElasticsearchClient} annotation.
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
@ElasticsearchNode
public class ElasticsearchClientAnnotationTest {

    @ElasticsearchClient
    Client client0;

    @ElasticsearchNode(name = "node1")
    Node node1;

    @ElasticsearchClient(nodeName = "node1")
    Client client1;

    @Test
    public void testElasticsearchClients() {
        assertNotNull(client0);

        assertNotNull(node1);
        assertNotNull(client1);

        assertNotSame(client0, client1);
    }
}
