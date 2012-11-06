package com.github.tlrx.elasticsearch.test.annotations;

import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * Test class for {@link ElasticsearchAdminClient} annotation.
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
@ElasticsearchNode
public class ElasticsearchAdminClientAnnotationTest {

    @ElasticsearchAdminClient
    AdminClient adminClient0;

    @ElasticsearchNode(name = "node1")
    Node node1;

    @ElasticsearchAdminClient(nodeName = "node1")
    AdminClient adminClient1;

    @Test
    public void testElasticsearchAdminClients() {
        assertNotNull(adminClient0);

        assertNotNull(node1);
        assertNotNull(adminClient1);

        assertNotSame(adminClient0, adminClient1);
    }
}
