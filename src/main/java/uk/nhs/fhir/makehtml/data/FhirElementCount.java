package uk.nhs.fhir.makehtml.data;

/**
 * Created by kevinmayfield on 09/05/2017.
 */
/*public enum FhirElementCount {
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

}*/
