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

import ca.uhn.fhir.model.dstu2.resource.ValueSet.ComposeInclude;
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.load.FileLoader;

public class FHIRUtils {

    /**
     * Constructor, never explicitly called
     */
    private FHIRUtils() {}

    private static final Logger LOG = LoggerFactory.getLogger(FHIRUtils.class.getName());

    /**
     * Method to load a fhir resource from a file.
     * 
     * @param file File object pointing to the file we want to load
     * @return A resource object
     */
    public static IBaseResource loadResourceFromFile(FhirVersion fhirVersion, final File file) {
    	
    	// ensure that we throw for unhandled FHIR Version by calling this before the try/catch
    	IParser xmlParser = FhirContexts.xmlParser(fhirVersion);
    	
    	IBaseResource resource = null;
        try (FileReader fr = new FileReader(file)) {
			resource = xmlParser.parseResource(fr);
            
            LOG.debug("Parsed resource and identified it's class as: " + resource.getClass().getName());
            
            // getUrl() is declared on each class individually.
            // Avoids a big if/else block
            String url = FhirReflectionUtils.expectUrlByReflection(resource);
            
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
    	if (vs.getCompose() != null
    	  && vs.getCompose().getInclude() != null) {
    		
			List<ConceptSetComponent> includeList = vs.getCompose().getInclude();
			
			for (ConceptSetComponent includeEntry : includeList) {
				
				if (includeEntry.getSystem() != null
				 && includeEntry.getSystem().equals(FhirURLConstants.SNOMED_ID)) {
					
					return true;
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
