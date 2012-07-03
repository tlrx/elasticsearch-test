package fr.tlrx.elasticsearch.test.annotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test class for {@link ElasticsearchTransportClient} annotation.
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchTransportClientAnnotationTest {

	@ElasticsearchNode(name = "node0", local = false, clusterName = "external")
	Node node0;

	@ElasticsearchTransportClient(local = false, clusterName = "external",
									hostnames = {"127.0.0.1"},
									ports= {9300})
	Client client0;
	
	@Test
	@ElasticsearchIndex(indexName = "sites", nodeName = "node0")
	public void testTransportClient() {
		
		assertNotNull(node0);
		assertNotNull(client0);
		
		// Checks if the index has been created
		IndicesExistsResponse existResponse = client0.admin().indices()
				.prepareExists("sites")
				.execute().actionGet();
		assertTrue("Index must exist", existResponse.exists());
	}
	
	@ElasticsearchNode
	Node node1;

	@ElasticsearchTransportClient
	Client client1;
	
	@Test
	@ElasticsearchIndex(indexName = "sites")
	public void testLocalTransportClient() {
		
		assertNotNull(node1);
		assertNotNull(client1);
		assertNotSame(node0, node1);
		assertNotSame(client0, client1);
		
		// Checks if the index has been created
		IndicesExistsResponse existResponse = client1.admin().indices()
				.prepareExists("sites")
				.execute().actionGet();
		assertTrue("Index must exist", existResponse.exists());
	}
}
