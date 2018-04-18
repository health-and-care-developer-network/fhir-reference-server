package uk.nhs.fhir.page.home;

import java.util.HashMap;

public interface ResourceCountsProvider {

	HashMap<String, Integer> getResourceTypeCounts();

}