/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ElasticsearchMappingField Annotation, used to define the mapping properties  for a given document field
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticsearchMappingField {

	public enum Types {
		String, MultiField
	}
	
	public enum Store {
		Yes,No
	}
	
	/**
	 * The field's name for which properties are defined
	 */
	String name();
	
	/**
	 * Default field type
	 */
	Types type() default Types.String;
		
	/**
	 * Store property (true means "yes", false means "no" and default is set to false)
	 */
	Store store() default Store.No;
}
