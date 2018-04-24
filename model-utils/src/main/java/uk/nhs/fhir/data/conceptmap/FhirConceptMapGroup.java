package uk.nhs.fhir.data.conceptmap;

import java.util.List;

import com.google.common.collect.Lists;

public class FhirConceptMapGroup {
	private final String fromCodeSystem;
	private final String toCodeSystem;
	private final List<FhirConceptMapElement> mappings = Lists.newArrayList();
	
	public FhirConceptMapGroup(String fromCodeSystem, String toCodeSystem) {
		this.fromCodeSystem = fromCodeSystem;
		this.toCodeSystem = toCodeSystem;
	}
	
	public void addMapping(String fromCode, FhirConceptMapElementTarget target) {
		FhirConceptMapElement mapping = null;
		
		for (FhirConceptMapElement existingMapping : mappings) {
			if (existingMapping.getCode().equals(fromCode)) {
				mapping = existingMapping;
				break;
			}
		}
		
		if (mapping == null) {
			mapping = new FhirConceptMapElement(fromCode);
			mappings.add(mapping);
		}
		
		mapping.addTarget(target);
	}
	
	public boolean matches(String fromCodeSystem, String toCodeSystem) {
		return this.toCodeSystem.equals(toCodeSystem)
		  && this.fromCodeSystem.equals(fromCodeSystem);
	}
	
	public String getFromCodeSystem() {
		return fromCodeSystem;
	}
	
	public String getToCodeSystem() {
		return toCodeSystem;
	}
	
	public List<FhirConceptMapElement> getMappings() {
		return mappings;
	}
}