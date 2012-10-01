package fr.tlrx.elasticsearch.test.support.junit.rules;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.rules.TestRule;

import fr.tlrx.elasticsearch.test.support.junit.handlers.ElasticsearchAnnotationHandler;
import fr.tlrx.elasticsearch.test.support.junit.handlers.MethodLevelElasticsearchAnnotationHandler;

/**
 * Simple {@link TestRule} automatically added to test classes that have an {@link ESRunner}.
 * 
 * This class adds Before and After class rules, which will call {@link ElasticsearchAnnotationHandler}s.
 */
public class ElasticsearchTestRule extends AbstractElasticsearchRule {
	
	private final static Logger LOGGER = Logger.getLogger(ElasticsearchTestRule.class.getName()); 
	
    /**
     * The current test instance
     */
    private final Object instance;

    /**
     * Constructor
     * 
     * @param testInstance
     */
    public ElasticsearchTestRule(Map<String, Object> context, Object testInstance) {
        super(context);
        this.instance = testInstance;
    }

    @Override
    protected void before(Collection<Annotation> annotations) throws Exception {
        executeBeforeOrAfterMethodHandlers(true, annotations);
    }

    @Override
    protected void after(Collection<Annotation> annotations) throws Exception {
        executeBeforeOrAfterMethodHandlers(false, annotations);
    }

    /**
     * Execute handlers at Before/After method time
     * @throws Exception 
     */
    private void executeBeforeOrAfterMethodHandlers(boolean isBefore, Collection<Annotation> annotations) throws Exception {
        if ((annotations != null) && (!annotations.isEmpty())) {
            
            // Handle annotations at Before or after time
            for (ElasticsearchAnnotationHandler handler : handlers) {
                if (handler instanceof MethodLevelElasticsearchAnnotationHandler) {

                    // Iterate over annotations
                    for (Annotation annotation : annotations) {
                        if (handler.support(annotation)) {
                            if (isBefore) {
                                ((MethodLevelElasticsearchAnnotationHandler) handler).handleBefore(annotation, instance, context);
                            } else {
                            	try {
                            		((MethodLevelElasticsearchAnnotationHandler) handler).handleAfter(annotation, instance, context);	
								} catch (Exception e) {
									LOGGER.severe(e.getMessage());
								}
                            }
                        }
                    }
                }
            }
        }
    }
}