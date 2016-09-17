package com.github.tlrx.elasticsearch.test.support.junit.runners;

import com.github.tlrx.elasticsearch.test.support.junit.handlers.annotations.*;
import com.github.tlrx.elasticsearch.test.support.junit.rules.ElasticsearchClassRule;
import com.github.tlrx.elasticsearch.test.support.junit.rules.ElasticsearchFieldRule;
import com.github.tlrx.elasticsearch.test.support.junit.rules.ElasticsearchTestRule;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JUnit Runner used to run test classes that have Elasticsearch annotations.
 *
 * @author tlrx
 */
public class ElasticsearchRunner extends BlockJUnit4ClassRunner {

    /**
     * Map used to store test execution context
     */
    Map<String, Object> context = new ConcurrentHashMap<String, Object>();

    /**
     * Constructor
     *
     * @param klass
     * @throws InitializationError
     */
    public ElasticsearchRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        // Get BlockJUnit4ClassRunner's default class rules
        List<TestRule> testRules = super.getTestRules(target);

        // Instantiate a specific JUnit TestRule
        ElasticsearchTestRule testRule = new ElasticsearchTestRule(context, target);

        // Declares the elasticsearch annotations handlers to use
        // Be careful, order is important
        testRule.addHandler(new ElasticsearchIndexesAnnotationHandler());
        testRule.addHandler(new ElasticsearchIndexAnnotationHandler());
        testRule.addHandler(new ElasticsearchBulkRequestAnnotationHandler());

        // Add a TestRule to manage method-level Elasticsearch annotations
        testRules.add(testRule);

        return testRules;
    }

    @Override
    protected List<TestRule> classRules() {
        // Get BlockJUnit4ClassRunner's default class rules
        List<TestRule> classRules = super.classRules();

        // Instantiate a specific JUnit TestRule, executed before/after every test class instantiation
        ElasticsearchClassRule classRule = new ElasticsearchClassRule(context, getTestClass());

        // Declares the elasticsearch annotations handlers to use
        // Be careful, order is important
        classRule.addHandler(new ElasticsearchNodeAnnotationHandler());
        classRule.addHandler(new ElasticsearchTransportClientAnnotationHandler());

        // Add a ClassRule to manage class-level Elasticsearch annotations
        classRules.add(classRule);

        return classRules;
    }

    @Override
    protected Object createTest() throws Exception {
        Object instance = super.createTest();

        // Instantiate a specific JUnit TestRule, executed before every test class instantiation
        ElasticsearchFieldRule fieldsRule = new ElasticsearchFieldRule(context, getTestClass());

        // Declares the elasticsearch annotations handlers to use
        // Be careful, order is important
        fieldsRule.addHandler(new ElasticsearchNodeAnnotationHandler());
        fieldsRule.addHandler(new ElasticsearchClientAnnotationHandler());
        fieldsRule.addHandler(new ElasticsearchAdminClientAnnotationHandler());
        fieldsRule.addHandler(new ElasticsearchTransportClientAnnotationHandler());

        // Manage annotations on class attributes
        fieldsRule.executeBeforeTestExecution(instance);

        return instance;
    }

}