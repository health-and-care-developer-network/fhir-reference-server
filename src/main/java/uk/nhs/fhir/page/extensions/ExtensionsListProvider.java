package uk.nhs.fhir.page.extensions;

import java.util.List;

import uk.nhs.fhir.data.metadata.ResourceMetadata;

public interface ExtensionsListProvider {

	List<ResourceMetadata> getExtensions();

}