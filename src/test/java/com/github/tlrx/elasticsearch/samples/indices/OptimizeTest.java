package com.github.tlrx.elasticsearch.samples.indices;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.shard.DocsStats;
import org.elasticsearch.node.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Test Java API / Indices : Optimize
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
public class OptimizeTest {

    private final static Logger LOGGER = Logger.getLogger(OptimizeTest.class.getName());

    @ElasticsearchNode
    Node node0;

    @ElasticsearchClient
    Client client;

    @ElasticsearchAdminClient
    AdminClient admin;

    private static int NB = 500;
    private static String INDEX = "my_index";
    private static String TYPE = "my_type";
    private static int deleted = 0;

    @Before
    public void setUp() throws IOException {

        // Creates NB documents
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        for (int i = 0; i < NB; i++) {
            IndexRequest indexRequest = new IndexRequest(INDEX)
                    .type(TYPE)
                    .id(String.valueOf(i))
                    .source(JsonXContent.contentBuilder()
                            .startObject()
                            .field("title", "Object #" + i)
                            .endObject()
                    );
            bulkRequestBuilder.add(indexRequest);
        }

        BulkResponse bulkResponse = bulkRequestBuilder.setRefresh(true).execute().actionGet();
        LOGGER.info(String.format("Bulk request executed in %d ms, %d document(s) indexed, failures : %s.\r\n", bulkResponse.getTookInMillis(), NB, bulkResponse.hasFailures()));

        // Deletes some documents
        for (int i = 0; i < NB; i = i + 9) {
            DeleteResponse deleteResponse = client
                    .prepareDelete(INDEX, TYPE, String.valueOf(i))
                    .setRefresh(true)
                    .execute()
                    .actionGet();

            if (!deleteResponse.isFound()) {
                LOGGER.info(String.format("Unable to delete document [id:%d], not found.\r\n", i));
            } else {
                deleted++;
                LOGGER.info(String.format("Document [id:%d] deleted.\r\n", i));
            }
        }
        LOGGER.info(String.format("%d document(s) deleted.\r\n", deleted));
    }

    @Test
    public void testOptimize() {

        // Count documents number
        CountResponse countResponse = client.prepareCount(INDEX).setTypes(TYPE).execute().actionGet();
        assertEquals((NB - deleted), countResponse.getCount());

        // Retrieves document status for the index
        IndicesStatsResponse status = admin.indices().prepareStats(INDEX).execute().actionGet();
        DocsStats docsStats = status.getIndex(INDEX).getTotal().getDocs();

        // Check docs status
        LOGGER.info(String.format("DocsStats before optimize: %d numDocs, %d deletedDocs\r\n", docsStats.getCount(), docsStats.getDeleted()));
        assertEquals((NB - deleted), docsStats.getCount());
        assertEquals(deleted, docsStats.getDeleted());

        // Now optimize the index
        admin.indices().prepareForceMerge(INDEX)
                .setFlush(true)
                .setOnlyExpungeDeletes(true)
                .execute()
                .actionGet();

        // Retrieves document status gain
        docsStats = admin.indices().prepareStats(INDEX).execute().actionGet().getIndex(INDEX).getTotal().getDocs();

        // Check again docs status
        LOGGER.info(String.format("DocsStats after optimize: %d numDocs, %d deletedDocs\r\n", docsStats.getCount(), docsStats.getDeleted()));
        assertEquals((NB - deleted), docsStats.getCount());
        // Must be zero
        assertEquals(0, docsStats.getDeleted());
    }
}
