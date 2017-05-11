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

class FhirElementCount {
	public static final String MANY = "*";
	
	private final String displayString;
	
	FhirElementCount(String displayString) {
		this.displayString = displayString;
	}
	
	public boolean isMany() {
		return displayString.equals(MANY);
	}
	
	public String getDisplayString() {
		return displayString;
	}
	
	public static FhirElementCount fromString(String cardinalityString){
		// cardinality strings can only be integers or the special value * meaning many
		if (!cardinalityString.equals(MANY)) {
			Integer.parseInt(cardinalityString);
		}
		
		return new FhirElementCount(cardinalityString);
	}
	
	public static boolean validMinMaxPair(FhirElementCount min, FhirElementCount max) {
		if (min.isMany()) {
			return false;
		}
		
		if (max.isMany()) {
			return true;
		} 
		
		int maxValue = Integer.parseInt(max.displayString);
		int minValue = Integer.parseInt(min.displayString);
		return maxValue >= minValue;
	}
}
