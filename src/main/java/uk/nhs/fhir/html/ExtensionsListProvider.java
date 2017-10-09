package uk.nhs.fhir.html;

import java.util.List;

import uk.nhs.fhir.datalayer.collections.ResourceEntity;

public interface ExtensionsListProvider {

	List<ResourceEntity> getExtensions();

}