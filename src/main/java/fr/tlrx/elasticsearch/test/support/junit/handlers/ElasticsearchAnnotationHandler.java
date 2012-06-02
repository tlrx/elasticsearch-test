/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers;

import java.lang.annotation.Annotation;

import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Handler for Elasticsearch's annotations.
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
	
	/**
	 * Handle an annotation before method invocation
	 * 
	 * @param runner
	 * @param instance
	 * @param annotation
	 */
	public void handleBefore(ElasticsearchRunner runner, Object instance, Annotation annotation);
	
	/**
	 * Handle an annotation after method invocation
	 * 
	 * @param runner
	 * @param instance
	 * @param annotation
	 */
	public void handleAfter(ElasticsearchRunner runner, Object instance, Annotation annotation);
}
