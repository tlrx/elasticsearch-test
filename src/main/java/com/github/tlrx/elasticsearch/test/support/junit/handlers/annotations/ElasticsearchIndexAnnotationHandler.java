/**
 *
 */
package com.github.tlrx.elasticsearch.test.support.junit.handlers.annotations;

import com.github.tlrx.elasticsearch.test.annotations.*;
import com.github.tlrx.elasticsearch.test.support.junit.handlers.MethodLevelElasticsearchAnnotationHandler;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handle {@link ElasticsearchIndex} annotation
 *
 * @author tlrx
 */
public class ElasticsearchIndexAnnotationHandler extends AbstractAnnotationHandler implements MethodLevelElasticsearchAnnotationHandler {

    private final static Logger LOGGER = Logger.getLogger(ElasticsearchIndexAnnotationHandler.class.getName());

    public boolean support(Annotation annotation) {
        return (annotation instanceof ElasticsearchIndex);
    }

    public void handleBefore(Annotation annotation, Object instance, Map<String, Object> context) throws Exception {
        buildIndex((ElasticsearchIndex) annotation, context);
    }

    public void handleAfter(Annotation annotation, Object instance, Map<String, Object> context) throws Exception {
        ElasticsearchIndex elasticsearchIndex = (ElasticsearchIndex) annotation;

        // Cleans (delete all documents) in the index
        if (elasticsearchIndex.cleanAfter()) {
            clean(context, elasticsearchIndex.nodeName(), elasticsearchIndex.indexName());
        }
    }

    /**
     * Delete all documents in the index
     *
     * @param nodeName
     * @param indexName
     * @throws Exception
     * @throws ElasticSearchException
     */
    private void clean(Map<String, Object> context, String nodeName, String indexName) throws ElasticSearchException, Exception {
        client(context, nodeName).prepareDeleteByQuery(indexName)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute().actionGet();
    }

    /**
     * Delete an index
     *
     * @param context
     * @param nodeName
     * @param indexName
     * @throws ElasticSearchException
     * @throws Exception
     */
    private void deleteIndex(Map<String, Object> context, String nodeName, String indexName) throws ElasticSearchException, Exception {
        DeleteIndexResponse response = admin(context, nodeName).indices().prepareDelete(indexName).execute().actionGet();
        if (!response.isAcknowledged()) {
            throw new Exception("Could not delete index [" + indexName + "]");
        }
    }

    /**
     * Create an index
     *
     * @param context
     * @param nodeName
     * @param indexName
     * @param settings
     * @throws ElasticSearchException
     * @throws Exception
     */
    private void createIndex(Map<String, Object> context, String nodeName, String indexName, Settings settings) throws ElasticSearchException, Exception {
        CreateIndexRequestBuilder builder = admin(context, nodeName).indices().prepareCreate(indexName);
        if (settings != null) {
            builder.setSettings(settings);
        }
        CreateIndexResponse response = builder.execute().actionGet();
        if (!response.isAcknowledged()) {
            throw new Exception("Could not create index [" + indexName + "]");
        }
    }


    /**
     * Put index mapping
     *
     * @param context
     * @param nodeName
     * @param indexName
     * @param type
     * @param mappingBuilder
     * @throws ElasticSearchException
     * @throws Exception
     */
    private void putIndexMapping(Map<String, Object> context, String nodeName, String indexName, String type, XContentBuilder mappingBuilder) throws ElasticSearchException, Exception {
        PutMappingResponse response = admin(context, nodeName).indices()
                .preparePutMapping(indexName)
                .setType(type)
                .setSource(mappingBuilder)
                .execute().actionGet();
        if (!response.isAcknowledged()) {
            throw new Exception("Could not put mapping [" + type + "] for index [" + indexName + "]");
        }
    }

    /**
     * Creates or Updates an index
     *
     * @param elasticsearchIndex
     * @throws Exception
     */
    protected void buildIndex(ElasticsearchIndex elasticsearchIndex, Map<String, Object> context) throws Exception {
        // Get an AdminClient for the node
        AdminClient admin = admin(context, elasticsearchIndex.nodeName());

        // Check if index already exists
        IndicesExistsResponse existResponse = admin.indices()
                .prepareExists(elasticsearchIndex.indexName()).execute()
                .actionGet();

        if (existResponse.isExists()) {
            // Index already exists, drop it if forceCreate = true
            if (elasticsearchIndex.forceCreate()) {

                // Delete the index
                deleteIndex(context, elasticsearchIndex.nodeName(), elasticsearchIndex.indexName());

                // Create the index
                createIndex(context, elasticsearchIndex.nodeName(), elasticsearchIndex.indexName(), buildIndexSettings(elasticsearchIndex));
            }
        } else {
            // Create the index
            createIndex(context, elasticsearchIndex.nodeName(), elasticsearchIndex.indexName(), buildIndexSettings(elasticsearchIndex));
        }

        // Build & update index mappings
        ElasticsearchMapping[] mappings = elasticsearchIndex.mappings();
        for (ElasticsearchMapping mapping : mappings) {
            putIndexMapping(context, elasticsearchIndex.nodeName(), elasticsearchIndex.indexName(), mapping.typeName(), buildMapping(mapping));
        }
    }

    /**
     * Build index settings
     */
    private Settings buildIndexSettings(ElasticsearchIndex elasticsearchIndex) {

        // Build default settings
        Builder settingsBuilder = ImmutableSettings.settingsBuilder();

        String settingsFile = "config/mappings/" + elasticsearchIndex.indexName() + "/_settings.json";
        if (elasticsearchIndex.settingsFile().length() > 0) {
            settingsFile = elasticsearchIndex.settingsFile();
        }

        // Loads settings from settings file
        Settings configSettings = ImmutableSettings.settingsBuilder().loadFromClasspath(settingsFile).build();
        settingsBuilder.put(configSettings);

        // Manage analysis filters & tokenizers
        ElasticsearchAnalysis analysis = elasticsearchIndex.analysis();
        if (analysis != null && (analysis.filters().length > 0 || analysis.analyzers().length > 0)) {
            for (ElasticsearchFilter filter : analysis.filters()) {
                String prefix = "index.analysis.filter." + filter.name();
                settingsBuilder.put(prefix + ".type", filter.typeName());
                for (ElasticsearchSetting setting : filter.settings()) {
                    settingsBuilder.put(prefix + "." + setting.name(), setting.value());
                }
            }
            for (ElasticsearchAnalyzer analyzer : analysis.analyzers()) {
                String prefix = "index.analysis.analyzer." + analyzer.name();
                settingsBuilder.put(prefix + ".tokenizer", analyzer.tokenizer());
                if (analyzer.filtersNames() != null && analyzer.filtersNames().length > 0) {
                    settingsBuilder.putArray(prefix + ".filter", analyzer.filtersNames());
                }
            }
        }

        // Other settings
        ElasticsearchSetting[] indexSettings = elasticsearchIndex.settings();
        for (ElasticsearchSetting setting : indexSettings) {
            settingsBuilder.put(setting.name(), setting.value());
        }

        // Build the settings
        return settingsBuilder.build();
    }

    /**
     * Builds a mapping for field of a mapping
     *
     * @param field
     * @param builder
     * @return
     * @throws IOException
     */
    private XContentBuilder buildField(ElasticsearchMappingField field, XContentBuilder builder) throws IOException {
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
                    .endObject();

            if (mapping.ttl()) {
                builder = builder.startObject("_ttl").field("enabled",
                        String.valueOf(mapping.ttl()));
                if (mapping.ttlValue().length() > 0) {
                    builder = builder.field("default", mapping.ttlValue());
                }
                builder = builder.endObject();
            }

            builder = builder.startObject("properties");

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

                        for (ElasticsearchMappingField field : fields) {
                            builder = buildField(field, builder);
                        }
                        builder = builder.endObject();
                    }
                    builder = builder.endObject();
                }
            }

            builder.endObject().endObject().endObject();

            // Prints generated mapping
            LOGGER.info(String.format("Mapping [%s]:\r\n%s\r\n", mapping.typeName(), builder.string()));

        } catch (Exception e) {
            LOGGER.severe("Exception when building mapping for type " + mapping.typeName() + ": " + e.getMessage());
        }

        return builder;
    }
}