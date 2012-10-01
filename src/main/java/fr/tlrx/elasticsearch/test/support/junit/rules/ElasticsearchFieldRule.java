package fr.tlrx.elasticsearch.test.support.junit.rules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runners.model.TestClass;

import fr.tlrx.elasticsearch.test.support.junit.handlers.ElasticsearchAnnotationHandler;
import fr.tlrx.elasticsearch.test.support.junit.handlers.FieldLevelElasticsearchAnnotationHandler;

/**
 * Simple {@link TestRule} automatically added to test classes that have an
 * {@link ESRunner}.
 * 
 * This class manages class attributes (fields) and call
 * {@link ElasticsearchAnnotationHandler}s.
 */
public class ElasticsearchFieldRule extends AbstractElasticsearchRule {

	/**
	 * Constructor
	 * 
	 * @param testClass
	 */
	public ElasticsearchFieldRule(Map<String, Object> context, TestClass testClass) {
		super(context);
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

				for(Field field : getAllFields(instance.getClass())){
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