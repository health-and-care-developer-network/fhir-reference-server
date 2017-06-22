package uk.nhs.fhir.makehtml;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UrlTester {

	private static Map<String, Integer> success = Maps.newConcurrentMap();
	private static Map<String, Integer> silentFailure = Maps.newConcurrentMap();
	private static Map<String, Integer> failure = Maps.newConcurrentMap();
	
	public void testUrls(Set<String> linkUrls) {
		testUrls(Lists.newArrayList(linkUrls));
	}
	
	public void testUrls(List<String> linkUrls) {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			
			for (String linkUrl : linkUrls) {
				try {
					testUrl(client, linkUrl, failure);
				} catch (IOException e) {
					e.printStackTrace();
					failure.put(linkUrl, -1);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void logSuccessAndFailures() {
		System.out.println("Success responses received from:\n" + String.join("\n", success.keySet()));
		System.out.println("Silent failure responses received from:\n" + String.join("\n", silentFailure.keySet()));
		System.out.println("Failure responses received from:\n" + String.join("\n", failure.keySet()));
	}

	public boolean testSingleUrl(String linkUrl) {
		if (success.containsKey(linkUrl)) {
			return true;
		} else if (silentFailure.containsKey(linkUrl)
		  || failure.containsKey(linkUrl)) {
			return false;
		}
		
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			testUrl(client, linkUrl, silentFailure);
		} catch (SSLHandshakeException e1) {
			System.out.println("SSL Handshake Exception for " + linkUrl);
			silentFailure.put(linkUrl, -1);
			return false;
		} catch (IOException e2) {
			System.out.println("Exception for " + linkUrl);
			e2.printStackTrace();
			silentFailure.put(linkUrl, -1);
			return false;
		}
		
		if (success.containsKey(linkUrl)) {
			return true;
		} else if (silentFailure.containsKey(linkUrl)) {
			return false;
		} else {
			throw new IllegalStateException("Didn't find URL in success or failure maps: " + linkUrl);
		}
	}
	
	private void testUrl(CloseableHttpClient client, String linkUrl, Map<String, Integer> failureMap) throws IOException {
		// fix up relative URLs before testing
		if (linkUrl.startsWith("/")) {
			// don't bother testing local addresses
			return;
		}
		
		System.out.println("Sending GET request to " + linkUrl);
		
		HttpGet request = new HttpGet(linkUrl);
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		
		try (CloseableHttpResponse response = client.execute(request)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode >= 200 && statusCode < 300) {
				success.put(linkUrl, statusCode);
			} else if (statusCode >= 300 && statusCode < 400) {
				response.getStatusLine().getReasonPhrase();
				System.out.println("" + statusCode);
				success.put(linkUrl, statusCode);
			} else if (statusCode >= 400) {
				failureMap.put(linkUrl, statusCode);
				System.out.println("" + statusCode);
			} else {
				failureMap.put(linkUrl, statusCode);
				System.out.println("" + statusCode);
			}
		}
	}
	
}
