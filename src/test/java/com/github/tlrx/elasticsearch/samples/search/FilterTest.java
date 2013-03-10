/**
 *
 */
package com.github.tlrx.elasticsearch.samples.search;

import com.github.tlrx.elasticsearch.test.annotations.*;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField.Store;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField.Types;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Test Java API / Search : Filters
 *
 * @author tlrx
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
    @ElasticsearchBulkRequest(dataFile = "com/github/tlrx/elasticsearch/samples/search/FilterTest.json")
    public void testFilters() throws IOException {

        // Verify if bulk import succeed
        SearchResponse response = client.prepareSearch(INDEX).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        assertEquals(7L, response.getHits().totalHits());

        // Search for match_all and filter "tags:french"
        response = client.prepareSearch(INDEX)
                .setQuery(QueryBuilders.matchAllQuery())
                .setFilter(FilterBuilders.termFilter("tags", "french"))
                .execute()
                .actionGet();
        assertEquals(7L, response.getHits().totalHits());

        // Search for match_all and filter "tags:poetry"
        response = client.prepareSearch(INDEX)
                .setQuery(QueryBuilders.matchAllQuery())
                .setFilter(FilterBuilders.termFilter("tags", "poetry"))
                .execute()
                .actionGet();
        assertEquals(3L, response.getHits().totalHits());

        // Search for match_all and filter "tags:literature" and "year:1829"
        response = client.prepareSearch(INDEX)
                .setQuery(QueryBuilders.matchAllQuery())
                .setFilter(FilterBuilders.andFilter(
                        FilterBuilders.termFilter("tags", "literature"),
                        FilterBuilders.termFilter("year", "1829")))
                .execute()
                .actionGet();
        assertEquals(2L, response.getHits().totalHits());

    }
}
