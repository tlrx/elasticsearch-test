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
     * The source's "compress" value (default to true (since v0.90))
     */
    boolean compress() default true;

    /**
     * Compress threshold value
     */
    String compressThreshold() default "";

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
     * The path used to extract the timestamp from the document
     * (default to "", meaning the "_timestamp" field should be explicitly set when indexing)
     */
    String timestampPath() default "";

    /**
     * The date format of the "_timestamp" field (default to "dateOptionalTime")
     */
    String timestampFormat() default "dateOptionalTime";

    /**
     * The _parent's type
     */
    String parent() default "";

}
