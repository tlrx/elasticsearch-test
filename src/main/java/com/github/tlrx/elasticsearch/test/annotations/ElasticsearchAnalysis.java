/**
 *
 */
package com.github.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ElasticsearchAnalysis Annotation, used to define analysis settings of an
 * ElasticsearchIndex
 *
 * @author tlrx
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticsearchAnalysis {

    /**
     * Filters
     */
    ElasticsearchFilter[] filters() default {};

    /**
     * Analyzers
     */
    ElasticsearchAnalyzer[] analyzers() default {};
}
