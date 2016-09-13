/**
 *
 */
package com.github.tlrx.elasticsearch.test.support.junit.handlers.annotations;

import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchSetting;
import com.github.tlrx.elasticsearch.test.support.junit.handlers.ClassLevelElasticsearchAnnotationHandler;
import com.github.tlrx.elasticsearch.test.support.junit.handlers.FieldLevelElasticsearchAnnotationHandler;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

import static com.github.tlrx.elasticsearch.test.provider.LocalClientProvider.deleteRecursively;

/**
 * Handle {@link ElasticsearchNode} annotation
 *
 * @author tlrx
 */
public class ElasticsearchNodeAnnotationHandler implements ClassLevelElasticsearchAnnotationHandler, FieldLevelElasticsearchAnnotationHandler {

    /**
     * Elasticsearch home directory
     */
    private static final String ES_HOME = "./target/elasticsearch-test";
    private static final String NODE_NAME = "node.name";

    public boolean support(Annotation annotation) {
        return (annotation instanceof ElasticsearchNode);
    }

    public void beforeClass(Object testClass, Map<String, Object> context) throws Exception {
        // Nothing to do here
    }

    public void handleBeforeClass(Annotation annotation, Object testClass, Map<String, Object> context) {
        // Instantiate a node
        buildNode((ElasticsearchNode) annotation, context);
    }

    public void handleAfterClass(Annotation annotation, Object testClass, Map<String, Object> context) {
        // Nothing to do here
    }

    public void afterClass(Object testClass, Map<String, Object> context) throws Exception {
        for (Object obj : context.values()) {
            if (obj instanceof Node) {
                Node node = (Node) obj;

                if (!node.isClosed()) {
                    node.close();
                }
            }
        }
        deleteRecursively(new File(ES_HOME));
    }

    public void handleField(Annotation annotation, Object instance, Map<String, Object> context, Field field) throws Exception {
        // Get the node
        Node node = buildNode((ElasticsearchNode) annotation, context);

        // Sets the node as the field's value
        try {
            field.setAccessible(true);
            field.set(instance, node);
        } catch (Exception e) {
            throw new Exception("Exception when setting the node:" + e.getMessage(), e);
        }
    }

    /**
     * Builds & start a new node, or retrieves an existing one from context
     *
     * @param elasticsearchNode
     * @param context
     * @return a {@link Node}
     */
    private Node buildNode(ElasticsearchNode elasticsearchNode, Map<String, Object> context) {

        // Create the node's settings
        Settings settings = buildNodeSettings(elasticsearchNode);

        // Search for the node in current context
        String nodeName = settings.get(NODE_NAME);
        Node node = (Node) context.get(nodeName);

        if (node == null) {
            // No node with this name has been found, let's instantiate a new one
            node = NodeBuilder.nodeBuilder()
                    .settings(settings)
                    .local(elasticsearchNode.local())
                    .node();
            context.put(nodeName, node);
        }
        return node;
    }

    /**
     * Build node settings
     */
    private Settings buildNodeSettings(ElasticsearchNode elasticsearchNode) {

        // Build default settings
        Builder settingsBuilder = Settings.settingsBuilder()
                .put(NODE_NAME, elasticsearchNode.name())
                .put("node.data", elasticsearchNode.data())
                .put("cluster.name", elasticsearchNode.clusterName())
                .put("path.home", ES_HOME)
                .put("path.data", ES_HOME + "/data")
                .put("path.logs", ES_HOME + "/logs")
                .put("index.number_of_shards", "1")
                .put("index.number_of_replicas", "0");

        // Loads settings from configuration file
        String settingsFile = elasticsearchNode.configFile();
        Settings configSettings;
        try(InputStream settingsStreams = Thread.currentThread().getContextClassLoader().getResourceAsStream(settingsFile)) {
            configSettings = Settings.builder().loadFromStream(settingsFile, settingsStreams).build();
            settingsBuilder.put(configSettings);
        } catch (IOException e) {
            throw new EsSetupRuntimeException("Failed to load settings "+settingsFile, e);
        }

        // Other settings
        ElasticsearchSetting[] settings = elasticsearchNode.settings();
        for (ElasticsearchSetting setting : settings) {
            settingsBuilder.put(setting.name(), setting.value());
        }

        // Build the settings
        return settingsBuilder.build();
    }
}
