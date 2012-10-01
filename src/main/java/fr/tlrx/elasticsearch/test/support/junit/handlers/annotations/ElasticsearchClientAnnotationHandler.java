/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Logger;

import org.elasticsearch.node.Node;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import fr.tlrx.elasticsearch.test.support.junit.handlers.FieldLevelElasticsearchAnnotationHandler;

/**
 * Handle {@link ElasticsearchClient} annotation
 * 
 * @author tlrx
 * 
 */
public class ElasticsearchClientAnnotationHandler implements FieldLevelElasticsearchAnnotationHandler {

	private final static Logger LOGGER = Logger.getLogger(ElasticsearchClientAnnotationHandler.class.getName()); 
	
	public boolean support(Annotation annotation) {
		return (annotation instanceof ElasticsearchClient);
	}

	public void handleField(Annotation annotation, Object instance, Map<String, Object> context, Field field) {
		ElasticsearchClient elasticsearchClient = (ElasticsearchClient)annotation;
		String nodeName = elasticsearchClient.nodeName();
		
		if(nodeName != null){
			Node node = (Node) context.get(nodeName);
			if (node != null) {					
				try {
					field.setAccessible(true);
					field.set(instance, node.client());					
				} catch (Exception e) {
					LOGGER.severe("Unable to set node for field " + field.getName() + ":" + e.getMessage());
				}
			}
		}		
	}
}
