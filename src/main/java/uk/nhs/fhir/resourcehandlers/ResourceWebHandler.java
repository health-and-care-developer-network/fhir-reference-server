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
package uk.nhs.fhir.resourcehandlers;

import static uk.nhs.fhir.enums.ResourceType.IMPLEMENTATIONGUIDE;
import static uk.nhs.fhir.enums.ResourceType.OPERATIONDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.STRUCTUREDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.VALUESET;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;

import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.html.ExtensionsListProvider;
import uk.nhs.fhir.html.ResourceCountsProvider;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.FhirServerProperties;

/**
 *
 * @author Tim Coates
 * @author Adam Hatherly
 */
public class ResourceWebHandler implements ResourceCountsProvider, ExtensionsListProvider {
    private static final Logger LOG = Logger.getLogger(ResourceWebHandler.class.getName());
    private static String logLevel = FhirServerProperties.getProperty("logLevel");
    private FHIRVersion fhirVersion = null;
    
    FilesystemIF myDataSource = null;

    public ResourceWebHandler(FilesystemIF dataSource, FHIRVersion fhirVersion) {
    	
        LOG.setLevel(Level.INFO);

        if(logLevel.equals("INFO")) {
           LOG.setLevel(Level.INFO);
        }
        if(logLevel.equals("FINE")) {
            LOG.setLevel(Level.FINE);
        }
        if(logLevel.equals("OFF")) {
            LOG.setLevel(Level.OFF);
        }
        myDataSource = dataSource;
        this.fhirVersion = fhirVersion;
        
        LOG.fine("Created ResourceWebHandler handler to respond to requests for Profile resource types from a browser.");
    }
    
    
    
    public HashMap<String, List<ResourceMetadata>> getAGroupedListOfResources(ResourceType resourceType) {
        LOG.fine("Called: ResourceWebHandler.getAlGroupedNames()");
        
        if (resourceType == STRUCTUREDEFINITION || resourceType == VALUESET
          || resourceType == OPERATIONDEFINITION || resourceType == IMPLEMENTATIONGUIDE) {

            if (resourceType == STRUCTUREDEFINITION) {
            	return myDataSource.getAllResourceNamesByBaseResource(resourceType);
            } else {
            	return myDataSource.getAllResourceNamesByCategory(resourceType);
            }
        }
        
        return null;
    }

    public List<ResourceMetadata> getAllNames(ResourceType resourceType, String namePart) {
        LOG.fine("Called: ResourceWebHandler.getAllNames(String namePart)");
        
        List<ResourceMetadata> myResourceList = myDataSource.getAllResourceIDforResourcesMatchingNamePattern(fhirVersion, resourceType, namePart);
        
        return myResourceList;
    }
    
    @Override
    public List<ResourceMetadata> getExtensions() {
        LOG.fine("Called: ResourceWebHandler.getExtensions()");
        
        return myDataSource.getExtensions();
    }
    
    public ResourceEntityWithMultipleVersions getVersionsForID(IIdType id) {
        LOG.fine("Called: ResourceWebHandler.getVersionsForID(IIdType id)");
        
        return myDataSource.getVersionsByID(fhirVersion, id);
    }
    
    public ResourceMetadata getResourceEntityByID(IIdType theId) {
        LOG.fine("Called: ResourceWebHandler.getResourceEntityByID(IIdType id)");
        
        return myDataSource.getResourceEntityByID(fhirVersion, theId);
    }

    public IBaseResource getResourceByID(IIdType id) {
        LOG.fine("Called: ResourceWebHandler.getResourceByID(IIdType id)");
        
        IBaseResource resource = myDataSource.getResourceByID(fhirVersion, id);
        return resource;
    }

    public ExampleResources getExamples(String resourceTypeAndID) {
        LOG.fine("Called: ResourceWebHandler.getExamples(String resourceTypeAndID)");
        
        ExampleResources examples = myDataSource.getExamples(fhirVersion, resourceTypeAndID);
        return examples;
    }
    
    public HashMap<String,Integer> getResourceTypeCounts() {
    	return myDataSource.getResourceTypeCounts();
    }

	public FilesystemIF getMyDataSource() {
		return myDataSource;
	}
}
