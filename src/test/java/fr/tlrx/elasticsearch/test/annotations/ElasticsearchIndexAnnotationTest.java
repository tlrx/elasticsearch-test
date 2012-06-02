package fr.tlrx.elasticsearch.test.annotations;

import static org.junit.Assert.assertTrue;

import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.client.AdminClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test class for {@link ElasticsearchIndex} annotation.
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchIndexAnnotationTest {
	
	@ElasticsearchAdminClient
	AdminClient adminClient;
		
	@Test
	@ElasticsearchIndex
	public void testElasticsearchIndex(){
		// Checks if a default index has been created
		IndicesExistsResponse existResponse = adminClient.indices()
				.prepareExists(ElasticsearchIndex.DEFAULT_NAME)
				.execute().actionGet();
		
		assertTrue("Index must exist", existResponse.exists());
	}
	
	@Test
	@ElasticsearchIndex(indexName = "people")
	public void testElasticsearchIndexWithName(){
		// Checks if the index has been created
		IndicesExistsResponse existResponse = adminClient.indices()
				.prepareExists("people")
				.execute().actionGet();
		
		assertTrue("Index must exist", existResponse.exists());
	}
	
	@Test
	@ElasticsearchIndex(indexName = "people")
	public void testElasticsearchIndexAlreadyExist(){
		// Checks if the index has been found
		IndicesExistsResponse existResponse = adminClient.indices()
				.prepareExists("people")
				.execute().actionGet();
		
		assertTrue("Index must exist", existResponse.exists());
	}
}
