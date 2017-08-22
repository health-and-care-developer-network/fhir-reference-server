package uk.nhs.fhir.data.codesystem;

import java.util.ArrayList;
import java.util.List;

public class FhirCodeSystemConcepts {

	private final String system;
	private final List<FhirCodeSystemConcept> concepts = new ArrayList<>();

	public FhirCodeSystemConcepts(String system) {
		this.system = system;
	}
	
	public String getSystem() {
		return system;
	}

	public void addConcept(FhirCodeSystemConcept concept) {
		concepts.add(concept);
	}
	
	public List<FhirCodeSystemConcept> getConcepts() {
		return concepts ;
	}
	
}
