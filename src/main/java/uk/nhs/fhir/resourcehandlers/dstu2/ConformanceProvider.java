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
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import uk.nhs.fhir.data.metadata.FHIRVersion;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.util.FHIRUtils;

/**
 *
 * @author Adam Hatherly
 */
public class ConformanceProvider extends AbstractResourceProviderDSTU2 {

    public ConformanceProvider(FilesystemIF dataSource) {
    	super(dataSource);
        resourceType = ResourceType.CONFORMANCE;
        fhirVersion = FHIRVersion.DSTU2;
        fhirClass = ca.uhn.fhir.model.dstu2.resource.Conformance.class;
    }
    
    
    public IBaseResource removeTextSection(IBaseResource resource) {
    	// Clear out the generated text
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv("");
    	Conformance output = (Conformance)resource;
    	output.setText(textElement);
    	return output;
    }

    public String getTextSection(IBaseResource resource) {
    	return ((Conformance)resource).getText().getDivAsString();
    }
    
    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	Conformance operation = (Conformance)FHIRUtils.loadResourceFromFile(FHIRVersion.DSTU2, thisFile);
    	String resourceName = operation.getName();
    	String url = operation.getUrl();
        String resourceID = getResourceIDFromURL(url, resourceName);
        String displayGroup = "Conformance";
        VersionNumber versionNo = new VersionNumber(operation.getVersion());
        String status = operation.getStatus();
        
        return new ResourceMetadata(resourceName, thisFile, ResourceType.CONFORMANCE,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FHIRVersion.DSTU2, url);
    }
}
