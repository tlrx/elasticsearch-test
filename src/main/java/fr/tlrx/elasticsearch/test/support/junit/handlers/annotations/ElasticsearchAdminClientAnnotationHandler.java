/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

import org.elasticsearch.node.Node;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import fr.tlrx.elasticsearch.test.support.junit.handlers.FieldLevelElasticsearchAnnotationHandler;

/**
 * Handle {@link ElasticsearchAdminClient} annotation
 * 
 * @author tlrx
 * 
 */
public class ElasticsearchAdminClientAnnotationHandler implements FieldLevelElasticsearchAnnotationHandler {

	public boolean support(Annotation annotation) {
		return (annotation instanceof ElasticsearchAdminClient);
	}

	public void handleField(Annotation annotation, Object instance, Map<String, Object> context, Field field) {
		ElasticsearchAdminClient elasticsearchAdminClient = (ElasticsearchAdminClient)annotation;
		String nodeName = elasticsearchAdminClient.nodeName();
		
		if(nodeName != null){
			Node node = (Node) context.get(nodeName);
			if (node != null) {					
				try {
					field.setAccessible(true);
					field.set(instance, node.client().admin());					
				} catch (Exception e) {
					System.err.println("Unable to set node for field " + field.getName());
					e.printStackTrace(System.err);
				}
			}
		}		
	}
}
