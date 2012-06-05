/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ElasticsearchMapping Annotation, used to create mapping for a given document type
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticsearchMapping {

	/**
	 * The type's name for which the mapping is defined
	 */
	String typeName();
	
	/**
	 * Fields of the mapping
	 */
	ElasticsearchMappingField[] properties();
	
	/**
     * Multi Fields of the mapping
     */
    ElasticsearchMappingMultiField[] propertiesMulti() default {};
}
