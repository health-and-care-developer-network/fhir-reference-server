package uk.nhs.fhir.data.structdef;

import com.google.common.base.Preconditions;

public class FhirCardinality {
	private final FhirElementCount min;
	private final FhirElementCount max;

	public FhirCardinality(Integer minRaw, String maxRaw) {
		this(Integer.toString(minRaw), maxRaw);
	}
	
	public FhirCardinality(String minRaw, String maxRaw) {
		this.min = FhirElementCount.fromString(Preconditions.checkNotNull(minRaw));
		this.max = FhirElementCount.fromString(Preconditions.checkNotNull(maxRaw));
		
		Preconditions.checkArgument(FhirElementCount.validMinMaxPair(min, max), "Invalid min and max pair [" + minRaw + ".." + maxRaw + "]");
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
