package il.co.topq.jmeter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TcpConnector {

	private final String host;
	private final int port;
	private final String delimiter;

	private int readTimeout = 30000;
	private int maxNumberOfIterations = 1024;
	private int numOfBytesInBuffer = 1024;
	private List<String> responseList = new ArrayList<String>();

	public TcpConnector(String host, int port, String delimiter) {
		super();
		this.host = host;
		this.port = port;
		this.delimiter = delimiter;
	}

	public List<String> executeRequest(String request, ResponseSeeker responseSeeker) throws Exception {
		try (Socket clientSocket = new Socket(host, port);
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

			clientSocket.setSoTimeout(readTimeout);
			String requestString = request.replace("\r", "").replace("\n", "") + delimiter;
			outToServer.writeBytes(requestString);

			StringBuilder response = new StringBuilder();
			int numOfCharacters = 0;
			while (numOfCharacters >= 0 && maxNumberOfIterations-- > 0) {
				sleep(100);
				char[] buffer = new char[numOfBytesInBuffer];
				inFromServer.read(buffer);
				response.append(new String(buffer).replace("\r", "").replace("\n", ""));
				response = handleResponse(response);
				if (responseSeeker.isResponseFound(responseList)) {
					break;
				}

			}
		}
		return responseList;
	}

	private StringBuilder handleResponse(StringBuilder response) {
		String temp = null;
		boolean first = false;
		try (Scanner scanner = new Scanner(response.toString())) {
			scanner.useDelimiter(Pattern.quote(delimiter));
			while (scanner.hasNext()) {
				first = !first;
				if (first) {
					temp = scanner.next().trim();

				} else {
					responseList.add(temp);
				}
			}

		}
		return first ? new StringBuilder(temp) : new StringBuilder();

	}

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
	
	

}
