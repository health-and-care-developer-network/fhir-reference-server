package uk.nhs.fhir.makehtml.data;

import com.google.common.base.Preconditions;

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

enum FhirElementCount {
	NONE("0"),
	ONE("1"),
	MANY("*");
	
	private final String displayString;
	
	FhirElementCount(String displayString) {
		this.displayString = displayString;
	}
	
	public String getDisplayString() {
		return displayString;
	}
	
	public static FhirElementCount fromString(String cardinalityString){
		for (FhirElementCount count : FhirElementCount.values()) {
			if (cardinalityString.equals(count.displayString)) {
				return count;
			}
		}
		throw new IllegalArgumentException("Unrecognised cardinality range [" + cardinalityString + "]");
	}
	
	public static boolean validMinMaxPair(FhirElementCount min, FhirElementCount max) {
		switch (max) {
			case NONE: 
				return min.equals(NONE);
			case ONE: 
			case MANY: 
				return min.equals(NONE) || min.equals(ONE);
			default: 
				throw new IllegalArgumentException("failed to match max value [" + max.name() + "]");
		}
	}
	
	
}