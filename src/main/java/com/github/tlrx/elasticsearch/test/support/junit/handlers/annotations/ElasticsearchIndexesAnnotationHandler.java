/**
 *
 */
package com.github.tlrx.elasticsearch.test.support.junit.handlers.annotations;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchIndexes;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Handle {@link ElasticsearchIndexes} annotation
 *
 * @author tlrx
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