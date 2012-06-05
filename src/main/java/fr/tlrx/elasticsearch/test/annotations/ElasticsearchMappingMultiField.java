/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ElasticsearchMappingMultiField Annotation, used to define a multi_field in the mapping properties
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticsearchMappingMultiField {

    /**
     * The multi_field's name
     */
    String name();
    
    /**
     * The fields of current the multi_field
     */
    ElasticsearchMappingField[] fields();
}