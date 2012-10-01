package fr.tlrx.elasticsearch.test.rules;

import static org.junit.Assert.assertNotNull;

import org.elasticsearch.client.Client;
import org.junit.Test;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import fr.tlrx.elasticsearch.test.support.junit.rules.ElasticsearchFieldRule;

/**
 * Simple child class to check that {@link ElasticsearchFieldRule} manage
 * declared and inherited class attributes.
 * 
 * @author tlrx
 * 
 */
public class ChildTest extends ParentTest {

	@ElasticsearchClient
	Client client;
	
	@Test
	public void test(){
		assertNotNull(client);
		assertNotNull(node);
		assertNotNull(transportClient);
	}
}
