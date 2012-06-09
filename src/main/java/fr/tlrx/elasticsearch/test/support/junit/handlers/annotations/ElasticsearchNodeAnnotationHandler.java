/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers.annotations;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchSetting;
import fr.tlrx.elasticsearch.test.support.junit.handlers.ClassLevelElasticsearchAnnotationHandler;
import fr.tlrx.elasticsearch.test.support.junit.handlers.FieldLevelElasticsearchAnnotationHandler;

/**
 * Handle {@link ElasticsearchNode} annotation
 * 
 * @author tlrx
 * 
 */
public class ElasticsearchNodeAnnotationHandler implements ClassLevelElasticsearchAnnotationHandler, FieldLevelElasticsearchAnnotationHandler {

	/**
	 * Elasticsearch home directory
	 */
	private static final String ES_HOME = "./elasticsearch-test";

	public boolean support(Annotation annotation) {
		return (annotation instanceof ElasticsearchNode);
	}

	public void handleBeforeClass(Annotation annotation, Object testClass, Map<String, Object> context) {
		// Instantiate a node
		ElasticsearchNode elasticsearchNode = (ElasticsearchNode) annotation;
		context.put(elasticsearchNode.name(), createNode(elasticsearchNode));
	}

	public void handleAfterClass(Annotation annotation, Object testClass, Map<String, Object> context) {
		// Shutdown all nodes
		for (Object node : context.values()) {
			if ((node instanceof Node) && (!((Node) node).isClosed())) {
				((Node) node).close();
			}
		}
		FileSystemUtils.deleteRecursively(new File(ES_HOME));
	}


	public void handleField(Annotation annotation, Object instance, Map<String, Object> context, Field field) {
		ElasticsearchNode elasticsearchNode = (ElasticsearchNode) annotation;
		Node node = (Node) context.get(elasticsearchNode.name());
		
		if(node == null){
			// No node with this name has been found, let's instantiate a new one
			node = createNode(elasticsearchNode);
			context.put(elasticsearchNode.name(), node);
		}
		
		// Sets the node as the field's value		
		try {
			field.setAccessible(true);
			field.set(instance, node);
		} catch (Exception e) {
			System.err.println("Exception when setting the node:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Instantiate a Node
	 */
	private Node createNode(ElasticsearchNode elasticsearchNode) {
		Builder settingsBuilder = ImmutableSettings.settingsBuilder();

		// Node name
		String name = elasticsearchNode.name();
		if ((name != null) && (name.length() > 0)) {
			settingsBuilder.put("node.name", name);
		}

		// Cluster name
		String clusterName = elasticsearchNode.clusterName();
		if (clusterName != null) {
			settingsBuilder.put("cluster.name", clusterName);
		} else {
			settingsBuilder.put("cluster.name", "elasticsearch-test-cluster");
		}

		// Paths
		settingsBuilder.put("path.data", ES_HOME + "/data")
				.put("path.work", ES_HOME + "/work")
				.put("path.logs", ES_HOME + "/logs")
				.put("index.store.type", "memory")
				.put("index.store.fs.memory.enabled", "true")
				.put("gateway.type", "none");

		// Other settings
		ElasticsearchSetting[] settings = elasticsearchNode.settings();
		for (ElasticsearchSetting setting : settings) {
			settingsBuilder.put(setting.name(), setting.value());
		}

		NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().settings(
				settingsBuilder.build());
		nodeBuilder = nodeBuilder.local(elasticsearchNode.local());
		nodeBuilder = nodeBuilder.data(elasticsearchNode.data());

		return nodeBuilder.node();
	}
}
