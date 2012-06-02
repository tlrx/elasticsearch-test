/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers;

import java.lang.annotation.Annotation;

import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Abstraact basic handler for elasticsearch annotation
 * 
 * @author tlrx
 * 
 */
public abstract class AbstractElasticsearchAnnotationHandler implements ElasticsearchAnnotationHandler {

	public boolean support(Annotation annotation) {
		return false;
	}
	
	public void handleBefore(ElasticsearchRunner runner, Object instance, Annotation annotation) {
		// Nothing to do
	}
	
	public void handleAfter(ElasticsearchRunner runner, Object instance, Annotation annotation) {
		// Nothing to do
	}
}
