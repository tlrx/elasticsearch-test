/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.tlrx.elasticsearch.test;

import com.github.tlrx.elasticsearch.test.provider.*;
import com.github.tlrx.elasticsearch.test.request.*;
import com.google.common.base.Preconditions;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

/**
 * This class aims to simplify the ElasticSearch setup for unit testing. It allows to start and stop an embedded local
 * node (or it can connect to a remote cluster), and then execute a set of requests to setup ElasticSearch.
 * <br/>
 * This sample code shows how to use EsSetup for a local node, and how to execute requests against this node:
 * <pre>
 *     public class MyTest {
 *
 *         EsSetup esSetup;
 *
 *         @Before
 *         public void setUp() throws Exception {
 *
 *              // Local node and client
 *              esSetup = new EsSetup();
 *
 *              // Clean all, and creates some indices
 *              esSetup.execute(
 *                              deleteAll(),
 *
 *                              createIndex("my_index_1"),
 *
 *                              createIndex("my_index_2")
 *                                  .withSettings(fromClassPath("path/to/settings.json"))
 *                                  .withMapping("type1", fromClassPath("path/to/mapping/of/type1.json"))
 *                                  .withData(fromClassPath("path/to/bulk.json"))
 *              );
 *         }
 *
 *         @After
 *         public void tearDown() throws Exception {
 *              esSetup.terminate();
 *        }
 *     }
 * </pre>
 */
public class EsSetup {

    private final ClientProvider provider;

    /**
     * This constructor uses the {@link ClientProvider} to retrieve an instance of {@link Client} and use it to execute requests.
     *
     * @param clientProvider the {@link ClientProvider} to use for requests execution
     */
    protected EsSetup(ClientProvider clientProvider) {
        Preconditions.checkNotNull(clientProvider, "No ClientProvider specified");
        provider = clientProvider;
    }

    /**
     * This constructor instantiates a local {@link org.elasticsearch.node.Node}.
     */
    public EsSetup() {
        this(new LocalClientProvider());
    }

    /**
     * This constructor instantiates a local {@link org.elasticsearch.node.Node} with specific {@link Settings}.
     */
    public EsSetup(Settings settings) {
        this(new LocalClientProvider(settings));
    }

    /**
     * This constructor allows to use a custom client (usually a TransportClient) to execute requests.
     * The client can be injected with a Dependency Injection framework or manually instantiated with Elasticsearch API:
     * <pre>
     * Client client = new TransportClient()
     *                         .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
     * EsSetup esSetup = new EsSetup(client);
     * </pre>
     *
     * @param client the client to use
     */
    public EsSetup(Client client) {
        this(new DefaultClientProvider(client, false));
    }

    /**
     * Similar to previous constructor, but it will close the client when terminate() is called.
     *
     * @param client                 the client to use
     * @param closeClientOnTerminate if true, the client will be closed when terminate() method is called.
     */
    public EsSetup(Client client, boolean closeClientOnTerminate) {
        this(new DefaultClientProvider(client, closeClientOnTerminate));
    }

    /**
     * @return the provided {@link Client}
     */
    public Client client() {
        return provider.client();
    }

    /**
     * Executes one or more requests. The requests will use the provided client. Execution will stop if a request fails.
     *
     * @param requests an array of requests to execute
     * @return the current EsSetup instance
     */
    public EsSetup execute(Request... requests) {
        provider.open();
        for (Request request : requests) {
            doExecute(request);
        }
        return this;
    }

    /**
     * Executes a request
     *
     * @param request the request to execute
     * @param <T>     the type of the request execution's result
     * @return the result of the execution (can be {@link Void}
     */
    private <T> T doExecute(Request request) {
        Preconditions.checkNotNull(request, "Request must not be null");
        provider.open();
        try {
            return (T) request.execute(provider.client());
        } catch (EsSetupRuntimeException e) {
            throw new EsSetupRuntimeException("Exception when executing request " + request, e);
        }
    }

    /**
     * Terminates and closes the node and client.
     */
    public void terminate() {
        provider.close();
    }

    /**
     * Util method to load a JSON file and get its content as a String.
     *
     * @param path the path of the file to load
     * @return a {@link JSONProvider} used to get file's content as JSON
     */
    public static JSONProvider fromClassPath(String path) {
        return new ClassPathJSONProvider(Thread.currentThread().getContextClassLoader(), path);
    }

    /**
     * Util method to load a JSON file and get its content as a String. This method uses {@link ClassLoader#getResourceAsStream(String)} to load the resource.
     *
     * @param classLoader  the {@link ClassLoader} used to load the resource.
     * @param resourceName the name of the resource/file to load
     * @return
     */
    public static JSONProvider fromClassPath(ClassLoader classLoader, String resourceName) {
        return new ClassPathJSONProvider(classLoader, resourceName);
    }

    /**
     * Util method to load a JSON file and get its content as a String. This method uses {@link Class#getResourceAsStream(String)} to load the resource.
     *
     * @param klass        the {@link Class} used to load the resource.
     * @param resourceName the name of the resource/file to load
     * @return
     */
    public static JSONProvider fromClassPath(Class klass, String resourceName) {
        return new ClassPathJSONProvider(klass, resourceName);
    }

    /**
     * Instantiates a request that can be used to create an index.
     * Here's a sample of how create an index:
     * <pre>
     *     import static com.github.tlrx.elasticsearch.test.EsSetup.createIndex;
     *     ...
     *     createIndex("my_index")
     *          .withSource(...)
     * </pre>
     *
     * @param index the index name
     * @return a {@link CreateIndex} request
     */
    public static CreateIndex createIndex(String index) {
        return new CreateIndex(index);
    }

    /**
     * Instantiates a request that can be used to index a document.
     *
     * @param index the index name
     * @param type  the document type
     * @return a {@link Index} request
     */
    public static Index index(String index, String type) {
        return new Index(index, type);
    }

    /**
     * Instantiates a request that can be used to index a document.
     *
     * @param index the index name
     * @param type  the document type
     * @param id    the document id
     * @return a {@link Index} request
     */
    public static Index index(String index, String type, String id) {
        return new Index(index, type, id);
    }

    /**
     * Instantiates a request that can be used to delete a document.
     *
     * @param index the index name
     * @param type  the document type
     * @param id    the document id
     * @return a {@link Delete} request
     */
    public static Delete delete(String index, String type, String id) {
        return new Delete(index, type, id);
    }

    /**
     * Instantiates a request that can be used to delete an index
     *
     * @param index the index name
     * @return a {@link DeleteIndices} request
     */
    public static DeleteIndices deleteIndex(String index) {
        return new DeleteIndices(index);
    }

    /**
     * Instantiates a request that can be used to delete given indices
     *
     * @param indices the indices names
     * @return a {@link DeleteIndices} request
     */
    public static DeleteIndices deleteIndices(String... indices) {
        return new DeleteIndices(indices);
    }

    /**
     * Instantiates a request that can be used to delete given templates
     *
     * @param templates the templates names
     * @return a {@link DeleteTemplates} request
     */
    public static DeleteTemplates deleteTemplates(String... templates) {
        return new DeleteTemplates(templates);
    }

    /**
     * Instantiates a request that can be used to delete all indices/documents
     *
     * @return a {@link DeleteIndices} request
     */
    public static DeleteIndices deleteAll() {
        return new DeleteIndices("_all");
    }

    /**
     * Instantiates a request that can be used to create a template
     *
     * @param name the template name
     * @return a {@link CreateTemplate} request
     */
    public static CreateTemplate createTemplate(String name) {
        return new CreateTemplate(name);
    }

    /**
     * Used to check if the index exists or not.
     *
     * @param index the index name
     * @return true if the index exists, false otherwise
     */
    public Boolean exists(String index) {
        return doExecute(new Exists(index));
    }

    /**
     * Used to check if the document exists or not.
     *
     * @param index the index of the document
     * @param type  the type of the document
     * @param id    the id of the document
     * @return true if the document exists, false otherwise
     */
    public Boolean exists(String index, String type, String id) {
        return doExecute(new Exists(index, type, id));
    }

    /**
     * Counts all the documents in all indices
     *
     * @return the total number of documents
     */
    public Long countAll() {
        return doExecute(new Count("_all"));
    }

    /**
     * Counts all the documents in given indices
     *
     * @param indices an array of indices names
     * @return the total number of documents
     */
    public Long count(String... indices) {
        return doExecute(new Count(indices));
    }

}
