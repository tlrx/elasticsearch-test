package fr.tlrx.elasticsearch.test.support.junit.handlers.annotations;

import java.util.Map;

import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

/**
 * Abstract annotation Handler
 * 
 * @author tlrx
 *
 */
public class AbstractAnnotationHandler {


	/**
	 * Creates an {@link Client} given a node's name and the current
	 * execution context
	 * 
	 * @param context
	 * @param nodeName
	 * @return
	 * @throws Exception
	 */
	protected Client client(Map<String, Object> context, String nodeName) throws Exception {
		// Get a node
		Node node = (Node) context.get(nodeName);
		if (node == null) {
			if (context.size() == 1) {
				node = (Node) context.values().iterator().next();
			} else {
				throw new Exception("Unable to manage index: nodeName must be defined.");
			}
		}
		return node.client();
	}

	/**
	 * Creates an {@link AdminClient} given a node's name and the current
	 * execution context
	 * 
	 * @param context
	 * @param nodeName
	 * @return
	 * @throws Exception
	 */
	protected AdminClient admin(Map<String, Object> context, String nodeName) throws Exception {
		return client(context, nodeName).admin();
	}
}
