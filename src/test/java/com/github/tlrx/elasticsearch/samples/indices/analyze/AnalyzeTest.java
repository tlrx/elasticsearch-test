/**
 *
 */
package com.github.tlrx.elasticsearch.samples.indices.analyze;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse.AnalyzeToken;
import org.elasticsearch.client.AdminClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Test Java API / Indices / Analyze
 *
 * @author tlrx
 */
@RunWith(ElasticsearchRunner.class)
@ElasticsearchNode
public class AnalyzeTest {

    private final static Logger LOGGER = Logger.getLogger(AnalyzeTest.class.getName());

    @ElasticsearchAdminClient
    AdminClient adminClient;

    @Test
    public void testUAXUrlEmailTokenizer() {

        // Analyze a string with uax_url_email tokenizer
        AnalyzeResponse response = adminClient.indices()
                .prepareAnalyze("My email address is foo@bar.com")
                .setTokenizer("uax_url_email")
                .execute()
                .actionGet();

        assertEquals(5, response.getTokens().size());
        printTokens(response.getTokens());

        // Analyze another string
        response = adminClient.indices()
                .prepareAnalyze("But I also own foo.bar@domain.com, bar.foo@domain.com and mys web site is http://www.elasticsearch.org/is-so-great?truth=true feel free to visit it")
                .setTokenizer("uax_url_email")
                .execute()
                .actionGet();

        assertEquals(17, response.getTokens().size());
        printTokens(response.getTokens());
    }

    /**
     * Prints a token list on standard out
     *
     * @param tokens
     */
    private void printTokens(List<AnalyzeToken> tokens) {
        if (tokens != null) {
            LOGGER.info(String.format("Printing %d tokens:\r\n", tokens.size()));
            for (AnalyzeToken token : tokens) {
                LOGGER.info(String.format("\tToken %d term=[%s], type=[%s], startOffset=[%d], endOffset=[%d]\r\n",
                        token.getPosition(), token.getTerm(), token.getType(),
                        token.getStartOffset(), token.getEndOffset()));
            }
        }
    }
}
