package uk.nhs.fhir.makehtml.data;

// See reference at top of http://hl7.org/fhir/stu3/datatypes.html
public enum FhirDataType {
	// Simple/Primitive types contain a single value
	SIMPLE_ELEMENT,
	PRIMITIVE,
	
	// Complex types - reusable clusters of elements
	COMPLEX_ELEMENT,
	
	// Complex data types for metadata
	METADATA,
	
	// Special purpose data types
	META,
	NARRATIVE,
	REFERENCE,
	DOSAGE,
	RESOURCE,
	EXTENSION,
	
	// Sneaky extra inclusions at the bottom of the datatypes page
	XHTML_NODE,
	
	// If we can't find an implementing class as defined by the lists that ship with HAPI (e.g. a user-defined type)
	UNKNOWN,
	
	// e.g. root of a profile
	DOMAIN_RESOURCE,
	
	// generic type
	ELEMENT, 
	
	// element has multiple types (name should end [x]) and if constrained to a particular type, name should be updated
	CHOICE,
	
	// type for dummy nodes that will inherit their type from their backup node
	DELEGATED_TYPE;
}
