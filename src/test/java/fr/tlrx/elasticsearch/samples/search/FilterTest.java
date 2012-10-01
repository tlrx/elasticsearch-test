/**
 * 
 */
package fr.tlrx.elasticsearch.samples.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchBulkRequest;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMapping;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField.Store;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField.Types;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test Java API / Search : Filters
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
@ElasticsearchNode
public class FilterTest {

	private static final String INDEX = "books";
	
	@ElasticsearchClient
	Client client;

	@Test
	@ElasticsearchIndex(indexName = INDEX, 
						mappings = {@ElasticsearchMapping(typeName = "book",
										properties = {
											@ElasticsearchMappingField(name = "title", store = Store.Yes, type = Types.String),
											@ElasticsearchMappingField(name = "tags", store = Store.Yes, type = Types.String),
											@ElasticsearchMappingField(name = "year", store = Store.Yes, type = Types.Integer),
											@ElasticsearchMappingField(name = "author.firstname", store = Store.Yes, type = Types.String),
											@ElasticsearchMappingField(name = "author.lastname", store = Store.Yes, type = Types.String)
										})
						})
	@ElasticsearchBulkRequest(dataFile = "fr/tlrx/elasticsearch/samples/search/FilterTest.json")
	public void testFilters() throws IOException {

		// Verify if bulk import succeed
		SearchResponse response = client.prepareSearch(INDEX).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
		assertEquals(7L, response.hits().totalHits());
		
		// Search for match_all and filter "tags:french"
		response = client.prepareSearch(INDEX)
				.setQuery(QueryBuilders.matchAllQuery())
				.setFilter(FilterBuilders.termFilter("tags", "french"))
				.execute()
				.actionGet();
		assertEquals(7L, response.hits().totalHits());
		
		// Search for match_all and filter "tags:poetry"
		response = client.prepareSearch(INDEX)
				.setQuery(QueryBuilders.matchAllQuery())
				.setFilter(FilterBuilders.termFilter("tags", "poetry"))
				.execute()
				.actionGet();
		assertEquals(3L, response.hits().totalHits());
		
		// Search for match_all and filter "tags:literature" and "year:1829"
		response = client.prepareSearch(INDEX)
				.setQuery(QueryBuilders.matchAllQuery())
				.setFilter(FilterBuilders.andFilter(
						FilterBuilders.termFilter("tags", "literature"),
						FilterBuilders.termFilter("year", "1829")))
				.execute()
				.actionGet();
		assertEquals(2L, response.hits().totalHits());
		
	}
}
