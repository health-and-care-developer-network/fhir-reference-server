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
package uk.nhs.fhir.resourcehandlers.dstu2;

import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.model.primitive.StringDt;
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
public class StructureDefinitionProvider extends AbstractResourceProviderDSTU2 {
    
	/**
     * Constructor, which tell us which data source and FHIR version we're working with.
     * @param dataSource
     */
    public StructureDefinitionProvider(FilesystemIF dataSource) {
        super(dataSource);
        resourceType = ResourceType.STRUCTUREDEFINITION;
        fhirVersion = FHIRVersion.DSTU2;
        fhirClass = ca.uhn.fhir.model.dstu2.resource.StructureDefinition.class;
    }

    public IBaseResource removeTextSection(IBaseResource resource) {
    	// Clear out the generated text
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv("");
    	StructureDefinition output = (StructureDefinition)resource;
    	output.setText(textElement);
    	return output;
    }
    
    public String getTextSection(IBaseResource resource) {
    	return ((StructureDefinition)resource).getText().getDivAsString();
    }
    
    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	String resourceName = null;
    	String baseType = null;
    	boolean extension = false;
    	String extensionCardinality = null;
    	ArrayList<String> extensionContexts = new ArrayList<String>();
    	String extensionDescription = null;
    	
    	StructureDefinition profile = (StructureDefinition)FHIRUtils.loadResourceFromFile(FHIRVersion.DSTU2, thisFile);
    	resourceName = profile.getName();
    	extension = (profile.getBase().equals("http://hl7.org/fhir/StructureDefinition/Extension"));
        
    	if (!extension) {
    		baseType = profile.getConstrainedType();
    	} else {
    		// Extra metadata for extensions
    		int min = profile.getSnapshot().getElementFirstRep().getMin();
    		String max = profile.getSnapshot().getElementFirstRep().getMax();
    		extensionCardinality = min + ".." + max;
    		
    		extensionContexts = new ArrayList<String>();
    		List<StringDt> contextList = profile.getContext();
    		for (StringDt context : contextList) {
    			extensionContexts.add(context.getValueAsString());
    		}
    		
    		extensionDescription = profile.getDifferential().getElementFirstRep().getShort();
    		if (extensionDescription == null) {
    			extensionDescription = profile.getDifferential().getElementFirstRep().getDefinition();
    		}
    		
    		List<ElementDefinitionDt> diffElements = profile.getDifferential().getElement();
    		boolean isSimple = false;
    		if (diffElements.size() == 3) {
    			if (diffElements.get(1).getPath().equals("Extension.url")) {
    				isSimple = true;
    				// It is a simple extension, so we can also find a type
    				List<Type> typeList = diffElements.get(2).getType();
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
        String status = profile.getStatus();
        
        return new ResourceMetadata(resourceName, thisFile, ResourceType.STRUCTUREDEFINITION,
							extension, baseType, displayGroup, false,
							resourceID, versionNo, status, null, extensionCardinality,
							extensionContexts, extensionDescription, FHIRVersion.DSTU2, url);
    }
    
}
