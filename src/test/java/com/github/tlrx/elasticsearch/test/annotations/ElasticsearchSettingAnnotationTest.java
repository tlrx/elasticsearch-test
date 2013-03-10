package com.github.tlrx.elasticsearch.test.annotations;

import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link ElasticsearchSetting} annotation.
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchSettingAnnotationTest {

    @ElasticsearchNode(name = "node0", settings = {
            @ElasticsearchSetting(name = "http.enabled", value = "false"),
            @ElasticsearchSetting(name = "node.zone", value = "zone_one")})
    Node node;

    @ElasticsearchAdminClient(nodeName = "node0")
    AdminClient adminClient;

    @Test
    @ElasticsearchIndexes(indexes = {
            @ElasticsearchIndex(indexName = "library",
                    forceCreate = true,
                    settings = {
                            @ElasticsearchSetting(name = "number_of_shards", value = "2"),
                            @ElasticsearchSetting(name = "number_of_replicas", value = "1")}),
            @ElasticsearchIndex(indexName = "people")})
    public void testElasticsearchSettings() {

        // Check custom settings on node
        NodesInfoResponse infoResponse = adminClient.cluster()
                .prepareNodesInfo("node0")
                .setSettings(true)
                .execute()
                .actionGet();

        Settings nodeSettings = infoResponse.getAt(0).getSettings();
        assertEquals("false", nodeSettings.get("http.enabled"));
        assertEquals("zone_one", nodeSettings.get("node.zone"));

        // Check custom settings on index
        ClusterStateResponse response = adminClient.cluster().prepareState()
                .execute().actionGet();

        Settings indexSettings = response.getState().metaData().index("library").settings();
        assertEquals("2", indexSettings.get("index.number_of_shards"));
        assertEquals("1", indexSettings.get("index.number_of_replicas"));

        // Check default settings
        indexSettings = response.getState().metaData().index("people").settings();
        assertEquals("1", indexSettings.get("index.number_of_shards"));
        assertEquals("0", indexSettings.get("index.number_of_replicas"));
    }
}
