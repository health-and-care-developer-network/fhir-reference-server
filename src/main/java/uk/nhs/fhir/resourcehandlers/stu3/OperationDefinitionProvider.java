/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers.stu3;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.OperationDefinition;
import org.hl7.fhir.instance.model.api.IBaseResource;

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
public class OperationDefinitionProvider extends AbstractResourceProviderSTU3 {

	public OperationDefinitionProvider(FilesystemIF dataSource) {
		super(dataSource);
        resourceType = ResourceType.OPERATIONDEFINITION;
        fhirVersion = FHIRVersion.STU3;
        fhirClass = org.hl7.fhir.dstu3.model.OperationDefinition.class;
    }
        
    public IBaseResource removeTextSection(IBaseResource resource) {
    	// Clear out the generated text
        Narrative textElement = new Narrative();
        textElement.setStatus(NarrativeStatus.GENERATED);
        textElement.setDivAsString("");
    	OperationDefinition output = (OperationDefinition)resource;
    	output.setText(textElement);
    	return output;
    }
    
    public String getTextSection(IBaseResource resource) {
    	return ((OperationDefinition)resource).getText().getDivAsString();
    }

    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	
    	OperationDefinition operation = (OperationDefinition)FHIRUtils.loadResourceFromFile(FHIRVersion.STU3, thisFile);
    	String resourceName = operation.getName();
    	String url = operation.getUrl();
    	String resourceID = getResourceIDFromURL(url, resourceName);
    	String displayGroup = "Operations";
        VersionNumber versionNo = new VersionNumber(operation.getVersion());
        String status = operation.getStatus().name();
    	
        return new ResourceMetadata(resourceName, thisFile, ResourceType.OPERATIONDEFINITION,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FHIRVersion.STU3, url);
    }

}
