/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ElasticsearchClient Annotation
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ElasticsearchClient {

	/**
	 * The node's name from which a client is instantiated, default to "elasticsearch-test-node"
	 */
	String nodeName() default ElasticsearchNode.DEFAULT_NODE_NAME;
}
