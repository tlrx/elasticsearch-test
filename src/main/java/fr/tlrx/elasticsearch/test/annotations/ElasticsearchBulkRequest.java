/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ElasticsearchData Annotation used to execute Bulk request against a given
 * index
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ElasticsearchBulkRequest {

	/**
	 * The default index's name used by default on bulk items that don't have an
	 * explicit index name
	 */
	String defaultIndexName() default "";

	/**
	 * The default document type name used by default on bulk items that don't
	 * have an explicit document type
	 */
	String defaultTypeName() default "";

	/**
	 * The node's name from which a client is instantiated to run bulk request
	 */
	String nodeName() default ElasticsearchNode.DEFAULT_NODE_NAME;

	/**
	 * JSON file containing the Bulk request items
	 */
	String dataFile() default "";
}
