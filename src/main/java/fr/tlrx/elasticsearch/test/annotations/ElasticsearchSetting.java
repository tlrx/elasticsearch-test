/**
 * 
 */
package fr.tlrx.elasticsearch.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.elasticsearch.common.settings.Settings;

/**
 * ElasticsearchSetting Annotation
 * 
 * This annotation is used to define elasticsearch {@link Settings}
 * 
 * @author tlrx
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ElasticsearchSetting {

	/**
	 * Name
	 */
	String name();

	/**
	 * Value
	 */
	String value();

}
