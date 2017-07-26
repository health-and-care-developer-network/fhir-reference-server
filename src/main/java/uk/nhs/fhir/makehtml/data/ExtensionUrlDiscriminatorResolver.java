package uk.nhs.fhir.makehtml.data;

import java.util.Set;

public interface ExtensionUrlDiscriminatorResolver {
	public Set<FhirURL> getExtensionUrlDiscriminators(FhirTreeNode node);
}
