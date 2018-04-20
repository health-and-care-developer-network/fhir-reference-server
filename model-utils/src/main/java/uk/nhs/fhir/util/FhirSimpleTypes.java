package uk.nhs.fhir.util;

import java.util.Set;

import com.google.common.collect.Sets;

public class FhirSimpleTypes {
	public static final Set<String> CHOICE_SUFFIXES = Sets.newHashSet(
		"Boolean", "Integer", "Decimal", "base64Binary", "Instant", 
		"String", "Uri", "Date", "dateTime", "Time", "Code", "Oid", "Id", "unsignedInt", "positiveInt",
		"Markdown", "Annotation", "Attachment", "Identifier", "CodeableConcept", "Coding", "Quantity",
		"Range", "Period", "Ratio", "SampledData", "Signature", "HumanName", "Address", "ContactPoint",
		"Timing", "Reference", "Meta");
	
	public static final Set<String> URL_CORRECTION_TYPES = Sets.newHashSet(// from Forge, via Jen
			"Address", "Age", "Annotation", "Attachment", "CodeableConcept", "Coding", "ContactPoint",
			"Count", "Distance", "DomainResource", "Dosage", "Duration", "HumanName", "Identifier", "Meta", "Money",
			"Narrative", "Period", "Quantity", "Range", "Ratio", "Reference", "Resource", "SampledData",
			"Signature", "SimpleQuantity", "Timing");
	
	public static final Set<String> METADATA_URL_CORRECTION_TYPES = Sets.newHashSet(
			"ContactDetail", "Contributor", "DataRequirement", "ParameterDefinition", "RelatedArtifact", "TriggerDefinition", "UsageContext");
}
