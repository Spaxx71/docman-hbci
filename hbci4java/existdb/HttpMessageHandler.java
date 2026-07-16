package hbci4java.existdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.io.Reader;
import org.exist.xquery.value.ValueSequence;
import org.exist.xquery.value.StringValue;

public class HttpMessageHandler extends MessageHandler {

    private static final String USER_AGENT = "HBCI/1.0";

    private String SendUrl = null;
	private String ReceiveUrl = null;
	private String AuthHeaderValue = null;

	public  HttpMessageHandler (String sendURL, String receiveURL, String authHeaderValue) {
		this.SendUrl  = sendURL;
		this.ReceiveUrl = receiveURL;
		this.AuthHeaderValue = authHeaderValue;
	}
    public static void main(String[] args) {

		String messageUrl = args[0];
		String responseUrl = args[1];
		String httpUser = args[2];
		String httpPass = args[3];

		System.out.println("message=" + messageUrl + " response=" + responseUrl + " httpUser=" + httpUser);

		String auth = httpUser + ":" + httpPass;
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
		String authHeaderValue = "Basic " + new String(encodedAuth);

		HttpMessageHandler MessageHandler = new HttpMessageHandler(messageUrl,responseUrl,authHeaderValue);
		
		System.out.println("Sending...");
		MessageHandler.sendMessage("<Test/>");

		System.out.println("Reading...");
		System.out.println("Message: " + MessageHandler.readMessage());

		System.out.println("Done");
    }

	public String readMessage(){
		StringBuffer response = null;

System.out.println("readMessage'" + this.ReceiveUrl + "'");;

		try {
			URL obj = new URL(this.ReceiveUrl);
			HttpsURLConnection urlConnection = (HttpsURLConnection) obj.openConnection();

			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("User-Agent", USER_AGENT);
			urlConnection.setRequestProperty("Authorization", AuthHeaderValue);
			urlConnection.setRequestProperty("Accept", "application/xml, */*");

			//HttpMessageHandler MessageHandler = new HttpMessageHandler(messageUrl,responseUrl,authHeaderValue);
			int responseCode = urlConnection.getResponseCode();
System.out.println("GET Response Code :: " + responseCode);
			response = new StringBuffer();

			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				Reader streamReader = null;
				if (responseCode > 299) {
					streamReader = new InputStreamReader(urlConnection.getErrorStream());
				} else {
					streamReader = new InputStreamReader(urlConnection.getInputStream());
				}
				BufferedReader in = new BufferedReader(streamReader);
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				} in.close();

				// print result
				System.out.println("OK: GET request from URL '" + this.ReceiveUrl + "' with '"+ response.toString() + "'");
			} else {
				System.err.println("ERROR: GET request from URL '" + this.ReceiveUrl + "' with Authorization header '" + AuthHeaderValue + "' not worked.");
			}

			for (int i = 1; i <= urlConnection.getHeaderFields().size(); i++) {
				if (urlConnection.getHeaderFieldKey(i) != null)
					System.out.println(urlConnection.getHeaderFieldKey(i) + " = " + urlConnection.getHeaderField(i));
			}

			urlConnection.disconnect();
		}
		catch (Exception e) {
			System.err.println("ERROR: Get request from URL exception" + e);
		}
		System.out.println("OK2: GET request from URL '" + this.ReceiveUrl + "' with '"+ response.toString() + "'");

		return response.toString();
	}


    public void sendMessage(String message) {

System.out.println("sendMessage '" + message + "' to URL '" + this.SendUrl + "'" );
		try {
			URL obj = new URL(this.SendUrl);
			HttpsURLConnection urlConnection = (HttpsURLConnection) obj.openConnection();

			urlConnection.setRequestMethod("PUT");
			urlConnection.setRequestProperty("User-Agent", USER_AGENT);
			urlConnection.setRequestProperty("Authorization", AuthHeaderValue);
			urlConnection.setRequestProperty("Accept", "application/xml, */*");
			urlConnection.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");

			// For POST only - START
			urlConnection.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
			out.write(message);
			out.close();
			// For POST only - END

			int responseCode = urlConnection.getResponseCode();
			System.out.println("PUT Response Code :: " + responseCode);

			if (responseCode == HttpsURLConnection.HTTP_OK) { // success
				Reader streamReader = null;
				if (responseCode > 299) {
					streamReader = new InputStreamReader(urlConnection.getErrorStream());
				} else {
					streamReader = new InputStreamReader(urlConnection.getInputStream());
				}

				BufferedReader in = new BufferedReader(streamReader);
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				} in.close();

				// print result
				System.out.println(response.toString());
			} else {
				System.err.println("PUT request to URL '" + this.SendUrl + "' with Authorization header '" + AuthHeaderValue + "' not worked.");
			}

			urlConnection.disconnect();
		}
		catch (Exception e) {
			System.err.println(e);
		}
    }
}