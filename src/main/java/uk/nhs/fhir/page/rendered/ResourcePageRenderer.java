package uk.nhs.fhir.page.rendered;

import java.util.HashMap;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.SupportingArtefact;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.resourcehandlers.ResourceHelperFactory;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.FhirVersion;

public class ResourcePageRenderer {
    
	private static final Logger LOG = LoggerFactory.getLogger(ResourcePageRenderer.class.getName());
	
	private final FhirVersion fhirVersion;
	private final ResourceWebHandler resourceWebHandler;
	
	public ResourcePageRenderer(FhirVersion fhirVersion, ResourceWebHandler resourceWebHandler) {
		this.fhirVersion = fhirVersion;
		this.resourceWebHandler = resourceWebHandler;
	}
    
	/**
     * Code used to display a single resource as HTML when requested by a
     * browser.
     */
    public String renderSingleResource(String baseURL, IIdType resourceID, String resourceName, ResourceType resourceType) {
    	IBaseResource resource = resourceWebHandler.getResourceByID(resourceID);

    	// List of versions
    	ResourceEntityWithMultipleVersions entity = resourceWebHandler.getVersionsForID(resourceID);
    	HashMap<VersionNumber, ResourceMetadata> versionsList = entity.getVersionList();

    	// Resource metadata
    	ResourceMetadata resourceMetadata = resourceWebHandler.getResourceEntityByID(resourceID);

    	// Check if we have a nice metadata table from the renderer
    	Optional<SupportingArtefact> metadataArtefact = 
			resourceMetadata.getArtefacts()
				.stream()
				.filter(artefact -> artefact.getArtefactType().isMetadata())
				.findAny();
    	LOG.debug("Has metadata from renderer: " + metadataArtefact.isPresent());

    	// Tree view
    	String textSection = ResourceHelperFactory.getResourceHelper(fhirVersion, resourceType).getTextSection(resource);

    	// Examples
    	ExampleResources examplesList = resourceWebHandler.getExamples(resourceType + "/" + resourceID.getIdPart());
    	Optional<ExampleResources> examples = 
    		(examplesList == null 
    		  || examplesList.isEmpty()) ? 
    			Optional.empty() : 
    			Optional.of(examplesList);
    	
    	String firstTabName = getFirstTabName(resourceType);
    	
    	return new ResourceWithMetadataTemplate(resourceType.toString(), resourceName, baseURL, resource, firstTabName,
    		versionsList, resourceMetadata, metadataArtefact, textSection, examples).getHtml();
    }

	private String getFirstTabName(ResourceType resourceType) {
        switch (resourceType) {
	        case STRUCTUREDEFINITION:
				return "Snapshot";
	        case VALUESET: 
	        	return "Entries";
	        case OPERATIONDEFINITION:
	        	return "Operation Description";
	        case IMPLEMENTATIONGUIDE:
	        	return "Description";
	        case CODESYSTEM:
	        	return "Description";
	        case CONCEPTMAP:
	        	return "Description";
        	default:
	        	throw new IllegalStateException("Unhandled resource type: " + resourceType.toString());
        }
	}

}
