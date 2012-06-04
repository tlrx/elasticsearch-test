/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.node.Node;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAnalysis;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAnalyzer;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchFilter;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndexes;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMapping;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchSetting;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Handle {@link ElasticsearchIndex} annotation
 * 
 * @author tlrx
 * 
 */
public class ElasticsearchIndexAnnotationHandler extends AbstractElasticsearchAnnotationHandler {

	public boolean support(Annotation annotation) {
		return (annotation instanceof ElasticsearchIndex) || (annotation instanceof ElasticsearchIndexes);
	}
	
	public void handleBefore(ElasticsearchRunner runner, Object instance, Annotation annotation) {
		
		List<ElasticsearchIndex> indexes = new ArrayList<ElasticsearchIndex>();
		
		// Manage @ElasticsearchIndex
		if (annotation instanceof ElasticsearchIndexes) {
			for(ElasticsearchIndex index : ((ElasticsearchIndexes)annotation).indexes()){
				indexes.add(index);	
			}
		// Manage @ElasticsearchIndex
		} else if (annotation instanceof ElasticsearchIndex) {
			indexes.add((ElasticsearchIndex) annotation);
		}		
		
		for (ElasticsearchIndex elasticsearchIndex : indexes) {
			// Get a node
			Node node = runner.node(elasticsearchIndex.nodeName());
			AdminClient admin = node.client().admin();

			// Check if index already exists
			IndicesExistsResponse existResponse = admin.indices()
					.prepareExists(elasticsearchIndex.indexName())
					.execute().actionGet();

			if (!existResponse.exists()) {
				Builder settings = ImmutableSettings.settingsBuilder()						
						.put("number_of_shards", "1")
						.put("number_of_replicas", "0");

				// Manage settings for this index
				ElasticsearchSetting[] indexSettings = elasticsearchIndex.settings();
				if (indexSettings != null && indexSettings.length > 0) {
					for (ElasticsearchSetting setting : indexSettings) {
						settings.put(setting.name(), setting.value());
					}
				}
				
				// Manage analysis filters & tokenizers
				ElasticsearchAnalysis analysis = elasticsearchIndex.analysis();
				if (analysis != null) {
					for(ElasticsearchFilter filter : analysis.filters()){
						String prefix = "settings.index.analysis.filter." + filter.name(); 
						settings.put(prefix + ".type", filter.typeName());
						for (ElasticsearchSetting setting : filter.settings()) {
							settings.put(prefix + "." + setting.name(), setting.value());
						}
					}
					for(ElasticsearchAnalyzer analyzer : analysis.analyzers()){
						String prefix = "settings.index.analysis.analyzer." + analyzer.name(); 
						settings.put(prefix + ".tokenizer", analyzer.tokenizer());
						if(analyzer.filtersNames() != null && analyzer.filtersNames().length > 0){
							settings.putArray(prefix +  ".filter", analyzer.filtersNames());
						}
					}
				}			
				
				CreateIndexRequestBuilder builder = admin.indices()
						.prepareCreate(elasticsearchIndex.indexName())
						.setSettings(settings.build());

				ElasticsearchMapping[] mappings = elasticsearchIndex.mappings();
				
				// Mappings are defined for this index
				if (mappings != null && mappings.length > 0) {
					for (ElasticsearchMapping mapping : mappings) {
						builder.addMapping(mapping.typeName(), buildMapping(mapping));
					}
				}

				// Create the index
				builder.execute().actionGet();
			}
		}
	}

	/**
	 * Builds a mapping
	 * 
	 * @param mapping
	 * @throws IOException
	 */
	private XContentBuilder buildMapping(ElasticsearchMapping mapping) {
		XContentBuilder builder = null;

		try {
			builder = JsonXContent.contentBuilder()
					.startObject()
					.startObject(mapping.typeName())
					.startObject("properties");

			ElasticsearchMappingField[] properties = mapping.properties();
			
			if ((properties != null) && (properties.length > 0)) {
				for (ElasticsearchMappingField field : properties) {
					builder.startObject(field.name())
								.field("type", field.type().toString().toLowerCase())
								.field("store", field.store().toString().toLowerCase())
							.endObject();
				}
			}

			builder.endObject().endObject().endObject();
		} catch (Exception e) {
			System.err.println("Exception when building mapping for type "
					+ mapping.typeName());
			e.printStackTrace(System.err);
		}
		return builder;
	}
}
