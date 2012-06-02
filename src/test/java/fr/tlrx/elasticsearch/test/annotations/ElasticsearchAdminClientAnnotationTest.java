package fr.tlrx.elasticsearch.test.annotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.elasticsearch.client.AdminClient;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test class for {@link ElasticsearchAdminClient} annotation.
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchAdminClientAnnotationTest {
	
	@ElasticsearchAdminClient
	AdminClient adminClient0;
	
	@ElasticsearchNode(name = "node1")
	Node node1;
	
	@ElasticsearchAdminClient(nodeName = "node1")
	AdminClient adminClient1;
	
	@Test
	public void testElasticsearchAdminClients(){
		assertNotNull(adminClient0);
		
		assertNotNull(node1);
		assertNotNull(adminClient1);
		
		assertNotSame(adminClient0, adminClient1);
	}
}
