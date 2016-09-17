/**
 *
 */
package com.github.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ElasticsearchMappingField Annotation, used to define the mapping properties  for a given document field
 *
 * @author tlrx
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticsearchMappingField {

    /**
     * Globally defined analyzer
     */
    public static final String DEFAULT_ANALYZER = "default-analyzer";

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

    /**
     * Index property (analyzed, not_analyzed, no)
     */
    Index index() default Index.Undefined;

    /**
     * Name of the analyzer
     */
    String analyzerName() default DEFAULT_ANALYZER;

    /**
     * The analyzer used to analyze the field when part of a query string.
     */
    String searchAnalyzerName() default DEFAULT_ANALYZER;

    /**
     * The term_vector value for analyzed field
     */
    TermVector termVector() default TermVector.No;

    /**
     * The fields
     */
    ElasticsearchMappingSubField[] fields() default {};
}