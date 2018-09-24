package uk.nhs.fhir.page.namingsystem;

import java.util.List;

import uk.nhs.fhir.data.metadata.ResourceMetadata;

public interface NamingSystemListProvider {

	List<ResourceMetadata> getNamingSystem();
}