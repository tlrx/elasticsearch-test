package fr.tlrx.elasticsearch.test.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test class for {@link ElasticsearchNode} annotation.
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchNodeAnnotationTest {

	private static final String CLUSTER_NAME = "cluster.name";
	private static final String NODE_NAME = "node.name";
	private static final String NODE_LOCAL = "node.local";
	private static final String NODE_DATA = "node.data";
	
	@ElasticsearchNode
	Node node0;

	@ElasticsearchNode
	Node node0bis;
	
	@ElasticsearchNode(name = "node1", clusterName = "second-cluster-name")
	Node node1;

	@ElasticsearchNode(name = "node2", clusterName = "third-cluster-name", local = false)
	Node node2;
	
	@ElasticsearchNode(name = "node3", clusterName = "fourth-cluster-name", local = true, data = false)
	Node node3;
	
	@ElasticsearchNode(name = "node3")
	Node node3bis;
	
	@ElasticsearchNode(name = "node3-1", clusterName = "fourth-cluster-name")
	Node node3_1;

	@Test
	public void testElasticsearchNodes(){
		assertNotNull(node0);
		assertEquals("elasticsearch-test-node", node0.settings().get(NODE_NAME));
		assertEquals("elasticsearch-test-cluster", node0.settings().get(CLUSTER_NAME));
		assertEquals(ElasticsearchNode.DEFAULT_NODE_NAME, node0.settings().get(NODE_NAME));
		assertEquals(ElasticsearchNode.DEFAULT_CLUSTER_NAME, node0.settings().get(CLUSTER_NAME));
		assertTrue(node0.settings().getAsBoolean(NODE_LOCAL, null));
		assertTrue(node0.settings().getAsBoolean(NODE_DATA, null));

		assertEquals(node0, node0bis);
		assertEquals("elasticsearch-test-node", node0bis.settings().get(NODE_NAME));
		assertEquals(node0.settings().get(CLUSTER_NAME), node0bis.settings().get(CLUSTER_NAME));
		
		assertNotNull(node1);
		assertEquals("node1", node1.settings().get(NODE_NAME));
		assertEquals("second-cluster-name", node1.settings().get(CLUSTER_NAME));
		assertTrue(node1.settings().getAsBoolean(NODE_LOCAL, null));
		assertTrue(node1.settings().getAsBoolean(NODE_DATA, null));

		assertNotNull(node2);
		assertEquals("node2", node2.settings().get(NODE_NAME));
		assertEquals("third-cluster-name", node2.settings().get(CLUSTER_NAME));
		assertFalse(node2.settings().getAsBoolean(NODE_LOCAL, null));
		assertTrue(node2.settings().getAsBoolean(NODE_DATA, null));

		assertNotNull(node3);
		assertEquals("node3", node3.settings().get(NODE_NAME));
		assertEquals("fourth-cluster-name", node3.settings().get(CLUSTER_NAME));
		assertTrue(node3.settings().getAsBoolean(NODE_LOCAL, null));
		assertFalse(node3.settings().getAsBoolean(NODE_DATA, null));
		
		assertEquals(node3, node3bis);
		assertEquals("node3", node3bis.settings().get(NODE_NAME));
		assertEquals(node3.settings().get(CLUSTER_NAME), node3bis.settings().get(CLUSTER_NAME));
		
		assertNotSame(node3, node3_1);
		assertEquals(node3.settings().get(CLUSTER_NAME), node3_1.settings().get(CLUSTER_NAME));
	}
}
