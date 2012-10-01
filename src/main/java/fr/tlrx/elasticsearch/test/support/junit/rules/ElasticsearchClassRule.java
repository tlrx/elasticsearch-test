package fr.tlrx.elasticsearch.test.support.junit.rules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runners.model.TestClass;

import fr.tlrx.elasticsearch.test.support.junit.handlers.ClassLevelElasticsearchAnnotationHandler;
import fr.tlrx.elasticsearch.test.support.junit.handlers.ElasticsearchAnnotationHandler;
import fr.tlrx.elasticsearch.test.support.junit.handlers.FieldLevelElasticsearchAnnotationHandler;

/**
 * Simple {@link TestRule} automatically added to test classes that have an
 * {@link ESRunner}.
 * 
 * This class adds BeforeClass and AfterClass class rules, which will call
 * {@link ElasticsearchAnnotationHandler}s.
 */
public class ElasticsearchClassRule extends AbstractElasticsearchRule {

	/**
	 * The current test class
	 */
	private final TestClass testClass;

	/**
	 * Constructor
	 * 
	 * @param testClass
	 */
	public ElasticsearchClassRule(Map<String, Object> context, TestClass testClass) {
		super(context);
		this.testClass = testClass;
	}

	@Override
	protected void before(Collection<Annotation> annotations) throws Exception {
		// Manage annotations on class
		executeBeforeOrAfterClassHandlers(true, annotations);
	}

	@Override
	protected void after(Collection<Annotation> annotations) throws Exception {
		// Manage annotations before destroying object class
		executeBeforeOrAfterClassHandlers(false, annotations);
	}

	/**
	 * Execute handlers at Before/AfterClass time
	 * @throws Exception 
	 */
	private void executeBeforeOrAfterClassHandlers(boolean isBefore, Collection<Annotation> annotations) throws Exception {
		if ((annotations != null) && (!annotations.isEmpty())) {

			// Handle annotations at Before or After time
			for (ElasticsearchAnnotationHandler handler : handlers) {
				if (handler instanceof ClassLevelElasticsearchAnnotationHandler) {
					ClassLevelElasticsearchAnnotationHandler classHandler = (ClassLevelElasticsearchAnnotationHandler) handler;
					
					// Call the handler first
					if (isBefore) {
						classHandler.beforeClass(testClass, context);
					}
					
					// Iterate over annotations
					for (Annotation annotation : annotations) {
						if (handler.support(annotation)) {
							if (isBefore) {
								classHandler.handleBeforeClass(annotation, testClass, context);
							} else {
								classHandler.handleAfterClass(annotation, testClass, context);
							}
						}
					}
					
					// Call the handler after
					if (!isBefore) {
						classHandler.afterClass(testClass, context);
					}
				}
			}
		}
	}
	
	/**
	 * Manage annotations on class attributes (fields)
	 * 
	 * @param instance test class instance
	 * @throws Exception
	 */
	public void executeBeforeTestExecution(Object instance) throws Exception {
		for (ElasticsearchAnnotationHandler handler : handlers) {
			if (handler instanceof FieldLevelElasticsearchAnnotationHandler) {
				FieldLevelElasticsearchAnnotationHandler fieldHandler = (FieldLevelElasticsearchAnnotationHandler) handler;

				for(Field field : instance.getClass().getDeclaredFields()){
					// Iterate over annotations
					for (Annotation annotation : field.getAnnotations()) {
						if (handler.support(annotation)) {
							fieldHandler.handleField(annotation, instance, context, field);
						}
					}
				}
			}
		}
	}
}