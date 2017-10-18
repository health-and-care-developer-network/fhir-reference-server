/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers.dstu2;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import uk.nhs.fhir.data.metadata.FHIRVersion;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.util.FHIRUtils;

/**
 *
 * @author tim
 */
public class OperationDefinitionProvider extends AbstractResourceProviderDSTU2 {

    public OperationDefinitionProvider(FilesystemIF dataSource) {
    	super(dataSource);
        resourceType = ResourceType.OPERATIONDEFINITION;
        fhirVersion = FHIRVersion.DSTU2;
        fhirClass = ca.uhn.fhir.model.dstu2.resource.OperationDefinition.class;
    }
        
    public IBaseResource removeTextSection(IBaseResource resource) {
    	// Clear out the generated text
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv("");
    	OperationDefinition output = (OperationDefinition)resource;
    	output.setText(textElement);
    	return output;
    }
    
    public String getTextSection(IBaseResource resource) {
    	return ((OperationDefinition)resource).getText().getDivAsString();
    }

    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	OperationDefinition operation = (OperationDefinition)FHIRUtils.loadResourceFromFile(FHIRVersion.DSTU2, thisFile);
    	String resourceName = operation.getName();
    	String url = operation.getUrl();
        String resourceID = getResourceIDFromURL(url, resourceName);
        String displayGroup = "Operations";
        VersionNumber versionNo = new VersionNumber(operation.getVersion());
        String status = operation.getStatus();
        
        return new ResourceMetadata(resourceName, thisFile, ResourceType.OPERATIONDEFINITION,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FHIRVersion.DSTU2, url);
    }

}
