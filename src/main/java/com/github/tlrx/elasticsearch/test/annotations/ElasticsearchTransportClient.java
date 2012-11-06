/**
 *
 */
package com.github.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ElasticsearchTransportClient Annotation
 *
 * @author tlrx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ElasticsearchTransportClient {

    /**
     * The cluster's name, default to "elasticsearch-test-cluster"
     */
    String clusterName() default ElasticsearchNode.DEFAULT_CLUSTER_NAME;

    /**
     * Array of host names, default to "localhost"
     */
    String[] hostnames() default {"localhost"};

    /**
     * Array of port numbers, default to 9300
     */
    int[] ports() default {9300};
}
