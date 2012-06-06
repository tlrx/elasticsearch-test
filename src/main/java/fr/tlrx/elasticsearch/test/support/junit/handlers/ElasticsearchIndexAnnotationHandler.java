/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.node.Node;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAnalysis;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAnalyzer;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchFilter;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndexes;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMapping;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMappingMultiField;
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

			if (existResponse.exists()) {
				// Drop the index
				DeleteIndexResponse deleteResponse = admin.indices()
						.prepareDelete(elasticsearchIndex.indexName())
						.execute().actionGet();
			}

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
					String prefix = "analysis.filter." + filter.name(); 
					settings.put(prefix + ".type", filter.typeName());
					for (ElasticsearchSetting setting : filter.settings()) {
						settings.put(prefix + "." + setting.name(), setting.value());
					}
				}
				for(ElasticsearchAnalyzer analyzer : analysis.analyzers()){
					String prefix = "analysis.analyzer." + analyzer.name(); 
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

	/**
	 * Builds a mapping for field of a mapping
	 * 
	 * @param field
	 * @param builder
	 * @return
	 * @throws IOException 
	 */
	private XContentBuilder buildField(ElasticsearchMappingField field, XContentBuilder builder) throws IOException{
		builder = builder.startObject(field.name())
				.field("type", field.type().toString().toLowerCase())
				.field("store", field.store().toString().toLowerCase());

        if (!field.index().equals(ElasticsearchMappingField.Index.Undefined)) {
            builder.field("index", field.index().toString().toLowerCase());
        }

        if ((field.analyzerName() != null) 
                && (!ElasticsearchMappingField.DEFAULT_ANALYZER.equals(field.analyzerName()))) {
            builder.field("analyzer", field.analyzerName().toString().toLowerCase());
        }
        
        if ((field.indexAnalyzerName() != null) 
                && (!ElasticsearchMappingField.DEFAULT_ANALYZER.equals(field.indexAnalyzerName()))) {
            builder.field("index_analyzer", field.indexAnalyzerName().toString().toLowerCase());
        }
        
        if ((field.searchAnalyzerName() != null) 
                && (!ElasticsearchMappingField.DEFAULT_ANALYZER.equals(field.searchAnalyzerName()))) {
            builder.field("search_analyzer", field.searchAnalyzerName().toString().toLowerCase());
        }                    
	
        if ((field.termVector() != null) 
                && (!ElasticsearchMappingField.TermVector.No.equals(field.termVector()))) {
            builder.field("term_vector", field.termVector().toString().toLowerCase());
        } 
        
        builder = builder.endObject();
		return builder;
	}
	
	
	/**
	 * Builds a mapping for a document type
	 * 
	 * @param mapping
	 * @throws IOException
	 */
	private XContentBuilder buildMapping(ElasticsearchMapping mapping) {
		XContentBuilder builder = null;

		try {
			builder = XContentFactory.contentBuilder(XContentType.JSON)
					.startObject()
					.startObject(mapping.typeName())
					.startObject("_source")
						.field("enabled", String.valueOf(mapping.source()))
						.field("compress", String.valueOf(mapping.compress()))
					.endObject()
					.startObject("properties");

			// Manage fields
			ElasticsearchMappingField[] properties = mapping.properties();
			
			if ((properties != null) && (properties.length > 0)) {
				for (ElasticsearchMappingField field : properties) {
					builder = buildField(field, builder);
				}
			}
			
			// Manage multi_fields
			ElasticsearchMappingMultiField[] propertiesMulti = mapping.propertiesMulti();
			
			if ((propertiesMulti != null) && (propertiesMulti.length > 0)) {
				for (ElasticsearchMappingMultiField multiField : propertiesMulti) {
				    builder = builder.startObject(multiField.name());

				    ElasticsearchMappingField[] fields = multiField.fields();
				    if ((fields != null) && (fields.length > 0)) {
				    	builder = builder.startObject("fields");
				    	
				    	for (ElasticsearchMappingField field :fields) {
				    		builder = buildField(field, builder);
					    }
				    	
				    	builder = builder.endObject();
				    }
				    
				    builder = builder.endObject();
				}
			}

			builder.endObject().endObject().endObject();
			
			// Prints generated mapping
			System.out.printf("Mapping [%s]:\r\n%s\r\n", mapping.typeName(), builder.string());
			
		} catch (Exception e) {
			System.err.println("Exception when building mapping for type "
					+ mapping.typeName());
			e.printStackTrace(System.err);
		}
		
		return builder;
	}
}