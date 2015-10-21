/**
 *
 */
package com.github.tlrx.elasticsearch.test.support.junit.handlers.annotations;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchBulkRequest;
import com.github.tlrx.elasticsearch.test.support.junit.handlers.MethodLevelElasticsearchAnnotationHandler;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handle {@link ElasticsearchBulkRequest} annotation
 *
 * @author tlrx
 */
public class ElasticsearchBulkRequestAnnotationHandler extends AbstractAnnotationHandler implements MethodLevelElasticsearchAnnotationHandler {

    private final static Logger LOGGER = Logger.getLogger(ElasticsearchBulkRequestAnnotationHandler.class.getName());

    public boolean support(Annotation annotation) {
        return (annotation instanceof ElasticsearchBulkRequest);
    }

    public void handleBefore(Annotation annotation, Object instance, Map<String, Object> context) throws Exception {
        ElasticsearchBulkRequest elasticsearchBulkRequest = (ElasticsearchBulkRequest) annotation;

        InputStream input = null;
        ByteArrayOutputStream output = null;

        try {
            // Get an AdminClient for the node
            Client client = client(context, elasticsearchBulkRequest.nodeName());

            // Load file as byte array
            input = getClass().getResourceAsStream(elasticsearchBulkRequest.dataFile());
            if (input == null) {
                input = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(
                                elasticsearchBulkRequest.dataFile());
            }
            if (input == null) {
                throw new IllegalArgumentException("Bulk file " + elasticsearchBulkRequest.dataFile() + " not found!");
            }
            output = new ByteArrayOutputStream();

            byte[] buffer = new byte[512 * 1024];
            while (input.read(buffer) > 0) {
                output.write(buffer);
            }

            buffer = output.toByteArray();

            // Execute the BulkRequest
            BulkResponse response = client.prepareBulk()
                    .add(buffer, 0, buffer.length, elasticsearchBulkRequest.defaultIndexName(), elasticsearchBulkRequest.defaultTypeName())
                    .setRefresh(true)
                    .execute()
                    .actionGet();

            LOGGER.info(String.format("Bulk request for data file '%s' executed in %d ms with %sfailures",
                    elasticsearchBulkRequest.dataFile(),
                    response.getTookInMillis(),
                    response.hasFailures() ? "" : "no "));
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public void handleAfter(Annotation annotation, Object instance, Map<String, Object> context) throws Exception {
        // Nothing to do here
    }
}
