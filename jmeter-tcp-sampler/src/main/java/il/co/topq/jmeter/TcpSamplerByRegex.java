package il.co.topq.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class TcpSamplerByRegex extends AbstractJavaSamplerClient {

	private static final String HOST = "Host";
	private static final String PORT = "Port";
	private static final String DELIMITER = "Delimiter";
	private static final String READ_TIME_OUT = "Read Timeout";
	private static final String REGEX = "Regular Expression";
	private static final String REQUEST = "Request";

	private String host = "qa02-gwapi01";
	private int port = 10300;
	private String delimiter = "/***/";
	private int readTimeout = 30000;
	private String request;
	private String regex;

	// set up default arguments for the JMeter GUI
	@Override
	public Arguments getDefaultParameters() {
		Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument(HOST, "qa02-gwapi01");
		defaultParameters.addArgument(PORT, "10300");
		defaultParameters.addArgument(DELIMITER, "/***/");
		defaultParameters.addArgument(READ_TIME_OUT, "30000");
		defaultParameters.addArgument(REGEX, "");
		defaultParameters.addArgument(REQUEST, "");
		return defaultParameters;
	}

	private void setParameters(JavaSamplerContext context) {
		host = context.getParameter(HOST);
		port = context.getIntParameter(PORT);
		delimiter = context.getParameter(DELIMITER);
		readTimeout = context.getIntParameter(READ_TIME_OUT);
		regex = context.getParameter(REGEX);
		request = context.getParameter(REQUEST);
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		final SampleResult result = new SampleResult();
		setParameters(context);
		final TcpConnector connector = new TcpConnector(host, port, delimiter);
		connector.setReadTimeout(readTimeout);
		result.sampleStart();
		try {
			final String foundResponse = connector.executeRequest(request,
					(Integer index, String response) -> response.matches("(.*)" + regex + "(.*)"));
			result.setSuccessful(true);
			result.setResponseCodeOK();
			result.setResponseData(foundResponse.getBytes());
			result.setResponseMessage("Found response that matches the given regex");
		} catch (TcpConnectorException e) {
			result.setResponseCode("500");
			result.setSuccessful(false);
			result.setResponseData(e.getResponses() != null ? e.getResponses().toString().getBytes()
					: "No data recieved".getBytes());
			result.setResponseMessage("Failed due to " + e.getMessage());
		}

		result.sampleEnd();
		return result;
	}

}
