package uk.nhs.fhir.makehtml.data;

import java.util.ArrayList;
import java.util.List;

public class FhirCodeSystem {

	private final String system;
	private final List<FhirCodeSystemConcept> concepts = new ArrayList<>();

	public FhirCodeSystem(String system) {
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
