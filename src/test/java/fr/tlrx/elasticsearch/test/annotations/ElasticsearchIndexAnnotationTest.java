package fr.tlrx.elasticsearch.test.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test class for {@link ElasticsearchIndex} annotation.
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchIndexAnnotationTest {
	
	@ElasticsearchNode(local=false)
	Node node;
	
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
	
	@Test
	@ElasticsearchIndex(indexName = "documents", settingsFile = "fr/tlrx/elasticsearch/test/annotations/documents/2settings.json")
	public void testElasticsearchSettingsFile() {
		// Check custom settings on index
		ClusterStateResponse response = adminClient.cluster().prepareState()
				.execute().actionGet();
		
		Settings indexSettings = response.state().metaData().index("documents").settings();
		assertEquals("3", indexSettings.get("index.number_of_shards"));
		assertEquals("7", indexSettings.get("index.number_of_replicas"));
		assertEquals("true", indexSettings.get("index.analysis.filter.test_word_delimiter.split_on_numerics"));
		fail("Sucks on mappings");
	}
}
