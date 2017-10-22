package uk.nhs.fhir.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.NewMain;

public class UrlValidator {

	private static final Logger LOG = LoggerFactory.getLogger(UrlValidator.class);
	
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
		if (!success.isEmpty()) {
			LOG.info("Successfully tested the following link URLs:\n" + String.join("\n", success.keySet()));
		}
		
		if (!silentFailure.isEmpty()) {
			LOG.warn("Displaying the following URLs as text rather than a link (received failure response):\n" + String.join("\n", silentFailure.keySet()));
		}
		
		if (!failure.isEmpty()) {
			LOG.error("WARNING - the following links are broken and included in output:\n" + String.join("\n", failure.keySet()));
		}
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
			LOG.warn("SSL Handshake Exception for " + linkUrl);
			silentFailure.put(linkUrl, -1);
			return false;
		} catch (IOException e2) {
			LOG.warn("Exception for " + linkUrl);
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
			// don't bother testing local addresses - would need to modify the URL to point at some up-to-date instance of the server
			return;
		}
		
		int statusCode;
		if (!NewMain.TEST_LINK_URLS) {
			statusCode = getRecordedResponseCode(linkUrl);
		} else {
			statusCode = sendTestRequest(client, linkUrl);
		}
		
		if (statusCode >= 200 && statusCode < 300) {
			success.put(linkUrl, statusCode);
		} else if (statusCode >= 300 && statusCode < 400) {
			success.put(linkUrl, statusCode);
		} else if (statusCode >= 400) {
			failureMap.put(linkUrl, statusCode);
		} else {
			failureMap.put(linkUrl, statusCode);
		}
	}

	private int sendTestRequest(CloseableHttpClient client, String linkUrl) throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(linkUrl);
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
	
		LOG.debug("Sending GET request to " + linkUrl);
		
		int statusCode;
		try (CloseableHttpResponse response = client.execute(request)) {
			statusCode = response.getStatusLine().getStatusCode();
		}
		LOG.debug("" + statusCode);
		return statusCode;
	}

	private static final Set<String> knownFailureUrls = Sets.newHashSet(
		"https://fhir.hl7.org.uk/CareConnect-ConditionClinicalStatus",
		"https://fhir.hl7.org.uk/CareConnect-MedicationFlag-1",
		"https://fhir.hl7.org.uk/CareConnect-RegistrationStatus-1",
		"https://fhir.hl7.org.uk/CareConnect-ConditionEpisodicity",
		"https://fhir.hl7.org.uk/CareConnect-NHSNumberVerificationStatus-1",
		"https://fhir.hl7.org.uk/CareConnect-PersonRelationshipType-1",
		"https://fhir.hl7.org.uk/CareConnect-HumanLanguage-1",
		"https://fhir.hl7.org.uk/CareConnect-TreatmentCategory-1",
		"https://fhir.hl7.org.uk/CareConnect-PersonStatedGender-DDMAP-1",
		"https://fhir.hl7.org.uk/CareConnect-LanguageAbilityMode-1",
		"https://fhir.hl7.org.uk/CareConnect-ConditionRelationship",
		"https://fhir.hl7.org.uk/CareConnect-ResidentialStatus-1",
		"https://fhir.hl7.org.uk/CareConnect-RegistrationType-1",
		"https://fhir.hl7.org.uk/CareConnect-ConditionCategory-1",
		"https://fhir.hl7.org.uk/CareConnect-EthnicCategory-1",
		"https://fhir.hl7.org.uk/CareConnect-MaritalStatus-DDMAP-1",
		"https://fhir.hl7.org.uk/CareConnect-LanguageAbilityProficiency-1",
		"https://fhir.hl7.org.uk/CareConnect-SDSJobRoleName-1",
		
		"http://snomed.info/sct",
		
		"https://fhir.nhs.uk/Id/local-organization-code",
		"https://fhir.nhs.uk/Id/ods-organization-code",
		"https://fhir.nhs.uk/Id/nhs-number",
		"https://fhir.nhs.uk/Id/local-practitioner-identifier",
		"https://fhir.nhs.uk/Id/sds-user-id",
		"https://fhir.nhs.uk/Id/local-patient-identifier",
		"https://fhir.nhs.uk/Id/sds-role-profile-id",
		"https://fhir.nhs.uk/Id/local-location-identifier",
		"https://fhir.nhs.uk/Id/ods-site-code"
	);
	
	private int getRecordedResponseCode(String linkUrl) {
		return knownFailureUrls.contains(linkUrl) ? 400 : 200;
	}
}
