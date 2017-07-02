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

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.primitive.IdDt;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.PropertyReader;

/**
 *
 * @author Tim Coates
 * @author Adam Hatherly
 */
public class ResourceWebHandler {
    private static final Logger LOG = Logger.getLogger(ResourceWebHandler.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");
    private FHIRVersion fhirVersion = null;
    
    Datasource myDataSource = null;

    public ResourceWebHandler(Datasource dataSource, FHIRVersion fhirVersion) {
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
    
    
    
    public HashMap<String, List<ResourceEntity>> getAGroupedListOfResources(ResourceType resourceType) {
        LOG.fine("Called: ResourceWebHandler.getAlGroupedNames()");
        if(resourceType == STRUCTUREDEFINITION || resourceType == VALUESET
        		|| resourceType == OPERATIONDEFINITION || resourceType == IMPLEMENTATIONGUIDE) {
            HashMap<String, List<ResourceEntity>> myNames = null;
            if(resourceType == STRUCTUREDEFINITION) {
            	return myDataSource.getAllResourceNamesByBaseResource(fhirVersion, resourceType);
            } else {
            	return myDataSource.getAllResourceNamesByCategory(fhirVersion, resourceType);
            }
        }
        return null;
    }

    public List<ResourceEntity> getAllNames(ResourceType resourceType, String namePart) {
        LOG.fine("Called: ResourceWebHandler.getAllNames(String namePart)");
        List<ResourceEntity> myResourceList = myDataSource.getAllResourceIDforResourcesMatchingNamePattern(fhirVersion, resourceType, namePart);
        return myResourceList;
    }
    
    public List<ResourceEntity> getExtensions() {
        LOG.fine("Called: ResourceWebHandler.getExtensions()");
        return myDataSource.getExtensions(fhirVersion);
    }
    
    public ResourceEntityWithMultipleVersions getVersionsForID(IdDt id) {
        LOG.fine("Called: ResourceWebHandler.getVersionsForID(IdDt id)");
        return myDataSource.getVersionsByID(fhirVersion, id);
    }
    
    public ResourceEntityWithMultipleVersions getVersionsForID(IdType id) {
        LOG.fine("Called: ResourceWebHandler.getVersionsForID(IdDt id)");
        return myDataSource.getVersionsByID(fhirVersion, id);
    }
    
    public ResourceEntity getResourceEntityByID(IdDt theId) {
        LOG.fine("Called: ResourceWebHandler.getResourceEntityByID(IdDt id)");
        return myDataSource.getResourceEntityByID(fhirVersion, theId);
    }
    
    public ResourceEntity getResourceEntityByID(IdType theId) {
        LOG.fine("Called: ResourceWebHandler.getResourceEntityByID(IdDt id)");
        return myDataSource.getResourceEntityByID(fhirVersion, theId);
    }
    
    public IBaseResource getResourceByID(IdDt id) {
        LOG.fine("Called: ResourceWebHandler.getResourceByID(IdDt id)");
        IBaseResource resource = myDataSource.getResourceByID(fhirVersion, id);
        return resource;
    }

    public IBaseResource getResourceByID(IdType id) {
        LOG.fine("Called: ResourceWebHandler.getResourceByID(IdDt id)");
        IBaseResource resource = myDataSource.getResourceByID(fhirVersion, id);
        return resource;
    }

    public ExampleResources getExamples(String resourceTypeAndID) {
        LOG.fine("Called: ResourceWebHandler.getExamples(String resourceTypeAndID)");
        ExampleResources examples = myDataSource.getExamples(fhirVersion, resourceTypeAndID);
        return examples;
    }
    
    public HashMap<String,Integer> getResourceTypeCounts() {
    	return myDataSource.getResourceTypeCounts(fhirVersion);
    }

	public Datasource getMyDataSource() {
		return myDataSource;
	}
}
