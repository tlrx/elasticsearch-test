package fr.tlrx.elasticsearch.test.support.junit.rules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import fr.tlrx.elasticsearch.test.support.junit.handlers.ElasticsearchAnnotationHandler;

/**
 * Abstract {@link TestRule}
 */
public abstract class AbstractElasticsearchRule implements TestRule {

	/**
     * Handlers for specific elasticsearch annotations
     */
    protected List<ElasticsearchAnnotationHandler> handlers = new ArrayList<ElasticsearchAnnotationHandler>();
	
	/**
	 * Test execution context
	 */
	protected final Map<String, Object> context;

    /**
     * Constructor
     * 
     * @param context
     */
    public AbstractElasticsearchRule(Map<String, Object> context) {
		super();
		this.context = context;
	}
    
    /**
     * Add an elasticsearch annotation handler
     * 
     * @param handler
     * @return true (as specified by Collection.add)
     */
    public boolean addHandler(ElasticsearchAnnotationHandler handler) {
        return handlers.add(handler);
    }

	/**
     * {@inheritDoc}
     */
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    // Execute handlers before statement execution
                    before(description.getAnnotations());

                    // Execute statements
                    base.evaluate();
                } finally {
                    // Execute handlers after statement execution
                    after(description.getAnnotations());
                }
            }
        };
    }
    
    /**
     * Process before statement execution 
     * 
     * @param annotations
     */
    protected void before(Collection<Annotation> annotations) throws Exception {
    	// Nothing here
    }
    
    /**
     * Process after statement execution 
     * 
     * @param annotations
     */
    protected void after(Collection<Annotation> annotations) throws Exception {
    	// Nothing here
    }
    
	/**
	 * Get all declared and inherited attributes of a given class
	 * 
	 * @param type
	 * @return a {@link List} of {@link Field}
	 */
	protected List<Field> getAllFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();
		if (type != null) {
			for (Field field : type.getDeclaredFields()) {
				fields.add(field);
			}

			if (type.getSuperclass() != null) {
				fields.addAll(getAllFields(type.getSuperclass()));
			}
		}
		return fields;
	}
}