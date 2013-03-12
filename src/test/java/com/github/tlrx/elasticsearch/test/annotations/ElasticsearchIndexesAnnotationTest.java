package com.github.tlrx.elasticsearch.test.annotations;

import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link ElasticsearchIndexes} annotation.
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchIndexesAnnotationTest {

    @ElasticsearchNode(local = false)
    Node node;

    @ElasticsearchAdminClient
    AdminClient adminClient;

    @Test
    @ElasticsearchIndexes(indexes = {@ElasticsearchIndex(indexName = "people")})
    public void testElasticsearchIndexWithName() {
        // Checks if the index has been created
        IndicesExistsResponse existResponse = adminClient.indices()
                .prepareExists("people")
                .execute().actionGet();

        assertTrue("Index must exist", existResponse.isExists());
    }

    @Test
    @ElasticsearchIndexes(indexes = {
            @ElasticsearchIndex(indexName = "people1"),
            @ElasticsearchIndex(indexName = "people2")})
    public void testElasticsearchIndexAlreadyExist() {
        // Checks if the index people1 has been found
        IndicesExistsResponse existResponse = adminClient.indices()
                .prepareExists("people1")
                .execute().actionGet();

        assertTrue("Index people1 must exist", existResponse.isExists());

        // Checks if the index people2 has been found
        existResponse = adminClient.indices()
                .prepareExists("people2")
                .execute().actionGet();

        assertTrue("Index people2 must exist", existResponse.isExists());
    }
}
