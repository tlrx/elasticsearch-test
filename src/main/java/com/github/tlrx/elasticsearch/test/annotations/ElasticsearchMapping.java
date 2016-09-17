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
     * The source's "enabled" value (default to true)
     */
    boolean source() default true;

    /**
     * Time To Live "enabled" value (default to false)
     */
    boolean ttl() default false;

    /**
     * Time To Live value
     */
    String ttlValue() default "";

    /**
     * Index the document's timestamp using the _timestamp field (default to false)
     */
    boolean timestamp() default false;

    /**
     * The date format of the "_timestamp" field (default to "dateOptionalTime")
     */
    String timestampFormat() default "dateOptionalTime";

    /**
     * The _parent's type
     */
    String parent() default "";

}
