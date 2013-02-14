package com.github.tlrx.elasticsearch.test.request;

import com.github.tlrx.elasticsearch.test.EsSetup;
import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import org.junit.Test;

import static com.github.tlrx.elasticsearch.test.EsSetup.createTemplate;
import static com.github.tlrx.elasticsearch.test.EsSetup.deleteTemplates;

/**
 * Unit test for the {@link DeleteTemplates} request
 */
public class DeleteTemplatesTest {

    private static final String TEST_TEMPLATE_SOURCE = "{\n" +
            "\t\"template\": \"test-template\",\n" +
            "\t\"mappings\": {\n" +
            "\t\t\"test\": {\n" +
            "\t\t\t\"properties\" : {\n" +
            "\t\t\t\t\"name\" : {\n" +
            "\t\t\t\t\t\"type\" : \"string\"\n" +
            "\t\t\t\t}" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t}\n" +
            "}";

    @Test
    public void testDeleteTemplates() {
        EsSetup esSetup=new EsSetup();
        esSetup.execute(createTemplate("test-template").withSource(TEST_TEMPLATE_SOURCE));
        esSetup.execute(deleteTemplates("test-template"));
        // TODO Check that "test-template" template is not here anymore
    }
    @Test(expected = EsSetupRuntimeException.class)
    public void testDeleteTemplatesFailSlow() {
        EsSetup esSetup=new EsSetup();
        esSetup.execute(createTemplate("test-template").withSource(TEST_TEMPLATE_SOURCE));
        esSetup.execute(deleteTemplates("test-fail", "test-template"));
        // TODO Check that "test-template" template is not here anymore
    }
    @Test(expected = EsSetupRuntimeException.class)
    public void testDeleteTemplatesFailFast() {
        EsSetup esSetup=new EsSetup();
        esSetup.execute(createTemplate("test-template").withSource(TEST_TEMPLATE_SOURCE));
        esSetup.execute(deleteTemplates("test-fail", "test-template").failFast());
        // TODO Check that "test-template" template is still here
    }
}