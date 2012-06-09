/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers.annotations;

import java.lang.annotation.Annotation;
import java.util.Map;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndexes;

/**
 * Handle {@link ElasticsearchIndexes} annotation
 * 
 * @author tlrx
 * 
 */
public class ElasticsearchIndexesAnnotationHandler extends ElasticsearchIndexAnnotationHandler {

	public boolean support(Annotation annotation) {
		return (annotation instanceof ElasticsearchIndexes);
	}

	public void handleBefore(Annotation annotation, Object instance, Map<String, Object> context) throws Exception {
		// Manage @ElasticsearchIndexes
		for (ElasticsearchIndex index : ((ElasticsearchIndexes) annotation).indexes()) {
			buildIndex((ElasticsearchIndex) index, context);
		}
	}

	public void handleAfter(Annotation annotation, Object instance, Map<String, Object> context) throws Exception {
		// TODO clean after Nothing
	}

}