/**
 * 
 */
package fr.tlrx.elasticsearch.test.support.junit.handlers;

import java.lang.annotation.Annotation;
import java.util.Map;


/**
 * Interface of Class-Level Elasticsearch's annotations handlers.
 * 
 * @author tlrx
 * 
 */
public interface ClassLevelElasticsearchAnnotationHandler extends ElasticsearchAnnotationHandler {
    
	/**
     * Call the handler at class instanciation time
     * 
     * @param testClass
     * @param context Test execution context
     * @throws Exception
     * 
     * @see org.junit.BeforeClass
     */
    public void beforeClass(Object testClass, Map<String, Object> context) throws Exception;
    
    /**
     * Handle an annotation at class instanciation time
     * 
     * @param annotation
     * @param testClass
     * @param context Test execution context
     * @throws Exception
     * 
     * @see org.junit.BeforeClass
     */
    public void handleBeforeClass(Annotation annotation, Object testClass, Map<String, Object> context) throws Exception;
    
    /**
     * Handle an annotation at class destroying time
     * 
     * @param annotation
     * @param testClass
     * @param context Test execution context
     * @throws Exception
     * 
     * @see org.junit.AfterClass
     */
    public void handleAfterClass(Annotation annotation, Object testClass, Map<String, Object> context) throws Exception;
    
    /**
     * Call the handler at class destroying time
     * 
     * @param testClass
     * @param context Test execution context
     * @throws Exception
     * 
     * @see org.junit.AfterClass
     */
    public void afterClass(Object testClass, Map<String, Object> context) throws Exception;
}
