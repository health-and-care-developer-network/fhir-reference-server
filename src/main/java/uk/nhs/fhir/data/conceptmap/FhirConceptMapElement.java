package uk.nhs.fhir.data.conceptmap;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class FhirConceptMapElement {

	private final String code;
	private final List<FhirConceptMapElementTarget> targets = Lists.newArrayList();
	
	public FhirConceptMapElement(String code) {
		Preconditions.checkNotNull(code);
		
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
	public void addTarget(FhirConceptMapElementTarget target) {
		targets.add(target);
	}
	
	public List<FhirConceptMapElementTarget> getTargets() {
		return targets;
	}
}
