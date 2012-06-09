/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Interface of Field-Level (class attributes) Elasticsearch's annotations
 * handlers.
 * 
 * @author tlrx
 * 
 */
public interface FieldLevelElasticsearchAnnotationHandler extends ElasticsearchAnnotationHandler {

	/**
	 * Handle an annotation on a Field before executing a first test
	 * 
	 * @param annotation
	 * @param instance
	 * @param context Test execution context
	 * @param field
	 * @throws Exception
	 */
	public void handleField(Annotation annotation, Object instance, Map<String, Object> context, Field field) throws Exception;
}
