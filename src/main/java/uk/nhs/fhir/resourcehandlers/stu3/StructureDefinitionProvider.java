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
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.FHIRUtils;

/**
 *
 * @author Tim Coates
 */
public class StructureDefinitionProvider extends AbstractResourceProviderSTU3 {

	public StructureDefinitionProvider(FilesystemIF dataSource) {
		super(dataSource);
        resourceType = ResourceType.STRUCTUREDEFINITION;
        fhirVersion = FHIRVersion.STU3;
        fhirClass = org.hl7.fhir.dstu3.model.StructureDefinition.class;
    }

    
    public IBaseResource getResourceWithoutTextSection(IBaseResource resource) {
    	// Clear out the generated text
        Narrative textElement = new Narrative();
        textElement.setStatus(NarrativeStatus.GENERATED);
        textElement.setDivAsString("");
    	StructureDefinition output = (StructureDefinition)resource;
    	output.setText(textElement);
    	return output;
    }
    
    public String getTextSection(IBaseResource resource) {
    	return ((StructureDefinition)resource).getText().getDivAsString();
    }

    public ResourceEntity getMetadataFromResource(File thisFile) {
    	String resourceName = null;
    	String baseType = null;
    	boolean extension = false;
    	String extensionCardinality = null;
    	ArrayList<String> extensionContexts = new ArrayList<String>();
    	String extensionDescription = null;
    	
    	org.hl7.fhir.dstu3.model.StructureDefinition profile =
    			(org.hl7.fhir.dstu3.model.StructureDefinition)FHIRUtils.loadResourceFromFile(FHIRVersion.STU3, thisFile);
    	resourceName = profile.getName();
    	extension = (profile.getBaseDefinition().equals("http://hl7.org/fhir/StructureDefinition/Extension"));
        
    	if (!extension) {
    		baseType = profile.getType();
    	} else {
    		// Extra metadata for extensions
    		int min = profile.getSnapshot().getElementFirstRep().getMin();
    		String max = profile.getSnapshot().getElementFirstRep().getMax();
    		extensionCardinality = min + ".." + max;
    		
    		extensionContexts = new ArrayList<String>();
    		List<StringType> contextList = profile.getContext();
    		for (StringType context : contextList) {
    			extensionContexts.add(context.getValueAsString());
    		}
    		
    		extensionDescription = profile.getDifferential().getElementFirstRep().getShort();
    		if (extensionDescription == null) {
    			extensionDescription = profile.getDifferential().getElementFirstRep().getDefinition();
    		}
    		
    		List<ElementDefinition> diffElements = profile.getDifferential().getElement();
    		boolean isSimple = false;
    		if (diffElements.size() == 3) {
    			if (diffElements.get(1).getPath().equals("Extension.url")) {
    				isSimple = true;
    				// It is a simple extension, so we can also find a type
    				List<TypeRefComponent> typeList = diffElements.get(2).getType();
    				if (typeList.size() == 1) {
    					baseType = typeList.get(0).getCode();
    				} else {
    					baseType = "(choice)";
    				}
    			}
    		}
    		if (!isSimple) {
    			baseType = "(complex)";
    		}
    	
    	}
        String url = profile.getUrl();
        String resourceID = getResourceIDFromURL(url, resourceName);
        String displayGroup = baseType;
        VersionNumber versionNo = new VersionNumber(profile.getVersion());
        String status = profile.getStatus().name();
        
        return new ResourceEntity(resourceName, thisFile, ResourceType.STRUCTUREDEFINITION,
				extension, baseType, displayGroup, false,
				resourceID, versionNo, status, null, extensionCardinality,
				extensionContexts, extensionDescription, FHIRVersion.STU3, url);
    }

}
