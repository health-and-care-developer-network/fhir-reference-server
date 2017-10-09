/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.resourcehandlers.stu3;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;

import org.hl7.fhir.dstu3.model.ConceptMap;
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
 * @author Tim Coates
 */
public class ConceptMapProvider extends AbstractResourceProviderSTU3 {

	
	public ConceptMapProvider(FilesystemIF dataSource) {
		super(dataSource);
        resourceType = ResourceType.CONCEPTMAP;
        fhirVersion = FHIRVersion.STU3;
        fhirClass = org.hl7.fhir.dstu3.model.ConceptMap.class;
    }
    
    public IBaseResource getResourceWithoutTextSection(IBaseResource resource) {
    	// Clear out the generated text
    	Narrative textElement = new Narrative();
        textElement.setStatus(NarrativeStatus.GENERATED);
        textElement.setDivAsString("");
        ConceptMap output = (ConceptMap)resource;
    	output.setText(textElement);
    	return output;
    }
    
    public String getTextSection(IBaseResource resource) {
    	return ((ConceptMap)resource).getText().getDivAsString();
    }

    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	String displayGroup = "Concept Map";
    	ConceptMap profile = (ConceptMap)FHIRUtils.loadResourceFromFile(FHIRVersion.STU3, thisFile);
    	String resourceName = profile.getName();
    	String url = profile.getUrl();
    	String resourceID = getResourceIDFromURL(url, resourceName);
    	VersionNumber versionNo = new VersionNumber(profile.getVersion());
    	String status = profile.getStatus().name();
    	
    	return new ResourceMetadata(resourceName, thisFile, ResourceType.CONCEPTMAP,
				false, null, displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, FHIRVersion.STU3, url);
    }

}
