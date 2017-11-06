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
package uk.nhs.fhir.util;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.hl7.fhir.dstu3.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet.ComposeInclude;
import uk.nhs.fhir.FhirURLConstants;

public class FHIRUtils {

    /**
     * Constructor, never explicitly called
     */
    private FHIRUtils() {}

    private static final Logger LOG = LoggerFactory.getLogger(FHIRUtils.class.getName());

    private static FhirContext ctxDSTU2 = FhirContexts.forVersion(FhirVersion.DSTU2);
    private static FhirContext ctxSTU3 = FhirContexts.forVersion(FhirVersion.STU3);


    /**
     * Method to load a fhir resource from a file.
     * 
     * @param file File object pointing to the file we want to load
     * @return A resource object
     */
    public static IBaseResource loadResourceFromFile(FhirVersion fhirVersion, final File file) {
        IBaseResource resource = null;
        try {
        	FileReader fr = new FileReader(file);
        	
        	if (fhirVersion.equals(FhirVersion.DSTU2)) {
        		resource = ctxDSTU2.newXmlParser().parseResource(fr);
        	} else if (fhirVersion.equals(FhirVersion.STU3)) {
        		resource = ctxSTU3.newXmlParser().parseResource(fr);
        	}
            String url = null;
            
            LOG.debug("Parsed resource and identified it's class as: " + resource.getClass().getName());

            // To get the URL we need to cast this to a concrete type
            if (resource instanceof StructureDefinition) {
            	url = ((StructureDefinition)resource).getUrl();
            } else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.ValueSet) {
            	url = ((ca.uhn.fhir.model.dstu2.resource.ValueSet)resource).getUrl();
            } else if (resource instanceof OperationDefinition) {
            	url = ((OperationDefinition)resource).getUrl();
            } else if (resource instanceof Conformance) {
            	url = ((Conformance)resource).getUrl();
            } 
            
            else if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
            	url = ((org.hl7.fhir.dstu3.model.StructureDefinition)resource).getUrl();
            } else if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
            	url = ((org.hl7.fhir.dstu3.model.ValueSet)resource).getUrl();
            } else if (resource instanceof org.hl7.fhir.dstu3.model.OperationDefinition) {
            	url = ((org.hl7.fhir.dstu3.model.OperationDefinition)resource).getUrl();
            } else if (resource instanceof org.hl7.fhir.dstu3.model.CodeSystem) {
            	url = ((org.hl7.fhir.dstu3.model.CodeSystem)resource).getUrl();
            } else if (resource instanceof org.hl7.fhir.dstu3.model.ConceptMap) {
            	url = ((org.hl7.fhir.dstu3.model.ConceptMap)resource).getUrl();
            } 
            
            // Should never get this as it relies on parsing by the hapi-fhir-structures-hl7org-dstu2 package.
            // There doesn't seem to be a notion of a Conformance object in org.hl7.fhir.dstu3.model 
            //else if (resource instanceof org.hl7.fhir.instance.model.Conformance) {
            //	url = ((org.hl7.fhir.instance.model.Conformance)resource).getUrl();
            //} 
            
            // If we can't get the ID from the URL for some reason, fall back on using the filename as the ID
            String id = FileLoader.removeFileExtension(file.getName());
            if (url != null) {
            	id = getResourceIDFromURL(url, id);
            }
            resource.setId(id);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.debug("Resource loaded from file: " + file.getName());
        return resource;
    }
    
    public static boolean isValueSetSNOMED(ca.uhn.fhir.model.dstu2.resource.ValueSet vs) {
    	if (vs.getCompose() != null
    	  && vs.getCompose().getInclude() != null) {
    		
			List<ComposeInclude> includeList = vs.getCompose().getInclude();
			
			for (ComposeInclude includeEntry : includeList) {
				
				if (includeEntry.getSystem() != null
				  && includeEntry.getSystem().equals(FhirURLConstants.SNOMED_ID)) {
			
					return true;
				}
			}
    	}
    	
    	return false;
    }
    
    public static boolean isSTU3ValueSetSNOMED(org.hl7.fhir.dstu3.model.ValueSet vs) {
    	if (vs.getCompose() != null) {
    		if (vs.getCompose().getInclude() != null) {
    			List<ConceptSetComponent> includeList = vs.getCompose().getInclude();
				for (ConceptSetComponent includeEntry : includeList) {
					if (includeEntry.getSystem() != null) {
						if (includeEntry.getSystem().equals(FhirURLConstants.SNOMED_ID)) {
							return true;
						}
					}
				}
    		}
    	}
    	return false;
    }
    
    public static boolean isValueSetSNOMED(org.hl7.fhir.dstu3.model.ValueSet vs) {
    	if (vs.getCompose() != null) {
    		if (vs.getCompose().getInclude() != null) {
    			List<ConceptSetComponent> includeList = vs.getCompose().getInclude();
				for (ConceptSetComponent includeEntry : includeList) {
					if (includeEntry.getSystem() != null) {
						if (includeEntry.getSystem().equals(FhirURLConstants.SNOMED_ID)) {
							return true;
						}
					}
				}
    		}
    	}
    	return false;
    }
    
    public static String getResourceIDFromURL(String url, String def) {
    	// Find the actual name of the resource from the URL
        int idx = url.lastIndexOf('/');
        if (idx > -1) {
        	return url.substring(idx+1);
        } else {
        	// Can't find a real name in the URL!
        	return def;
        }
    }

}
