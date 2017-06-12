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

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.primitive.IdDt;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.PropertyReader;

import static uk.nhs.fhir.enums.ResourceType.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Coates
 * @author Adam Hatherly
 */
public class ResourceWebHandler {
    private static final Logger LOG = Logger.getLogger(PatientProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");
    
    Datasource myDataSource = null;

    public ResourceWebHandler(Datasource dataSource) {
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
        LOG.fine("Created ResourceWebHandler handler to respond to requests for Profile resource types from a browser.");
    }
    
    
    
    public HashMap<String, List<ResourceEntity>> getAGroupedListOfResources(ResourceType resourceType) {
        LOG.fine("Called: ResourceWebHandler.getAlGroupedNames()");
        if(resourceType == STRUCTUREDEFINITION || resourceType == VALUESET
        		|| resourceType == OPERATIONDEFINITION || resourceType == IMPLEMENTATIONGUIDE) {
            HashMap<String, List<ResourceEntity>> myNames = null;
            if(resourceType == STRUCTUREDEFINITION) {
            	return myDataSource.getAllResourceNamesByBaseResource(resourceType);
            } else {
            	return myDataSource.getAllResourceNamesByCategory(resourceType);
            }
        }
        return null;
    }

    public List<ResourceEntity> getAllNames(ResourceType resourceType, String namePart) {
        LOG.fine("Called: ResourceWebHandler.getAllNames(String namePart)");
        List<ResourceEntity> myResourceList = myDataSource.getAllResourceIDforResourcesMatchingNamePattern(resourceType, namePart);
        return myResourceList;
    }
    
    public List<ResourceEntity> getExtensions() {
        LOG.fine("Called: ResourceWebHandler.getExtensions()");
        return myDataSource.getExtensions();
    }
    
    public ResourceEntityWithMultipleVersions getVersionsForID(IdDt id) {
        LOG.fine("Called: ResourceWebHandler.getVersionsForID(IdDt id)");
        return myDataSource.getVersionsByID(id);
    }
    
    public ResourceEntity getResourceEntityByID(IdDt theId) {
        LOG.fine("Called: ResourceWebHandler.getResourceEntityByID(IdDt id)");
        return myDataSource.getResourceEntityByID(theId);
    }
    
    public IResource getResourceByID(IdDt id) {
        LOG.fine("Called: ResourceWebHandler.getResourceByID(IdDt id)");
        IResource resource = (IResource)myDataSource.getResourceByID(id);
        return resource;
    }
    
    public StructureDefinition getSDByID(IdDt id) {
        LOG.fine("Called: ResourceWebHandler.getSDByID(String id)");
        StructureDefinition sd = (StructureDefinition)myDataSource.getResourceByID(id);
        return sd;
    }

    public OperationDefinition getOperationByID(IdDt id) {
        LOG.fine("Called: ResourceWebHandler.getOperationByID(String id)");
        OperationDefinition od = (OperationDefinition)myDataSource.getResourceByID(id);
        return od;
    }

    public ImplementationGuide getImplementationGuideByID(IdDt id) {
        LOG.fine("Called: ResourceWebHandler.getImplementationGuideByID(String id)");
        ImplementationGuide ig = (ImplementationGuide)myDataSource.getResourceByID(id);
        return ig;
    }
    
    public ValueSet getVSByID(IdDt id) {
        LOG.fine("Called: ResourceWebHandler.getVSByID(String id)");
        ValueSet valSet = (ValueSet)myDataSource.getResourceByID(id);
        return valSet;
    }
    
    public ExampleResources getExamples(String resourceTypeAndID) {
        LOG.fine("Called: ResourceWebHandler.getExamples(String resourceTypeAndID)");
        ExampleResources examples = myDataSource.getExamples(resourceTypeAndID);
        return examples;
    }
    
    public HashMap<String,Integer> getResourceTypeCounts() {
    	return myDataSource.getResourceTypeCounts();
    }
}
