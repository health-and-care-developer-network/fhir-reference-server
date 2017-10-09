/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir.resourcehandlers.stu3;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;

import org.hl7.fhir.dstu3.model.ImplementationGuide;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.instance.model.api.IBaseResource;

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
public class ImplementationGuideProvider extends AbstractResourceProviderSTU3 {

	public ImplementationGuideProvider(FilesystemIF dataSource) {
		super(dataSource);
        resourceType = ResourceType.IMPLEMENTATIONGUIDE;
        fhirVersion = FHIRVersion.STU3;
        fhirClass = org.hl7.fhir.dstu3.model.ImplementationGuide.class;
    }
    
    
    public IBaseResource getResourceWithoutTextSection(IBaseResource resource) {
    	// Clear out the generated text
        Narrative textElement = new Narrative();
        textElement.setStatus(NarrativeStatus.GENERATED);
        textElement.setDivAsString("");
    	ImplementationGuide output = (ImplementationGuide)resource;
    	output.setText(textElement);
    	return output;
    }

    public String getTextSection(IBaseResource resource) {
    	return ((ImplementationGuide)resource).getText().getDivAsString();
    }
    
    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	ImplementationGuide guide = (ImplementationGuide)FHIRUtils.loadResourceFromFile(FHIRVersion.STU3, thisFile);
    	String resourceName = guide.getName();
    	String url = guide.getUrl();
    	String resourceID = getResourceIDFromURL(url, resourceName);
    	String displayGroup = "Implementation Guides";
        VersionNumber versionNo = new VersionNumber(guide.getVersion());
        String status = guide.getStatus().name();
    	
        return new ResourceMetadata(resourceName, thisFile, ResourceType.IMPLEMENTATIONGUIDE,
				false, null, displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FHIRVersion.STU3, url);
    }
}
