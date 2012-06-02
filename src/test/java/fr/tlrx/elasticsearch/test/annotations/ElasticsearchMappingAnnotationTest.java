package fr.tlrx.elasticsearch.test.annotations;

import static org.junit.Assert.assertNotNull;
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

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMapping;
import fr.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField;
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
						properties = { 
							@ElasticsearchMappingField(name = "title", store = Store.Yes, type = Types.String),
							@ElasticsearchMappingField(name = "author", store = Store.Yes, type = Types.String)
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
			Object properties = def.get("properties");
			assertNotNull("properties must exists", properties);
		} catch (IOException e) {
			fail("Exception when reading mapping metadata");
		}
	}
	
}
