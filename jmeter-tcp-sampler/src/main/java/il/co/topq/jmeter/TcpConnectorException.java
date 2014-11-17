package il.co.topq.jmeter;

import java.util.List;

/**
 * 
 * @author Itai Agmon
 * 
 *         Exception that is thrown by the TcpConnector. It may hold the list of
 *         all the received responses
 *
 */
public class TcpConnectorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final List<String> responses;

	public TcpConnectorException(List<String> responses) {
		super();
		this.responses = responses;
	}

	public TcpConnectorException(String message, List<String> responses) {
		super(message);
		this.responses = responses;
	}

	protected List<String> getResponses() {
		return responses;
	}

}
