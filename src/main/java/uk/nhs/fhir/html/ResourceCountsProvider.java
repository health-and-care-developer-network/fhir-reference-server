package uk.nhs.fhir.html;

import java.util.HashMap;

public interface ResourceCountsProvider {

	HashMap<String, Integer> getResourceTypeCounts();

}