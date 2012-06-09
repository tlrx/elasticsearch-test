package fr.tlrx.elasticsearch.test.annotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndexes;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test class for {@link ElasticsearchIndexes} annotation.
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchIndexesAnnotationTest {
	
	@ElasticsearchNode(local = false)
	Node node;
	
	@ElasticsearchAdminClient
	AdminClient adminClient;
		
	@Test
	@ElasticsearchIndexes(indexes = {})
	public void testElasticsearchIndex(){
		// Checks if a default index has been created
		IndicesExistsResponse existResponse = adminClient.indices()
				.prepareExists(ElasticsearchIndex.DEFAULT_NAME)
				.execute().actionGet();
		
		assertFalse("Index must not exist", existResponse.exists());
	}
	
	@Test
	@ElasticsearchIndexes(indexes = {@ElasticsearchIndex(indexName = "people")})	
	public void testElasticsearchIndexWithName(){
		// Checks if the index has been created
		IndicesExistsResponse existResponse = adminClient.indices()
				.prepareExists("people")
				.execute().actionGet();
		
		assertTrue("Index must exist", existResponse.exists());
	}
	
	@Test
	@ElasticsearchIndexes(indexes = {
			@ElasticsearchIndex(indexName = "people1"),
			@ElasticsearchIndex(indexName = "people2") })
	public void testElasticsearchIndexAlreadyExist(){
		// Checks if the index people1 has been found
		IndicesExistsResponse existResponse = adminClient.indices()
				.prepareExists("people1")
				.execute().actionGet();
		
		assertTrue("Index people1 must exist", existResponse.exists());
		
		// Checks if the index people2 has been found
		existResponse = adminClient.indices()
				.prepareExists("people2")
				.execute().actionGet();
		
		assertTrue("Index people2 must exist", existResponse.exists());
	}
}
