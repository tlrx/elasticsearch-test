/**
 * 
 */
package fr.tlrx.elasticsearch.samples.indices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndexes;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test Java API / Indices : Aliases
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public class AliasesTest {

	@ElasticsearchNode(name = "node0", local = false, data = true)
	Node node0;

	@ElasticsearchNode(name = "node1", local = false, data = true)
	Node node1;

	@ElasticsearchAdminClient(nodeName = "node0")
	AdminClient adminClient;

	@ElasticsearchClient(nodeName = "node1")
	Client client;

	@Test
	@ElasticsearchIndexes(indexes = {
			@ElasticsearchIndex(indexName="library1"),
			@ElasticsearchIndex(indexName="library2"),
			@ElasticsearchIndex(indexName="library3")
	})
	public void testAliases() throws IOException {
		
		// Create an alias "library" that targets the index "library1"
		IndicesAliasesResponse aliasReponse = adminClient.indices().prepareAliases()
																.addAlias("library1", "library")
																.execute()
																.actionGet();
		assertTrue(aliasReponse.acknowledged());

		// Index book #1 in the index "library1"
		IndexResponse indexResponse = client
				.prepareIndex("library1", "book", "1")
				.setRefresh(true)				
				.setSource(
						JsonXContent.contentBuilder().startObject()
								.field("title", "Les Miserables")
								.field("author", "Victor Hugo").endObject())
				.execute().actionGet();
		assertNotNull(indexResponse.id());
		
		// Retrieves book #1 on index "library1"
		GetResponse getResponse = client.prepareGet("library1", "book", "1").execute().actionGet();
		assertTrue(getResponse.exists());
				
		// Retrieves book #1 on alias "library"
		getResponse = client.prepareGet("library", "book", "1").execute().actionGet();
		assertTrue(getResponse.exists());
		
		// Index another book #1 in the index "library2"
		indexResponse = client
				.prepareIndex("library2", "book", "1")
				.setRefresh(true)
				.setSource(
						JsonXContent.contentBuilder().startObject()
								.field("title", "Notre-Dame de Paris")
								.field("author", "Victor Hugo").endObject())
				.execute().actionGet();
		assertNotNull(indexResponse.id());
		
		// Retrieves another book #1 on index "library2"
		getResponse = client.prepareGet("library2", "book", "1").execute().actionGet();
		assertTrue(getResponse.exists());
		
		// Create an alias "library" that targets the index "library2"
		aliasReponse = adminClient.indices().prepareAliases()
													.addAlias("library2", "library")
													.execute()
													.actionGet();
		assertTrue(aliasReponse.acknowledged());
		
		// Search for all books in alias "library"
		SearchResponse searchResponse = client.prepareSearch("library").execute().actionGet();
		assertEquals(2, searchResponse.hits().totalHits());
	}
}
