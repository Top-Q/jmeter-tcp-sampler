package il.co.topq.jmeter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

/**
 * 
 * @author Itai Agmon
 *
 */
public class TcpConnector {

	private final String host;
	private final int port;
	private final String delimiter;
	private StringBuilder response;

	/**
	 * The time out for each request before throwing exception
	 */
	private int readTimeout = 30000;

	/**
	 * The number of read iterations
	 */
	private int maxNumberOfIterations = 1024;

	/**
	 * The buffer to use to store each part of the response
	 */
	private int numOfBytesInBuffer = 1024;

	/**
	 * Holds all the responses until and include the requested response
	 */
	private List<String> responseList = new ArrayList<String>();

	public TcpConnector(String host, int port, String delimiter) {
		super();
		this.host = host;
		this.port = port;
		this.delimiter = delimiter;
	}

	/**
	 * 
	 * @param request
	 *            Request body
	 * @param responseSeeker
	 *            The logic to find the required response
	 * @return The found response
	 * @throws TcpConnectorException
	 *             If response was not found or if exception occurred while
	 *             sending request.
	 */
	public String executeRequest(String request, BiPredicate<Integer, String> responseSeeker)
			throws TcpConnectorException {
		boolean foundResponse = false;
		try (Socket clientSocket = new Socket(host, port);
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

			clientSocket.setSoTimeout(readTimeout);
			String requestString = request.replace("\r", "").replace("\n", "") + delimiter;
			outToServer.writeBytes(requestString);

			response = new StringBuilder();
			int numOfCharacters = 0;
			while (numOfCharacters >= 0 && maxNumberOfIterations-- > 0) {
				sleep(100);
				char[] buffer = new char[numOfBytesInBuffer];
				inFromServer.read(buffer);
				response.append(new String(buffer).replace("\r", "").replace("\n", ""));
				foundResponse = handleResponse(responseSeeker);
				if (foundResponse) {
					break;
				}

			}
		} catch (Exception e) {
			throw new TcpConnectorException(e.getMessage(), responseList);
		}
		if (!foundResponse) {
			throw new TcpConnectorException("Requested response was not found", responseList);
		}
		return responseList.get(responseList.size() - 1);
	}

	/**
	 * 
	 * @param responseSeeker
	 *            Logic that defines the requested response.
	 * @return true if the requested response was found using the responseSeeker
	 */
	private boolean handleResponse(BiPredicate<Integer, String> responseSeeker) {
		String temp = null;
		boolean first = false;
		boolean responseFound = false;
		try (Scanner scanner = new Scanner(response.toString())) {
			scanner.useDelimiter(Pattern.quote(delimiter));
			while (scanner.hasNext()) {
				first = !first;
				if (first) {
					temp = scanner.next().trim();
				} else {
					responseList.add(temp);
					if (responseSeeker.test(responseList.size() - 1, temp)) {
						responseFound = true;
						break;
					}

				}
			}
		}
		response = first ? new StringBuilder(temp) : new StringBuilder();
		return responseFound;

	}

	/**
	 * Helper method for sleeping without the annoying exception
	 * 
	 * @param milliseconds
	 */
	private static void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getMaxNumberOfIterations() {
		return maxNumberOfIterations;
	}

	public void setMaxNumberOfIterations(int maxNumberOfIterations) {
		this.maxNumberOfIterations = maxNumberOfIterations;
	}

	public int getNumOfBytesInBuffer() {
		return numOfBytesInBuffer;
	}

	public void setNumOfBytesInBuffer(int numOfBytesInBuffer) {
		this.numOfBytesInBuffer = numOfBytesInBuffer;
	}

	public List<String> getResponseList() {
		return responseList;
	}

}
