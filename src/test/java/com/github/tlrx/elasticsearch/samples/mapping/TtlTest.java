package com.github.tlrx.elasticsearch.samples.mapping;

import com.github.tlrx.elasticsearch.test.annotations.*;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField.Types;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test Java API / Mappings / Fields : TTL
 *
 * @author Tanguy Leroux
 */
@RunWith(ElasticsearchRunner.class)
public class TtlTest {

    @ElasticsearchNode
    Node node;

    @ElasticsearchClient
    Client client;

    @ElasticsearchIndex(indexName = "events",
            mappings = {
                    @ElasticsearchMapping(typeName = "event",
                            ttl = true,
                            ttlValue = "20s",
                            properties = {
                                    @ElasticsearchMappingField(name = "title", type = Types.String)
                            })
            })
    @Test
    public void testTTL() throws IOException {

        XContentBuilder builder = JsonXContent.contentBuilder();

        // Event #1
        builder.startObject().field("title", "Poke you!").endObject();

        // Index event #1
        IndexResponse indexResponse1 = client.prepareIndex("events", "event")
                .setSource(builder)
                .setRefresh(true)
                .execute()
                .actionGet();

        // Event #2
        builder = JsonXContent.contentBuilder();
        builder.startObject().field("title", "Poke me!").endObject();

        // Index event #2 (with TTL=120s)
        IndexResponse indexResponse2 = client.prepareIndex("events", "event")
                .setSource(builder)
                .setRefresh(true)
                .setTTL(120 * 1000)
                .execute()
                .actionGet();

        // Event #3
        builder = JsonXContent.contentBuilder();
        builder.startObject().field("title", "Poke both of you!").endObject();

        // Index event #3 (with TTL=3600s)
        IndexResponse indexResponse3 = client.prepareIndex("events", "event")
                .setSource(builder)
                .setRefresh(true)
                .setTTL(60 * 60 * 1000)
                .execute()
                .actionGet();

        // Get event #1
        GetResponse getResponse = client.prepareGet("events", "event", indexResponse1.getId()).execute().actionGet();
        assertTrue("Event 1 is not deleted yet", getResponse.isExists());

        // Get event #2
        getResponse = client.prepareGet("events", "event", indexResponse2.getId()).execute().actionGet();
        assertTrue("Event 2 is not deleted yet", getResponse.isExists());

        // Get event #3
        getResponse = client.prepareGet("events", "event", indexResponse3.getId()).execute().actionGet();
        assertTrue("Event 3 is not deleted yet", getResponse.isExists());

        // Wait 90s
        try {
            Thread.sleep(90 * 1000);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // Get event #1 again
        getResponse = client.prepareGet("events", "event", indexResponse1.getId()).execute().actionGet();
        assertFalse("Event 1 must be deleted now", getResponse.isExists());

        // Get event #2 again
        getResponse = client.prepareGet("events", "event", indexResponse2.getId()).execute().actionGet();
        assertTrue("Event 2 is not deleted yet", getResponse.isExists());

        // Get event #3
        getResponse = client.prepareGet("events", "event", indexResponse3.getId()).execute().actionGet();
        assertTrue("Event 3 is not deleted yet", getResponse.isExists());

        // Wait 90s again
        try {
            Thread.sleep(90 * 1000);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // Get event #1 again
        getResponse = client.prepareGet("events", "event", indexResponse1.getId()).execute().actionGet();
        assertFalse("Event 1 must be deleted now", getResponse.isExists());

        // Get event #1 again
        getResponse = client.prepareGet("events", "event", indexResponse2.getId()).execute().actionGet();
        assertFalse("Event 2 must be deleted now", getResponse.isExists());

        // Get event #3
        getResponse = client.prepareGet("events", "event", indexResponse3.getId()).execute().actionGet();
        assertTrue("Event 3 must be the survivor", getResponse.isExists());
    }

}
