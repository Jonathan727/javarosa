package org.javarosa.services.transport.impl.simplehttp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.util.StreamsUtil;

/**
 * The SimpleHttpTransporter is able to send SimpleHttpTransportMessages (text over POST)
 * 
 */
public class SimpleHttpTransporter implements Transporter {

	/**
	 * The message to be sent by this Transporter
	 */
	private SimpleHttpTransportMessage message;

	/**
	 * @param message The message to be sent
	 */
	public SimpleHttpTransporter(SimpleHttpTransportMessage message) {
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.Transporter#getMessage()
	 */
	public TransportMessage getMessage() {
		return this.message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.Transporter#send()
	 */
	public TransportMessage send() {
		HttpConnection conn = null;
		try {

			conn = getConnection(message.getDestinationURL());

			if (conn.getResponseCode() == HttpConnection.HTTP_OK) {

				writeToConnection(conn, (byte[]) message.getContent());

				readResponse(conn, message);
			}

			conn.close();
		} catch (Exception e) {
			System.out.println("Connection failed: ");
			message.setFailureReason(e.getMessage());
			message.incrementFailureCount();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (IOException e) {
					// do nothing
				}
		}

		return message;

	}

	/**
	 * 
	 * 
	 * Write the byte array to the HttpConnection
	 * 
	 * @param conn
	 * @param bytes
	 * @throws IOException
	 */
	private void writeToConnection(HttpConnection conn, byte[] bytes)
			throws Exception {
		OutputStream out = null;
		try {
			// earlier code was commented: Problem exists here on 3110c CommCare
			// Application: open hangs
			out = conn.openOutputStream();
			System.out.println("writing: " + new String(bytes));
			StreamsUtil.writeToOutput(bytes, out);

		} catch (Exception e) {
			e.printStackTrace();
			throw e;

		} finally {
			if (out != null) {
				out.close();
			}

		}

	}

	/**
	 * 
	 * Read the response from the HttpConnection and record
	 * in the SimpleHttpTransportMessage
	 * 
	 * 
	 * @param conn
	 * @param result
	 * @return
	 * @throws IOException
	 * @throws ClassCastException
	 */
	private SimpleHttpTransportMessage readResponse(HttpConnection conn,
			SimpleHttpTransportMessage message) throws IOException {

		int responseCode = conn.getResponseCode();
		if (responseCode == HttpConnection.HTTP_OK) {
			message.setResponseBody(readResponseBody(conn));
			message.setStatus(TransportMessageStatus.SENT);
		} else {
			message.setResponseCode(responseCode);
		}

		return message;

	}

	/**
	 * 
	 * 
	 * 
	 * @param conn
	 * @return
	 * @throws IOException
	 */
	private String readResponseBody(HttpConnection conn) throws IOException {
		InputStream in = null;
		String r = null;
		try {
			in = conn.openInputStream();
			int len = (int) conn.getLength();
			byte[] response = StreamsUtil.readFromStream(in, len);
			r = new String(response);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return r;
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private HttpConnection getConnection(String url) throws IOException {
		HttpConnection conn;
		Object o = Connector.open(url);
		if (o instanceof HttpConnection) {
			conn = (HttpConnection) o;
			conn.setRequestMethod(HttpConnection.POST);
			conn.setRequestProperty("User-Agent",
					"Profile/MIDP-2.0 Configuration/CLDC-1.1");
			conn.setRequestProperty("Content-Language", "en-US");
			conn.setRequestProperty("MIME-version", "1.0");
			conn.setRequestProperty("Content-Type", "text/plain");
		} else {
			throw new IllegalArgumentException("Not HTTP URL:" + url);
		}
		return conn;

	}

}
