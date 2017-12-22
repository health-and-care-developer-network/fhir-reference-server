package uk.nhs.fhir.util.text;

import java.util.Map;

import com.google.common.collect.Maps;

import uk.nhs.fhir.util.FhirVersion;

public class FhirTextSectionHelpers {
	private static final Map<FhirVersion, FhirTextSectionHelper> helpers = Maps.newConcurrentMap();
	static {
		helpers.put(FhirVersion.DSTU2, new Dstu2FhirTextSectionHelper());
		helpers.put(FhirVersion.STU3, new Stu3FhirTextSectionHelper());
	}
	
	public static FhirTextSectionHelper forVersion(FhirVersion version) {
		if (helpers.containsKey(version)) {
			return helpers.get(version);
		} else {
			throw new IllegalStateException("No text section helper for " + version.toString());
		}
	}
}
