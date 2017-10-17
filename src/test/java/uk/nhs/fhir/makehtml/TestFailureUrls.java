package uk.nhs.fhir.makehtml;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.url.UrlValidator;

public class TestFailureUrls {
	private static final List<String> urls = Lists.newArrayList(
		"http://hl7.org/fhir/DSTU2/valueset-v3-FamilyMember.html",
		"http://hl7.org/fhir/DSTU2/valueset-v3-ActSubstanceAdminSubstitutionCode.html",
		"http://hl7.org/fhir/DSTU2/valueset-v3-ServiceDeliveryLocationRoleType.html",
		"http://hl7.org/fhir/DSTU2/valueset-v3-SubstanceAdminSubstitutionReason.html");

	@Test
	public void testUrls() {
		new UrlValidator().testUrls(urls);
	}
}
