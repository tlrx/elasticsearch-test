/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ElasticsearchNode Annotation
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
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
	 * Index settings
	 */
	ElasticsearchSetting[] settings() default {};	
}
