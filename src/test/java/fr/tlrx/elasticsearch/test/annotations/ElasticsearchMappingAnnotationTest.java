package fr.tlrx.elasticsearch.test.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField.Index;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField.Store;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField.Types;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test class for {@link ElasticsearchMapping} and {@link ElasticsearchMappingField} annotations.
 * 
 * @author tlrx
 * 
 */
@RunWith(ElasticsearchRunner.class)
public class ElasticsearchMappingAnnotationTest {
	
	@ElasticsearchAdminClient
	AdminClient adminClient;
	
    @Test
	@ElasticsearchIndex(indexName = "library",
			mappings = { 
				@ElasticsearchMapping(typeName = "book", 
						source = false,
						compress = false,
						properties = { 
							@ElasticsearchMappingField(name = "title", store = Store.Yes, type = Types.String),
							@ElasticsearchMappingField(name = "author", store = Store.No, type = Types.String, index = Index.Not_Analyzed),
							@ElasticsearchMappingField(name = "description", store = Store.Yes, type = Types.String, index = Index.Analyzed, analyzerName = "standard"),
							@ElasticsearchMappingField(name = "role", store = Store.No, type = Types.String, index = Index.Analyzed, indexAnalyzerName = "keyword", searchAnalyzerName = "standard")
						},
						propertiesMulti = {
				            @ElasticsearchMappingMultiField(name = "name",
				                                            fields = {
                            				                    @ElasticsearchMappingField(name = "name", type = Types.String, index = Index.Analyzed),
                            				                    @ElasticsearchMappingField(name = "untouched", type = Types.String, index = Index.Not_Analyzed)
				            })
				        }) 
			})
	public void testElasticsearchMapping(){
		
		// Checks if the index has been created
		IndicesExistsResponse existResponse = adminClient.indices()
				.prepareExists("library")
				.execute().actionGet();
		
		assertTrue("Index must exist", existResponse.exists());
		
		// Checks if mapping has been created
		ClusterStateResponse stateResponse = adminClient.cluster()
				.prepareState()
				.setFilterIndices("library").execute()
				.actionGet();
		
		IndexMetaData indexMetaData = stateResponse.getState().getMetaData().index("library");
		MappingMetaData mappingMetaData = indexMetaData.getMappings().get("book");
		assertNotNull("Mapping must exists", mappingMetaData);
		        
		try {
            Map<String, Object> def = mappingMetaData.sourceAsMap();

		    // Check _source
        	@SuppressWarnings("unchecked")
            Map<String, Object> source = (Map<String, Object>) def.get("_source");
            assertNotNull("_source must exists", source);
            assertEquals(Boolean.FALSE, source.get("compress"));
            assertEquals(Boolean.FALSE, source.get("enabled"));
            
		    // Check properties
        	@SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) def.get("properties");
            assertNotNull("properties must exists", properties);
            
            // Check title
        	@SuppressWarnings("unchecked")
            Map<String, Object> title = (Map<String, Object>) properties.get("title");
            assertEquals("string", title.get("type"));
            assertEquals("yes", title.get("store"));

            // Check author
        	@SuppressWarnings("unchecked")
            Map<String, Object> author = (Map<String, Object>) properties.get("author");
            assertEquals("not_analyzed", author.get("index"));
            assertEquals("string", author.get("type"));
            assertNull("Store = No must be null", author.get("store"));
            
            // Check description
        	@SuppressWarnings("unchecked")
            Map<String, Object> description = (Map<String, Object>) properties.get("description");
            assertNull("index = analyzed must be null", description.get("index"));
            assertEquals("string", description.get("type"));
            assertEquals("yes", description.get("store"));
            assertEquals("standard", description.get("analyzer"));            
            
            // Check role
        	@SuppressWarnings("unchecked")
            Map<String, Object> role = (Map<String, Object>) properties.get("role");
            assertNull("index = analyzed must be null", role.get("index"));
            assertEquals("string", role.get("type"));
            assertNull("Store = No must be null", role.get("store"));
            assertEquals("keyword", role.get("index_analyzer"));
            assertEquals("standard", role.get("search_analyzer"));
            
            // Check name
        	@SuppressWarnings("unchecked")
            Map<String, Object> name = (Map<String, Object>) properties.get("name");
        	assertEquals("multi_field", name.get("type"));
        	@SuppressWarnings("unchecked")
			Map<String, Object> fields = (Map<String, Object>) name.get("fields");
        	assertNotNull("fields must exists", fields);
        	@SuppressWarnings("unchecked")
			Map<String, Object> untouched = (Map<String, Object>) fields.get("untouched");
            assertEquals("string", untouched.get("type"));
            assertNull("Store = No must be null", untouched.get("store"));
            assertEquals("not_analyzed", untouched.get("index"));
            @SuppressWarnings("unchecked")
			Map<String, Object> nameName = (Map<String, Object>) fields.get("name");
            assertEquals("string", nameName.get("type"));
            
		} catch (IOException e) {
			fail("Exception when reading mapping metadata");
		}
	}
	
}