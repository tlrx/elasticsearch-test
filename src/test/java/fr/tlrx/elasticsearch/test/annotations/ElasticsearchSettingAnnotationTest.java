package fr.tlrx.elasticsearch.test.annotations;

import static org.junit.Assert.assertEquals;

import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndexes;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchSetting;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test class for {@link ElasticsearchSetting} annotation.
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchSettingAnnotationTest {

	@ElasticsearchNode(name = "node0", settings = {
			@ElasticsearchSetting(name = "http.enabled", value = "false"),
			@ElasticsearchSetting(name = "node.zone", value = "zone_one") })
	Node node;

	@ElasticsearchAdminClient(nodeName = "node0")
	AdminClient adminClient;
	
	@Test
	@ElasticsearchIndexes(indexes = {
			@ElasticsearchIndex(indexName = "library", settings = {
					@ElasticsearchSetting(name = "number_of_shards", value = "2"),
					@ElasticsearchSetting(name = "number_of_replicas", value = "1") }),
			@ElasticsearchIndex(indexName = "people") })
	public void testElasticsearchSettings() {

		// Check custom settings on node
		NodesInfoResponse infoResponse = adminClient.cluster()
				.prepareNodesInfo("node0")
				.setSettings(true)
				.execute()
				.actionGet();

		Settings nodeSettings = infoResponse.getAt(0).settings();
		assertEquals("false", nodeSettings.get("http.enabled"));
		assertEquals("zone_one", nodeSettings.get("node.zone"));
		
		// Check custom settings on index
		ClusterStateResponse response = adminClient.cluster().prepareState()
				.execute().actionGet();
		
		Settings indexSettings = response.state().metaData().index("library")
				.settings();
		assertEquals("2", indexSettings.get("index.number_of_shards"));
		assertEquals("1", indexSettings.get("index.number_of_replicas"));

		// Check default settings
		indexSettings = response.state().metaData().index("people").settings();
		assertEquals("1", indexSettings.get("index.number_of_shards"));
		assertEquals("0", indexSettings.get("index.number_of_replicas"));
	}

}
