package uk.nhs.fhir.page.rendered;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.velocity.VelocityContext;
import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.SupportingArtefact;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.page.VelocityTemplate;
import uk.nhs.fhir.util.FhirVersion;

public class ResourceWithMetadataTemplate extends VelocityTemplate {

	private final IBaseResource resource;
	private final String firstTabName;
	private final Map<VersionNumber, ResourceMetadata> versionsList;
	private final ResourceMetadata resourceMetadata;
	private final Optional<SupportingArtefact> metadataArtefact;
	private final String textSection;
	private final Optional<List<ResourceMetadata>> examples;
	private final String baseURL;
	private final FhirVersion fhirVersion;

	public ResourceWithMetadataTemplate(ResourceType resourceType, String resourceName, String baseURL, IBaseResource resource,
			String firstTabName, Map<VersionNumber, ResourceMetadata> versionsList, ResourceMetadata resourceMetadata,
			Optional<SupportingArtefact> metadataArtefact, String textSection, Optional<List<ResourceMetadata>> examples,
			FhirVersion fhirVersion) {
		super("resource-with-metadata.vm", Optional.of(resourceType), Optional.of(resourceName));
		this.resource = resource;
		this.firstTabName = firstTabName;
		this.versionsList = versionsList;
		this.resourceMetadata = resourceMetadata;
		this.metadataArtefact = metadataArtefact;
		this.textSection = textSection;
		this.examples = examples;
		this.baseURL = baseURL;
		this.fhirVersion = fhirVersion;
	}

	@Override
	protected void updateContext(VelocityContext context) {
		context.put( "resource", resource );
    	context.put( "firstTabName", firstTabName );
    	context.put( "versions", versionsList );
    	
    	context.put( "metadata", resourceMetadata );
    	context.put( "baseURL", baseURL);
    	context.put( "generatedurl", resourceMetadata.getVersionedUrl(baseURL) );
    	context.put( "fhirVersion", fhirVersion);
    	
    	context.put( "hasGeneratedMetadataFromRenderer", metadataArtefact.isPresent() );
    	if (metadataArtefact.isPresent()) {
    		context.put( "metadataType", metadataArtefact.get().getArtefactType().name());
    	}
    	context.put( "treeView", textSection );
    	
    	if (examples.isPresent()) {
			context.put( "examples", examples.get() );
    	}
	}

}
