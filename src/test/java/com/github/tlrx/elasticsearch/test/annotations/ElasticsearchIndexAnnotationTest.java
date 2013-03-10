package com.github.tlrx.elasticsearch.test.annotations;

import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test class for {@link ElasticsearchIndex} annotation.
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchIndexAnnotationTest {

    @ElasticsearchNode(local = false)
    Node node;

    @ElasticsearchClient
    Client client;

    @ElasticsearchAdminClient
    AdminClient adminClient;

    @Test
    @ElasticsearchIndex
    public void testElasticsearchIndex() {
        // Checks if a default index has been created
        IndicesExistsResponse existResponse = adminClient.indices()
                .prepareExists(ElasticsearchIndex.DEFAULT_NAME)
                .execute().actionGet();

        assertTrue("Index must exist", existResponse.isExists());
    }

    @Test
    @ElasticsearchIndex(indexName = "people")
    public void testElasticsearchIndexWithName() throws ElasticSearchException, IOException {
        // Checks if the index has been created
        IndicesExistsResponse existResponse = adminClient.indices()
                .prepareExists("people")
                .execute().actionGet();

        assertTrue("Index must exist", existResponse.isExists());

        // Index a simple doc
        client.prepareIndex("people", "person", "1")
                .setSource(JsonXContent.contentBuilder().startObject().field("Name", "John Doe").endObject())
                .setRefresh(true)
                .execute()
                .actionGet();
    }

    @Test
    @ElasticsearchIndex(indexName = "people", cleanAfter = true)
    public void testElasticsearchCleanAfter1() {
        // Checks if the index has been found
        IndicesExistsResponse existResponse = adminClient.indices()
                .prepareExists("people")
                .execute().actionGet();

        assertTrue("Index must exist", existResponse.isExists());

        // Check if document is still here
        assertTrue("Document #1 must be found", client.prepareGet("people", "person", "1").execute().actionGet().isExists());
    }

    @Test
    @ElasticsearchIndex(indexName = "people")
    public void testElasticsearchCleanAfter2() throws ElasticSearchException, IOException {
        // Checks if the index has been found
        IndicesExistsResponse existResponse = adminClient.indices()
                .prepareExists("people")
                .execute().actionGet();

        assertTrue("Index must exist", existResponse.isExists());

        // Check that document has been cleaned/deleted after previous @Test method execution
        assertFalse("Document #1 must be found", client.prepareGet("people", "person", "1").execute().actionGet().isExists());

        // Index a simple doc
        client.prepareIndex("people", "person", "1")
                .setSource(JsonXContent.contentBuilder().startObject().field("Name", "John Doe").endObject())
                .setRefresh(true)
                .execute()
                .actionGet();
    }

    @Test
    @ElasticsearchIndex(indexName = "people")
    public void testElasticsearchCleanAfter3() {
        // Checks if the index has been found
        IndicesExistsResponse existResponse = adminClient.indices()
                .prepareExists("people")
                .execute().actionGet();

        assertTrue("Index must exist", existResponse.isExists());

        // Check if document is still here
        assertTrue("Document #1 must be found", client.prepareGet("people", "person", "1").execute().actionGet().isExists());
    }


    @Test
    @ElasticsearchIndex(indexName = "people", forceCreate = true)
    public void testElasticsearchForceCreate() {
        // Checks if the index has been found
        IndicesExistsResponse existResponse = adminClient.indices()
                .prepareExists("people")
                .execute().actionGet();

        assertTrue("Index must exist", existResponse.isExists());

        adminClient.cluster().prepareHealth("people").request().waitForGreenStatus();

        // Check that document has been cleaned/deleted after previous @Test method execution
        assertEquals("Document #1 must not exist", 0, client.prepareSearch("people").setQuery(QueryBuilders.idsQuery("person").addIds("1")).execute().actionGet().getHits().totalHits());
    }


    @Test
    @ElasticsearchIndex(indexName = "documents", settingsFile = "com/github/tlrx/elasticsearch/test/annotations/documents/settings.json")
    public void testElasticsearchSettingsFile() {
        // Check custom settings on index
        ClusterStateResponse response = adminClient.cluster().prepareState()
                .execute().actionGet();

        Settings indexSettings = response.getState().metaData().index("documents").settings();
        assertEquals("3", indexSettings.get("index.number_of_shards"));
        assertEquals("7", indexSettings.get("index.number_of_replicas"));
        assertEquals("true", indexSettings.get("index.analysis.filter.test_word_delimiter.split_on_numerics"));
    }
}
