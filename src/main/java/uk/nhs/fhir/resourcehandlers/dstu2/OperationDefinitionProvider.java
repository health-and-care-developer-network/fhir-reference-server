/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers.dstu2;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.util.Optional;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
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
public class OperationDefinitionProvider extends AbstractResourceProviderDSTU2 {

    public OperationDefinitionProvider(FilesystemIF dataSource) {
    	super(dataSource, ResourceType.OPERATIONDEFINITION, ca.uhn.fhir.model.dstu2.resource.OperationDefinition.class);
    }

    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	OperationDefinition operation = (OperationDefinition)FHIRUtils.loadResourceFromFile(FhirVersion.DSTU2, thisFile);
    	String resourceName = operation.getName();
    	String url = operation.getUrl();
        String resourceID = getResourceIDFromURL(url, resourceName);
        String displayGroup = "Operations";
        VersionNumber versionNo = new VersionNumber(operation.getVersion());
        String status = operation.getStatus();
        
        return new ResourceMetadata(resourceName, thisFile, ResourceType.OPERATIONDEFINITION,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FhirVersion.DSTU2, url);
    }

}
