/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers.stu3;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.ImplementationGuide;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FhirVersion;

/**
 *
 * @author tim
 */
public class ImplementationGuideProvider extends AbstractResourceProviderSTU3 {

	public ImplementationGuideProvider(FilesystemIF dataSource) {
		super(dataSource, ResourceType.IMPLEMENTATIONGUIDE, org.hl7.fhir.dstu3.model.ImplementationGuide.class);
    }
    
    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	ImplementationGuide guide = (ImplementationGuide)FHIRUtils.loadResourceFromFile(FhirVersion.STU3, thisFile);
    	String resourceName = guide.getName();
    	String url = guide.getUrl();
    	String resourceID = getResourceIDFromURL(url, resourceName);
    	String displayGroup = "Implementation Guides";
        VersionNumber versionNo = new VersionNumber(guide.getVersion());
        String status = guide.getStatus().name();
    	
        return new ResourceMetadata(resourceName, thisFile, ResourceType.IMPLEMENTATIONGUIDE,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FhirVersion.STU3, url);
    }
}
