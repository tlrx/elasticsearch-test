package com.github.tlrx.elasticsearch.test.request;

import com.github.tlrx.elasticsearch.test.EsSetup;
import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateAction;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequestBuilder;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.junit.Before;
import org.junit.Test;

import static com.github.tlrx.elasticsearch.test.EsSetup.createTemplate;
import static com.github.tlrx.elasticsearch.test.EsSetup.deleteTemplates;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link DeleteTemplates} request
 */
public class DeleteTemplatesTest {
    private static final String TEST_TEMPLATE_NAME = "test-template";
    private EsSetup esSetup;
    private static final String TEST_TEMPLATE_SOURCE = "{\n" +
            "\t\"template\": \""+TEST_TEMPLATE_NAME+"\",\n" +
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

    @Before
    public void setUp() {
        esSetup = new EsSetup();
    }
    public void tearDown() {
        esSetup.execute(deleteTemplates());
    }
    public boolean existsTemplate(String templateName) {
        ClusterStateRequestBuilder clusterStateRequestBuilder =
                ClusterStateAction.INSTANCE.newRequestBuilder(esSetup.client().admin().cluster())
                        .all().setMetaData(false);
        ClusterStateResponse clusterStateResponse = clusterStateRequestBuilder.execute().actionGet();
        IndexTemplateMetaData indexTemplateMetaData = clusterStateResponse.getState().getMetaData()
                .getTemplates().get(templateName);
        return indexTemplateMetaData != null;
    }

    @Test
    public void testDeleteTemplates() {
        esSetup.execute(createTemplate(TEST_TEMPLATE_NAME).withSource(TEST_TEMPLATE_SOURCE));
        esSetup.execute(deleteTemplates(TEST_TEMPLATE_NAME));
        assertFalse(existsTemplate(TEST_TEMPLATE_NAME));
    }

    @Test
    public void testDeleteAllTemplates() {
        esSetup.execute(createTemplate(TEST_TEMPLATE_NAME).withSource(TEST_TEMPLATE_SOURCE));
        esSetup.execute(deleteTemplates());
        assertFalse(existsTemplate(TEST_TEMPLATE_NAME));
    }


    @Test(expected = EsSetupRuntimeException.class)
    public void testDeleteTemplatesFailSlow() {
        EsSetup esSetup = new EsSetup();
        esSetup.execute(createTemplate(TEST_TEMPLATE_NAME).withSource(TEST_TEMPLATE_SOURCE));
        esSetup.execute(deleteTemplates("test-fail", TEST_TEMPLATE_NAME));
        assertFalse(existsTemplate(TEST_TEMPLATE_NAME));
    }

    @Test(expected = EsSetupRuntimeException.class)
    public void testDeleteTemplatesFailFast() {
        EsSetup esSetup = new EsSetup();
        esSetup.execute(createTemplate(TEST_TEMPLATE_NAME).withSource(TEST_TEMPLATE_SOURCE));
        esSetup.execute(deleteTemplates("test-fail", TEST_TEMPLATE_NAME).failFast());
        assertTrue(existsTemplate(TEST_TEMPLATE_NAME));
    }
}