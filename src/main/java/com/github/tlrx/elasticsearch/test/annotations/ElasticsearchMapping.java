/**
 *
 */
package com.github.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ElasticsearchMapping Annotation, used to create mapping for a given document type
 *
 * @author tlrx
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
    ElasticsearchMappingField[] properties() default {};

    /**
     * Multi Fields of the mapping
     */
    ElasticsearchMappingMultiField[] propertiesMulti() default {};

    /**
     * The source's "enabled" value (default to true)
     */
    boolean source() default true;

    /**
     * The source's "compress" value (default to false)
     */
    boolean compress() default false;

    /**
     * Time To Live "enabled" value (default to false)
     */
    boolean ttl() default false;

    /**
     * Time To Live value
     */
    String ttlValue() default "";
}
