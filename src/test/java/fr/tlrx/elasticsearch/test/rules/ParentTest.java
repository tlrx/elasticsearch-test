package fr.tlrx.elasticsearch.test.rules;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.node.Node;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchTransportClient;
import fr.tlrx.elasticsearch.test.support.junit.rules.ElasticsearchFieldRule;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Simple parent class to check that {@link ElasticsearchFieldRule} manage
 * declared and inherited class attributes.
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public abstract class ParentTest {

	@ElasticsearchNode
	Node node;

	@ElasticsearchTransportClient
	TransportClient transportClient;

}
