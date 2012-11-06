/**
 *
 */
package com.github.tlrx.elasticsearch.test.support.junit.handlers;

import java.lang.annotation.Annotation;
import java.util.Map;


/**
 * Interface of Method-Level Elasticsearch's annotations handlers.
 *
 * @author tlrx
 */
public interface MethodLevelElasticsearchAnnotationHandler extends ElasticsearchAnnotationHandler {

    /**
     * Handle an annotation before method execution
     *
     * @param annotation
     * @param instance
     * @param context    Test execution context
     * @throws Exception
     * @see org.junit.Before
     */
    public void handleBefore(Annotation annotation, Object instance, Map<String, Object> context) throws Exception;

    /**
     * Handle an annotation after method execution
     *
     * @param annotation
     * @param instance
     * @param context    Test execution context
     * @throws Exception
     * @see org.junit.After
     */
    public void handleAfter(Annotation annotation, Object instance, Map<String, Object> context) throws Exception;
}
