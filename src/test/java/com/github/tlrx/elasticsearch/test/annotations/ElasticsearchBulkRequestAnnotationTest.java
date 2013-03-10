package com.github.tlrx.elasticsearch.test.annotations;

import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link ElasticsearchBulkRequest} annotation.
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchBulkRequestAnnotationTest {

    @ElasticsearchNode(local = false)
    Node node;

    @ElasticsearchClient
    Client client;

    @Test
    @ElasticsearchIndex(indexName = "documents", forceCreate = true)
    @ElasticsearchBulkRequest(dataFile = "com/github/tlrx/elasticsearch/test/annotations/documents/bulk1.json")
    public void testElasticsearchBulkRequest1() {
        // Count number of documents
        CountResponse countResponse = client.prepareCount("documents").setTypes("doc1").execute().actionGet();
        assertEquals(6, countResponse.getCount());
    }

    @Test
    @ElasticsearchIndex(indexName = "documents")
    @ElasticsearchBulkRequest(dataFile = "com/github/tlrx/elasticsearch/test/annotations/documents/bulk2.json")
    public void testElasticsearchBulkRequest2() {
        // Count number of documents
        CountResponse countResponse = client.prepareCount("documents").setTypes("doc1").execute().actionGet();
        assertEquals(9, countResponse.getCount());
    }
}
