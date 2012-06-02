/**
 * 
 */
package fr.tlrx.elasticsearch.samples.indices.analyze;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse.AnalyzeToken;
import org.elasticsearch.client.AdminClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.tlrx.elasticsearch.test.annotations.ElasticsearchAdminClient;
import fr.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;

/**
 * Test Java API / Indices / Analyze
 * 
 * @author tlrx
 * 
 */
 @RunWith(ElasticsearchRunner.class)
public class AnalyzeTest {

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

		assertEquals(5, response.tokens().size());
		printTokens(response.tokens());
		
		// Analyze another string
		response = adminClient.indices()
				.prepareAnalyze("But I also own foo.bar@domain.com, bar.foo@domain.com and mys web site is http://www.elasticsearch.org/is-so-great?truth=true feel free to visit it")
				.setTokenizer("uax_url_email")
				.execute()
				.actionGet();

		assertEquals(17, response.tokens().size());
		printTokens(response.tokens());		
	}
	
	/**
	 * Prints a token list on standard out
	 * @param tokens
	 */
	private void printTokens(List<AnalyzeToken> tokens) {
		if(tokens != null){
			System.out.printf("Printing %d tokens:\r\n", tokens.size());
			for (AnalyzeToken token : tokens) {
				System.out
						.printf("\tToken %d term=[%s], type=[%s], startOffset=[%d], endOffset=[%d]\r\n",
								token.position(), token.term(), token.type(),
								token.startOffset(), token.endOffset());
			}
		}		
	}
}
