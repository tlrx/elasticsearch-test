/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ElasticsearchIndex Annotation, used to define an elasticsearch index with a
 * configuration file
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ElasticsearchIndexFile {

	/**
	 * Default index name
	 */
	public static final String DEFAULT_NAME = "test";

	/**
	 * The index's name, default to "test"
	 */
	String indexName() default DEFAULT_NAME;

	/**
	 * The node's name from which a client is instantiated to run operations
	 */
	String nodeName() default ElasticsearchNode.DEFAULT_NODE_NAME;

	/**
	 * File
	 */
	String file() default "";
}
