/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ElasticsearchNode Annotation, used to instantiate Elasticsearch nodes.<br/>
 * <br/>
 * The annotation can be placed on Class or Class attributes. By default, the
 * node is local, holds data, and has the name "elasticsearch-test-node" and the
 * cluster's name "elasticsearch-test-cluster".
 * <br/>
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface ElasticsearchNode {

	/**
	 * Default node name
	 */
	public static final String DEFAULT_NODE_NAME = "elasticsearch-test-node";

	/**
	 * The node's name, default to "elasticsearch-test-node"
	 */
	String name() default DEFAULT_NODE_NAME;

	/**
	 * Default cluster name
	 */
	public static final String DEFAULT_CLUSTER_NAME = "elasticsearch-test-cluster";

	/**
	 * The cluster's name, default to "elasticsearch-test-cluster"
	 */
	String clusterName() default DEFAULT_CLUSTER_NAME;

	/**
	 * The local property of the node, default to "true"
	 */
	boolean local() default true;

	/**
	 * The data property of the node, default to "true"
	 */
	boolean data() default true;

	/**
	 * Node settings, defined with annotations
	 */
	ElasticsearchSetting[] settings() default {};
	
	/**
	 * Node settings defined in a configuration file. By default, the file
	 * /config/elasticsearch.yml is loaded.
	 */
	String configFile() default "config/elasticsearch.yml";
}
