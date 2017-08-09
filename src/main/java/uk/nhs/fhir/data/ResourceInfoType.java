package uk.nhs.fhir.data;

/**
 * Type of information about an element, such as binding, constraint, example value etc. 
 * @author jon
 *
 */
public enum ResourceInfoType {
	CONSTRAINT,
	BINDING,
	EXAMPLE_VALUE,
	SLICING,
	SLICING_DISCRIMINATOR,
	FIXED_VALUE,
	DEFAULT_VALUE,
	PROFILE,
	EXTENSION_URL;
}
