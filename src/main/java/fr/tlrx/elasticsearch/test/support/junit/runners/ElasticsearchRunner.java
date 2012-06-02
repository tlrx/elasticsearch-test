/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.runners;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchSetting;
import fr.tlrx.elasticsearch.test.support.junit.handlers.ElasticsearchAnnotationHandler;
import fr.tlrx.elasticsearch.test.support.junit.handlers.ElasticsearchIndexAnnotationHandler;

/**
 * @author tlrx
 * 
 */
public class ElasticsearchRunner extends BlockJUnit4ClassRunner {

	/**
	 * Nodes
	 */
	private Map<String, Node> nodes = new ConcurrentHashMap<String, Node>();

	/**
	 * Handlers for specific elasticsearch annotations set on method
	 */
	private List<ElasticsearchAnnotationHandler> methodHandlers = new ArrayList<ElasticsearchAnnotationHandler>();
	
	/**
	 * Elasticsearch home directory
	 */
	private static final String ES_HOME = "./elasticsearch-test";
	
	/**
	 * Constructor
	 * 
	 * @param klass
	 * @throws InitializationError
	 */
	public ElasticsearchRunner(Class<?> klass) throws InitializationError {
		super(klass);
		
		// Add annotations handlers
		methodHandlers.add(new ElasticsearchIndexAnnotationHandler());
	}
	
	/**
	 * Initialization of Nodes
	 * 
	 * @throws Exception
	  */
	private void initNodes(Object obj) throws Exception {
		for (Field field : obj.getClass().getDeclaredFields()) {
			// Manage @ElasticsearchNode
			if (field.isAnnotationPresent(ElasticsearchNode.class)) {
				ElasticsearchNode elasticsearchNode = field.getAnnotation(ElasticsearchNode.class);
				String nodeName = elasticsearchNode.name();

				// Instantiate a new Node and put it in the nodes map
				Node node = nodes.get(nodeName);
				if (node == null) {
					node = createNode(nodeName, elasticsearchNode.clusterName(), elasticsearchNode.local(), elasticsearchNode.data(), elasticsearchNode.settings());
					nodes.put(nodeName, node);
				}
				
				try {
					field.setAccessible(true);
					field.set(obj, node);	
				} catch (Exception e) {
					throw e;
				}				
			}
		}
	}

	/**
	 * Initialization of Clients
	 * 
	 * @throws Exception
	 */
	private void initClients(Object obj) throws Exception {
		for (Field field : obj.getClass().getDeclaredFields()) {
			String nodeName = null;
			boolean admin = false;
			
			// Manage @ElasticsearchClient
			if (field.isAnnotationPresent(ElasticsearchClient.class)) {
				ElasticsearchClient elasticsearchClient = field.getAnnotation(ElasticsearchClient.class);
				nodeName = elasticsearchClient.nodeName();
			
			// Manage @ElasticsearchClient
			} else if (field.isAnnotationPresent(ElasticsearchAdminClient.class)) {
				ElasticsearchAdminClient elasticsearchAdminClient = field.getAnnotation(ElasticsearchAdminClient.class);
				nodeName = elasticsearchAdminClient.nodeName();
				admin = true;
			}
			
			if(nodeName != null) {			
				// Instantiate a new Node and put it in the nodes map
				Node node = nodes.get(nodeName);
				if (node == null) {					
					// Instantiate a default node for this client
					node = createNode(nodeName, null, null, null, null);
					nodes.put(nodeName, node);
				}
				try {
					field.setAccessible(true);
					if (admin) {
						field.set(obj, node.client().admin());
					} else {
						field.set(obj, node.client());
					}						
				} catch (Exception e) {
					System.err.println("Unable to set node for field " + field.getName());
					e.printStackTrace(System.err);
				}
			}
		}
	}
	
	@Override
	protected Object createTest() throws Exception {
		Object test = super.createTest();
		initNodes(test);
		initClients(test);
		return test;
	}

	@Override
	protected Statement methodInvoker(FrameworkMethod method, Object test) {
		// Handle annotations before method invocation
		if (!methodHandlers.isEmpty()) {
			for (Annotation annotation : method.getAnnotations()) {
				for (ElasticsearchAnnotationHandler handler : methodHandlers) {
					if (handler.support(annotation)) {
						handler.handleBefore(this, test, annotation);
					}
				}
			}
		}
		// Invoke method
		Statement statement = super.methodInvoker(method, test);
		
		// Handle annotations after method invocation
		if (!methodHandlers.isEmpty()) {
			for (Annotation annotation : method.getAnnotations()) {
				for (ElasticsearchAnnotationHandler handler : methodHandlers) {
					if (handler.support(annotation)) {
						handler.handleAfter(this, test, annotation);
					}
				}
			}
		}
		return statement;
	}

	@Override
	public void run(RunNotifier notifier) {
		super.run(notifier);
		shutdown();
	}

	/**
	 * Shutdown nodes
	 */
	private void shutdown() {
		for (Node node : nodes.values()) {
			if (!node.isClosed()) {
				node.close();
			}
		}
		FileSystemUtils.deleteRecursively(new File(ES_HOME));
	}

	/**
	 * Instantiate a Node
	 */
	private Node createNode(String name, String clusterName, Boolean local, Boolean data, ElasticsearchSetting[] settings) {
		Builder settingsBuilder = ImmutableSettings.settingsBuilder();
		
		// Node name
		if ((name != null) && (name.length() > 0)) {
			settingsBuilder.put("node.name", name);
		}
		
		// Cluster name
		if (clusterName != null) {
			settingsBuilder.put("cluster.name", clusterName);
		} else {
			settingsBuilder.put("cluster.name", "elasticsearch-test-cluster");
		}

		// Paths
		settingsBuilder
			.put("path.data", ES_HOME + "/data")
			.put("path.work", ES_HOME + "/work")
			.put("path.logs", ES_HOME + "/logs");
		
		// Other settings
		if (settings != null && settings.length > 0) {
			for (ElasticsearchSetting setting : settings) {
				settingsBuilder.put(setting.name(), setting.value());
			}
		}
		
		NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().settings(settingsBuilder.build());
		if (local != null) {
			nodeBuilder = nodeBuilder.local(local);
		} else {
			nodeBuilder = nodeBuilder.local(true);
		}
		if (data != null) {
			nodeBuilder = nodeBuilder.data(data);
		} else {
			nodeBuilder = nodeBuilder.data(true);
		}
		return nodeBuilder.node();
	}

	/**
	 * @return the {@link Node} used in the JUnit {@link Runner}
	 */
	public Map<String, Node> nodes() {
		return nodes;
	}
	
	/**
	 * @return the {@link Node} used in the JUnit {@link Runner}
	 */
	public Node node(String nodeName) {
		Node node = nodes.get(nodeName);
		if (node == null && nodes.size() > 0) {
			node = nodes.values().iterator().next();
		}
		return node;
	}
}
