package uk.nhs.fhir.makehtml.data;

import com.google.common.base.Preconditions;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;

public class FhirCardinality {
	private final FhirElementCount min;
	private final FhirElementCount max;

	public FhirCardinality(Integer minRaw, String maxRaw) {
		this(Integer.toString(minRaw), maxRaw);
	}
	
	public FhirCardinality(String minRaw, String maxRaw) {
		Preconditions.checkNotNull(minRaw);
		Preconditions.checkNotNull(maxRaw);
		
		this.min = FhirElementCount.fromString(minRaw);
		this.max = FhirElementCount.fromString(maxRaw);
		
		Preconditions.checkArgument(FhirElementCount.validMinMaxPair(min, max), "Invalid min and max pair [" + minRaw + ".." + maxRaw + "]");
	}
	
	public FhirCardinality(ElementDefinitionDt elementDefinition) {
		this(elementDefinition.getMin(), elementDefinition.getMax());
	}

	public FhirElementCount getMax() {
		return max;
	}
	
	public FhirElementCount getMin() {
		return min;
	}
	
	public String toString() {
		return min.getDisplayString() + ".." + max.getDisplayString();
	}
}

