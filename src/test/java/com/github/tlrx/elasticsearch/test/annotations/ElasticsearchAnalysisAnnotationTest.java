package com.github.tlrx.elasticsearch.test.annotations;

import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link ElasticsearchAnalysis} annotation.
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchAnalysisAnnotationTest {

    @ElasticsearchNode(name = "node0", local = false)
    Node node;

    @ElasticsearchAdminClient(nodeName = "node0")
    AdminClient adminClient;

    @Test
    @ElasticsearchIndexes(indexes = {
            @ElasticsearchIndex(indexName = "library", settings = {
                    @ElasticsearchSetting(name = "number_of_shards", value = "7"),
                    @ElasticsearchSetting(name = "number_of_replicas", value = "8")},
                    analysis = @ElasticsearchAnalysis(
                            filters = {
                                    @ElasticsearchFilter(name = "myLength", typeName = "length",
                                            settings = {
                                                    @ElasticsearchSetting(name = "min", value = "0"),
                                                    @ElasticsearchSetting(name = "max", value = "5")
                                            }),
                                    @ElasticsearchFilter(name = "myEdgeNGram", typeName = "edgeNGram",
                                            settings = {
                                                    @ElasticsearchSetting(name = "min_gram", value = "2"),
                                                    @ElasticsearchSetting(name = "max_gram", value = "10"),
                                                    @ElasticsearchSetting(name = "side", value = "front")
                                            })
                            },
                            analyzers = {
                                    @ElasticsearchAnalyzer(name = "untouched", tokenizer = "keyword", filtersNames = {"lowercase", "asciifolding"}),
                                    @ElasticsearchAnalyzer(name = "basic", tokenizer = "standard", filtersNames = {"lowercase", "myEdgeNGram", "myLength"})
                            })
            ),
            @ElasticsearchIndex(indexName = "people")})
    public void testElasticsearchSettings() {

        // Check custom settings on index
        ClusterStateResponse response = adminClient.cluster().prepareState()
                .execute().actionGet();

        Settings indexSettings = response.getState().metaData().index("library").settings();
        assertEquals("7", indexSettings.get("index.number_of_shards"));
        assertEquals("8", indexSettings.get("index.number_of_replicas"));

        // Check filters
        assertEquals("length", indexSettings.get("index.analysis.filter.myLength.type"));
        assertEquals("0", indexSettings.get("index.analysis.filter.myLength.min"));
        assertEquals("5", indexSettings.get("index.analysis.filter.myLength.max"));

        assertEquals("edgeNGram", indexSettings.get("index.analysis.filter.myEdgeNGram.type"));
        assertEquals("2", indexSettings.get("index.analysis.filter.myEdgeNGram.min_gram"));
        assertEquals("10", indexSettings.get("index.analysis.filter.myEdgeNGram.max_gram"));
        assertEquals("front", indexSettings.get("index.analysis.filter.myEdgeNGram.side"));

        // Check analyzers
        assertEquals("keyword", indexSettings.get("index.analysis.analyzer.untouched.tokenizer"));
        assertEquals("lowercase", indexSettings.get("index.analysis.analyzer.untouched.filter.0"));
        assertEquals("asciifolding", indexSettings.get("index.analysis.analyzer.untouched.filter.1"));

        assertEquals("standard", indexSettings.get("index.analysis.analyzer.basic.tokenizer"));
        assertEquals("lowercase", indexSettings.get("index.analysis.analyzer.basic.filter.0"));
        assertEquals("myEdgeNGram", indexSettings.get("index.analysis.analyzer.basic.filter.1"));
        assertEquals("myLength", indexSettings.get("index.analysis.analyzer.basic.filter.2"));
    }

}
