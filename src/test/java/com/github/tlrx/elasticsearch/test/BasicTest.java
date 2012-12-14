package com.github.tlrx.elasticsearch.test;


import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.tlrx.elasticsearch.test.EsSetup.*;
import static org.junit.Assert.*;

public class BasicTest {

    EsSetup esSetup;

    @Before
    public void setUp() throws Exception {

        // Using a local node & client
        esSetup = new EsSetup();

        /* Using a remote client
        Client client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        esSetup = new EsSetup(client);
        */

        /* Using specific Settings
        esSetup = new EsSetup();
         */

        esSetup.execute(
                deleteAll(),

                createIndex("catalog-2009"),

                createIndex("catalog-2010")
                        .withSource("{\n" +
                                "    \"settings\" : {\n" +
                                "        \"number_of_shards\" : 2\n" +
                                "    },\n" +
                                "    \"mappings\" : {\n" +
                                "        \"type2010\" : {\n" +
                                "            \"_source\" : { \"enabled\" : false },\n" +
                                "            \"properties\" : {\n" +
                                "                \"field1\" : { \"type\" : \"string\", \"index\" : \"not_analyzed\" }\n" +
                                "            }\n" +
                                "        }\n" +
                                "    }\n" +
                                "}"),

                createIndex("catalog-2011")
                        .withSource(fromClassPath("com/github/tlrx/elasticsearch/test/indices/catalog-2011.json")),

                createIndex("catalog-2012")
                        .withSettings("com/github/tlrx/elasticsearch/test/settings/catalog.json"),

                createIndex("catalog-2013")
                        .withSettings(fromClassPath("com/github/tlrx/elasticsearch/test/settings/catalog.json"))
                        .withMapping("product", fromClassPath("com/github/tlrx/elasticsearch/test/mappings/product.json"))
                        .withMapping("customer", fromClassPath("com/github/tlrx/elasticsearch/test/mappings/customer.json"))
                        .withData(fromClassPath("com/github/tlrx/elasticsearch/test/data/products.json")),

                createTemplate("template-1")
                        .withSource(fromClassPath("com/github/tlrx/elasticsearch/test/templates/template-1.json")),

                createTemplate("template-2")
                        .withTemplate("test*")
                        .withSettings(fromClassPath("com/github/tlrx/elasticsearch/test/settings/catalog.json"))
                        .withMapping("customer", fromClassPath("com/github/tlrx/elasticsearch/test/mappings/customer.json"))
        );
    }

    @Test
    public void testIndices() {

        // test exists()
        assertTrue(esSetup.exists("catalog-2009"));
        assertTrue(esSetup.exists("catalog-2010"));
        assertTrue(esSetup.exists("catalog-2011"));
        assertTrue(esSetup.exists("catalog-2012"));
        assertTrue(esSetup.exists("catalog-2013"));

        // test exists(index, type, id)
        assertTrue(esSetup.exists("catalog-2013", "product", "1"));
        assertTrue(esSetup.exists("catalog-2013", "product", "5"));

        // test countAll()
        assertEquals(new Long(4), esSetup.countAll());

        // test index()
        esSetup.execute(index("catalog-2013", "product", "6").withSource("{ \"field1\" : \"value6\" }"));
        esSetup.execute(index("catalog-2013", "product", "7").withSource("{ \"field1\" : \"value7\" }"));
        assertEquals(new Long(6), esSetup.countAll());

        // test delete()
        esSetup.execute(delete("catalog-2013", "product", "6"));
        assertEquals(new Long(5), esSetup.countAll());


        // test count(index)
        assertEquals(esSetup.countAll(), esSetup.count("catalog-2013"));

        // test createIndex() and createTemplate()
        esSetup.execute(createIndex("toomuch"));
        esSetup.execute(createIndex("tests"));

        // test deleteIndex()
        esSetup.execute(deleteIndex("catalog-2009"));
        assertFalse(esSetup.exists("catalog-2009"));

        // test deleteIndices()
        esSetup.execute(deleteIndices("catalog-2010", "catalog-2011"));
        assertFalse(esSetup.exists("catalog-2010"));
        assertFalse(esSetup.exists("catalog-2011"));

        // test deleteAll()
        esSetup.execute(deleteAll());
        assertFalse(esSetup.exists("catalog-2012"));
        assertFalse(esSetup.exists("catalog-2013"));
    }

    @After
    public void tearDown() throws Exception {
        esSetup.terminate();
    }
}
