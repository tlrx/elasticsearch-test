/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers;

import java.lang.annotation.Annotation;

/**
 * Marker interface for Elasticsearch's annotations handlers.
 * 
 * @author tlrx
 * 
 */
public interface ElasticsearchAnnotationHandler {

    /**
     * Check if the handler supports a given annotation
     * 
     * @param annotation
     * @return true if the annotation is supported
     */
    public boolean support(Annotation annotation);

}
