/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.LocalTransportAddress;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchTransportClient;
import fr.tlrx.elasticsearch.test.support.junit.handlers.FieldLevelElasticsearchAnnotationHandler;

/**
 * Handle {@link ElasticsearchTransportClient} annotation
 * 
 * @author tlrx
 * 
 */
public class ElasticsearchTransportClientAnnotationHandler implements
		FieldLevelElasticsearchAnnotationHandler {

	public boolean support(Annotation annotation) {
		return (annotation instanceof ElasticsearchTransportClient);
	}

	public void handleField(Annotation annotation, Object instance, Map<String, Object> context, Field field) {
		ElasticsearchTransportClient elasticsearchTransportClient = (ElasticsearchTransportClient) annotation;

		// Settings
		Settings settings = ImmutableSettings.settingsBuilder()
								.put("node.local", String.valueOf(elasticsearchTransportClient.local()))
								.put("cluster.name", String.valueOf(elasticsearchTransportClient.clusterName()))
								.build();
		
		TransportClient client = new TransportClient(settings);

		// Instantiate a local TransportClient
		if (elasticsearchTransportClient.local()) {
			for (String id : elasticsearchTransportClient.ids()) {
				client.addTransportAddress(new LocalTransportAddress(id));
			}
			
		// Instantiate a TransportClient
		} else {
			int n = 0;
			for (String host : elasticsearchTransportClient.hostnames()) {
				client.addTransportAddress(new InetSocketTransportAddress(host, elasticsearchTransportClient.ports()[n++]));
			}
		}
		if (client != null) {
			try {
				field.setAccessible(true);
				field.set(instance, client);
			} catch (Exception e) {
				System.err.println("Unable to set transport client for field " + field.getName());
				e.printStackTrace(System.err);
			}
		}
	}
}
