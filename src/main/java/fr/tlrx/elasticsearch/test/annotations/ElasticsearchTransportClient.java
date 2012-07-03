/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ElasticsearchTransportClient Annotation
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ElasticsearchTransportClient {
	
	/**
	 * The cluster's name, default to "elasticsearch-test-cluster"
	 */
	String clusterName() default ElasticsearchNode.DEFAULT_CLUSTER_NAME;
	
	/**
	 * The local property of the transport client, default to "true"
	 */
	boolean local() default true;
	
	/**
	 * Array of local ids, used when local = true
	 * 
	 * Default set to "1"
	 */
	String[] ids() default {"1"};
	
	/**
	 * Array of host names, used when local = false
	 */
	String[] hostnames() default {};
	
	/**
	 * Array of port numbers, used when local = false
	 */
	int[] ports() default {};
}
