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
import java.util.Optional;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.primitive.StringDt;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FhirVersion;

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
        super(dataSource, ResourceType.STRUCTUREDEFINITION, ca.uhn.fhir.model.dstu2.resource.StructureDefinition.class);
    }
    
    public ResourceMetadata getMetadataFromResource(File thisFile) {
    	StructureDefinition profile = (StructureDefinition)FHIRUtils.loadResourceFromFile(FhirVersion.DSTU2, thisFile);
    	String resourceName = profile.getName();
    	boolean extension = (profile.getBase().equals("http://hl7.org/fhir/StructureDefinition/Extension"));

    	String baseType;
    	String extensionCardinality;
    	List<String> extensionContexts;
    	String extensionDescription;
    	if (!extension) {
    		extensionCardinality = null;
    		extensionContexts = new ArrayList<String>();
			extensionDescription = null;
    		
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
    		baseType = null;
    		if (diffElements.size() == 3
    		  && diffElements.get(1).getPath().equals("Extension.url")) {
				// It is a simple extension, so we can also find a type
				List<Type> typeList = diffElements.get(2).getType();
				if (typeList.size() == 1) {
					baseType = typeList.get(0).getCode();
				} else {
					baseType = "(choice)";
				}
    		}
    		if (baseType == null) {
    			baseType = "(complex)";
    		}
    	
    	}
    	
        String url = profile.getUrl();
        String resourceID = getResourceIDFromURL(url, resourceName);
        String displayGroup = baseType;
        VersionNumber versionNo = new VersionNumber(profile.getVersion());
        String status = profile.getStatus();
        
        return new ResourceMetadata(resourceName, thisFile, ResourceType.STRUCTUREDEFINITION,
							extension, Optional.of(baseType), displayGroup, false,
							resourceID, versionNo, status, null, extensionCardinality,
							extensionContexts, extensionDescription, FhirVersion.DSTU2, url);
    }
    
}
