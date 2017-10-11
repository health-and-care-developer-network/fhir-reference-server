/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers.dstu2;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FHIRVersion;

/**
 *
 * @author tim
 */
public class ImplementationGuideProvider extends AbstractResourceProviderDSTU2 {

    public ImplementationGuideProvider(FilesystemIF dataSource) {
    	super(dataSource);
        resourceType = ResourceType.IMPLEMENTATIONGUIDE;
        fhirVersion = FHIRVersion.DSTU2;
        fhirClass = ca.uhn.fhir.model.dstu2.resource.ImplementationGuide.class;
    }
        
    public IBaseResource removeTextSection(IBaseResource resource) {
    	// Clear out the generated text
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv("");
    	ImplementationGuide output = (ImplementationGuide)resource;
    	output.setText(textElement);
    	return output;
    }

    public String getTextSection(IBaseResource resource) {
    	return ((ImplementationGuide)resource).getText().getDivAsString();
    }
    
    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	ImplementationGuide guide = (ImplementationGuide)FHIRUtils.loadResourceFromFile(FHIRVersion.DSTU2, thisFile);
    	String resourceName = guide.getName();
    	String url = guide.getUrl();
        String resourceID = getResourceIDFromURL(url, resourceName);
        String displayGroup = "Implementation Guides";
        VersionNumber versionNo = new VersionNumber(guide.getVersion());
        String status = guide.getStatus();
        
        return new ResourceMetadata(resourceName, thisFile, ResourceType.IMPLEMENTATIONGUIDE,
				false, null, displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FHIRVersion.DSTU2, url);
    }
}
