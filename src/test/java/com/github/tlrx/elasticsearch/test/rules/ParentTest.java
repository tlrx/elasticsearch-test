package com.github.tlrx.elasticsearch.test.rules;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchTransportClient;
import com.github.tlrx.elasticsearch.test.support.junit.rules.ElasticsearchFieldRule;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.node.Node;
import org.junit.runner.RunWith;

/**
 * Simple parent class to check that {@link ElasticsearchFieldRule} manage
 * declared and inherited class attributes.
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
public abstract class ParentTest {

    @ElasticsearchNode
    Node node;

    @ElasticsearchTransportClient
    TransportClient transportClient;

}
