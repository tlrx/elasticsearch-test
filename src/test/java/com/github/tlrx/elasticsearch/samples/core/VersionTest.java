/**
 *
 */
package com.github.tlrx.elasticsearch.samples.core;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchIndex;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test Java API / Core / Index : Versionning
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
@ElasticsearchNode
public class VersionTest {

    @ElasticsearchClient
    Client client;

    @Test
    @ElasticsearchIndex(indexName = "library")
    public void indexInternalVersion() throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();

        // Book #1
        builder.startObject()
                .field("title", "Les Miserables")
                .field("author", "Victor Hugo")
                .endObject();

        // Index book #1
        IndexResponse response = client.prepareIndex("library", "book", "1")
                .setSource(builder)
                .execute()
                .actionGet();

        assertEquals("First document version must be 1", 1, response.getVersion());

        // Modify book #1
        builder = JsonXContent.contentBuilder().startObject()
                .field("title", "Les Misérables")
                .field("author", "Victor Hugo")
                .endObject();


        // Update book #1
        response = client.prepareIndex("library", "book", "1")
                .setSource(builder)
                .execute()
                .actionGet();

        assertEquals("Updated version must be 2", 2, response.getVersion());

        // Try to update book #1 with a wrong version number
        try {
            response = client.prepareIndex("library", "book", "1")
                    .setSource(builder)
                    .setVersion(1)
                    .execute().actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals("Current version must be 2", 2, e.getCurrentVersion());
        }

        // Update book #1 with a right version number
        response = client.prepareIndex("library", "book", "1")
                .setSource(builder)
                .setVersion(2)
                .execute().actionGet();

        assertEquals("Updated version must be 3", 3, response.getVersion());

        // Try to update book #1 with a wrong version number
        try {
            response = client.prepareIndex("library", "book", "1")
                    .setSource(builder)
                    .setVersion(5)
                    .execute().actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals("Current version must be 3", 3, e.getCurrentVersion());
        }
    }


    @Test
    @ElasticsearchIndex(indexName = "library")
    public void indexExternalVersion() throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();

        // Book #2
        builder.startObject()
                .field("title", "Notre Dame de Paris")
                .field("author", "Victor Hugo")
                .endObject();

        long startVersion = System.currentTimeMillis();

        // Try to index book #2 with a custom version but no external
        try {
            client.prepareIndex("library", "book", "2")
                    .setSource(builder)
                    .setVersion(startVersion)
                    .execute()
                    .actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals("Current version must be -1", -1, e.getCurrentVersion());
        }

        // Index book #2 with a custom version  with external
        IndexResponse response = client.prepareIndex("library", "book", "2")
                .setSource(builder)
                .setVersion(startVersion)
                .setVersionType(VersionType.EXTERNAL)
                .execute()
                .actionGet();

        assertEquals("Document version must be incremented", startVersion, response.getVersion());

        // Modify book #2
        builder = JsonXContent.contentBuilder().startObject()
                .field("title", "Notre-Dame de Paris")
                .field("author", "Victor Hugo")
                .endObject();

        // Update book #2 without version control
        response = client.prepareIndex("library", "book", "2")
                .setSource(builder)
                .execute()
                .actionGet();

        assertEquals("Document updated must have an incremented version number", startVersion + 1, response.getVersion());

        // Try to index book #2 with lower version number
        try {
            response = client.prepareIndex("library", "book", "2")
                    .setSource(builder)
                    .setVersion(startVersion - 10)
                    .setVersionType(VersionType.EXTERNAL)
                    .execute()
                    .actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals(startVersion + 1, e.getCurrentVersion());
        }

        // Try to index book #2 with same version number
        try {
            response = client.prepareIndex("library", "book", "2")
                    .setSource(builder)
                    .setVersion(startVersion + 1)
                    .setVersionType(VersionType.EXTERNAL)
                    .execute()
                    .actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals(startVersion + 1, e.getCurrentVersion());
        }

        // Try to index book #2 with greater version number
        response = client.prepareIndex("library", "book", "2")
                .setSource(builder)
                .setVersion(startVersion + 10)
                .setVersionType(VersionType.EXTERNAL)
                .execute()
                .actionGet();
        assertEquals("Updated version must be " + startVersion + 10, startVersion + 10, response.getVersion());

        // Try to index book #2 with lower version number and no version_type = external
        try {
            response = client.prepareIndex("library", "book", "2")
                    .setSource(builder)
                    .setVersion(startVersion + 5)
                    .setVersionType(VersionType.EXTERNAL)
                    .execute()
                    .actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals(startVersion + 10, e.getCurrentVersion());
        }

        // Update book #2 with same version number but no version_type = external
        response = client.prepareIndex("library", "book", "2")
                .setSource(builder)
                .setVersion(startVersion + 10)
                .execute()
                .actionGet();

        assertEquals("Document updated must have an incremented version number", startVersion + 11, response.getVersion());

        // Try to index book #2 with greater version number and no version_type = external
        try {
            response = client.prepareIndex("library", "book", "2")
                    .setSource(builder)
                    .setVersion(startVersion + 30)
                    .execute()
                    .actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals(startVersion + 11, e.getCurrentVersion());
        }
    }
}
