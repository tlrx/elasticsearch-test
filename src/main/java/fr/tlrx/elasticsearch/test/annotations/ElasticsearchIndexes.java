/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ElasticsearchIndexes Annotation
 * 
 * This annotation is used to define multiple {@link ElasticsearchIndex}
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ElasticsearchIndexes {
	
	/**
	 * Indexes
	 */
	ElasticsearchIndex[] indexes() default {};

}
