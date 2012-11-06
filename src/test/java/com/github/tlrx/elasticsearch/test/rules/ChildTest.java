package com.github.tlrx.elasticsearch.test.rules;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.support.junit.rules.ElasticsearchFieldRule;
import org.elasticsearch.client.Client;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Simple child class to check that {@link ElasticsearchFieldRule} manage
 * declared and inherited class attributes.
 *
 * @author tlrx
 */
public class ChildTest extends ParentTest {

    @ElasticsearchClient
    Client client;

    @Test
    public void test() {
        assertNotNull(client);
        assertNotNull(node);
        assertNotNull(transportClient);
    }
}
