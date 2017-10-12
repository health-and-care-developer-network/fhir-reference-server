package uk.nhs.fhir.page.rendered;

import java.util.HashMap;
import java.util.Optional;

import org.apache.velocity.VelocityContext;
import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.page.VelocityTemplate;

public class ResourceWithMetadataTemplate extends VelocityTemplate {

	private final IBaseResource resource;
	private final String firstTabName;
	private final HashMap<VersionNumber, ResourceMetadata> versionsList;
	private final ResourceMetadata resourceMetadata;
	private final Optional<SupportingArtefact> metadataArtefact;
	private final String textSection;
	private final Optional<ExampleResources> examples;

	public ResourceWithMetadataTemplate(String resourceType, String resourceName, String baseURL, IBaseResource resource,
			String firstTabName, HashMap<VersionNumber, ResourceMetadata> versionsList, ResourceMetadata resourceMetadata,
			Optional<SupportingArtefact> metadataArtefact, String textSection, Optional<ExampleResources> examples) {
		super(Optional.of("resource-with-metadata.vm"), Optional.empty(), Optional.of(resourceType), Optional.of(resourceName), baseURL);
		this.resource = resource;
		this.firstTabName = firstTabName;
		this.versionsList = versionsList;
		this.resourceMetadata = resourceMetadata;
		this.metadataArtefact = metadataArtefact;
		this.textSection = textSection;
		this.examples = examples;
	}

	@Override
	protected void updateContext(VelocityContext context) {
		context.put( "resource", resource );
    	context.put( "firstTabName", firstTabName );
    	context.put( "versions", versionsList );
    	
    	context.put( "metadata", resourceMetadata );
    	context.put( "generatedurl", resourceMetadata.getVersionedUrl(baseURL) );
    	
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
