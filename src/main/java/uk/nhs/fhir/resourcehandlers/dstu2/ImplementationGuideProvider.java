/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers.dstu2;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.util.Optional;

import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
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
public class ImplementationGuideProvider extends AbstractResourceProviderDSTU2 {

    public ImplementationGuideProvider(FilesystemIF dataSource) {
    	super(dataSource, ResourceType.IMPLEMENTATIONGUIDE, ImplementationGuide.class);
    }
    
    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	ImplementationGuide guide = (ImplementationGuide)FHIRUtils.loadResourceFromFile(FhirVersion.DSTU2, thisFile);
    	String resourceName = guide.getName();
    	String url = guide.getUrl();
        String resourceID = getResourceIDFromURL(url, resourceName);
        String displayGroup = "Implementation Guides";
        VersionNumber versionNo = new VersionNumber(guide.getVersion());
        String status = guide.getStatus();
        
        return new ResourceMetadata(resourceName, thisFile, ResourceType.IMPLEMENTATIONGUIDE,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FhirVersion.DSTU2, url);
    }
}
