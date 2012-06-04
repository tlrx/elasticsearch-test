package fr.tlrx.elasticsearch.test.annotations;

import static org.junit.Assert.assertEquals;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test class for {@link ElasticsearchAnalysis} annotation.
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchAnalysisAnnotationTest {

	@ElasticsearchNode(name = "node0", local=false)
	Node node;

	@ElasticsearchAdminClient(nodeName = "node0")
	AdminClient adminClient;
	
	@Test
	@ElasticsearchIndexes(indexes = {
			@ElasticsearchIndex(indexName = "library", settings = {
					@ElasticsearchSetting(name = "number_of_shards", value = "7"),
					@ElasticsearchSetting(name = "number_of_replicas", value = "8") },
					analysis = @ElasticsearchAnalysis(
							filters = {
									@ElasticsearchFilter(name = "myPhonetic", typeName = "phonetic", 
											settings = {
												@ElasticsearchSetting(name = "encoder", value = "double_metaphone"),
												@ElasticsearchSetting(name = "replace", value = "false"),
											}),
									@ElasticsearchFilter(name = "myEdgeNGram", typeName = "edgeNGram",
											settings = {
												@ElasticsearchSetting(name = "min_gram", value = "2"),
												@ElasticsearchSetting(name = "max_gram", value = "10"),
												@ElasticsearchSetting(name = "side", value = "front"),
											})
							},
							analyzers = {
									@ElasticsearchAnalyzer(name = "untouched", tokenizer = "keyword", filtersNames = {"lowercase", "asciifolding"}),
									@ElasticsearchAnalyzer(name = "basic", tokenizer = "standard", filtersNames = {"lowercase", "asciifolding", "myPhonetic"})
							})
					),
			@ElasticsearchIndex(indexName = "people") })
	public void testElasticsearchSettings() {

		// Check custom settings on index
		ClusterStateResponse response = adminClient.cluster().prepareState()
				.execute().actionGet();
		
		Settings indexSettings = response.state().metaData().index("library").settings();
		assertEquals("7", indexSettings.get("index.number_of_shards"));
		assertEquals("8", indexSettings.get("index.number_of_replicas"));

		// Check filters
		assertEquals("phonetic", indexSettings.get("index.settings.index.analysis.filter.myPhonetic.type"));
		assertEquals("double_metaphone", indexSettings.get("index.settings.index.analysis.filter.myPhonetic.encoder"));
		assertEquals("false", indexSettings.get("index.settings.index.analysis.filter.myPhonetic.replace"));
		
		assertEquals("edgeNGram", indexSettings.get("index.settings.index.analysis.filter.myEdgeNGram.type"));
		assertEquals("2", indexSettings.get("index.settings.index.analysis.filter.myEdgeNGram.min_gram"));
		assertEquals("10", indexSettings.get("index.settings.index.analysis.filter.myEdgeNGram.max_gram"));
		assertEquals("front", indexSettings.get("index.settings.index.analysis.filter.myEdgeNGram.side"));
		
		// Check analyzers
		assertEquals("keyword", indexSettings.get("index.settings.index.analysis.analyzer.untouched.tokenizer"));
		assertEquals("lowercase", indexSettings.get("index.settings.index.analysis.analyzer.untouched.filter.0"));
		assertEquals("asciifolding", indexSettings.get("index.settings.index.analysis.analyzer.untouched.filter.1"));
		
		assertEquals("standard", indexSettings.get("index.settings.index.analysis.analyzer.basic.tokenizer"));
		assertEquals("lowercase", indexSettings.get("index.settings.index.analysis.analyzer.basic.filter.0"));
		assertEquals("asciifolding", indexSettings.get("index.settings.index.analysis.analyzer.basic.filter.1"));
		assertEquals("myPhonetic", indexSettings.get("index.settings.index.analysis.analyzer.basic.filter.2"));
	}

}
