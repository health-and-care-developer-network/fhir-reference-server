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
package uk.nhs.fhir.datalayer;

import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PropertyReader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author Tim Coates
 */
public class FilesystemIF implements Datasource {
    private static final Logger LOG = Logger.getLogger(FilesystemIF.class.getName());

    private static String logLevel = PropertyReader.getProperty("logLevel");

    /**
     * Constructor, we simply set the logging level, and log that we've been instantiated.
     * 
     */
    public FilesystemIF() {
        LOG.setLevel(Level.INFO);

        if(logLevel.equals("FINE")) {
            LOG.setLevel(Level.FINE);
        }
        if(logLevel.equals("OFF")) {
            LOG.setLevel(Level.OFF);
        }
        LOG.info("FilesystemIF instantiated, using filesystem datasource.");
    }

    /**
     * Gets a specific one
     * @param id
     * @return 
     */
    public IBaseResource getResourceByID(String id) {
    	ResourceEntity entry = FileCache.getSingleResourceByID(id);
    	File path = entry.getResourceFile();
    	LOG.info("Getting Resource with id=" + id + " looking for file: " + path.getAbsolutePath());
        
    	IBaseResource foundResource = FHIRUtils.loadResourceFromFile(path);
        return foundResource;
    }
    
    /**
     * Get a single resource back by name
     * @param name
     * @return
     */
    public StructureDefinition getSingleStructureDefinitionByName(String name) {
    	ResourceEntity entry = FileCache.getSingleResourceByName(name);
    	File path = entry.getResourceFile();
    	LOG.info("Getting StructureDefinition with id=" + name + " looking for file: " + path.getAbsolutePath());
        
    	StructureDefinition foundProfile = (StructureDefinition)FHIRUtils.loadResourceFromFile(path);
        return foundProfile;
    }

    /**
     * This is the method to do a search based on name, ie to find where
     * name:contains=[parameter]
     * 
     * @param theNamePart
     * @return 
     */
    public List<IBaseResource> getResourceMatchByName(ResourceType resourceType, String theNamePart) {
        LOG.info("Getting " + resourceType.name() + " resources with name containing: " + theNamePart);
        
        List<IBaseResource> list = new ArrayList<IBaseResource>();
        List<String> matchingIDs = getAllResourceIDforResourcesMatchingNamePattern(resourceType, theNamePart);

        for(String id : matchingIDs) {
        	list.add(getResourceByID(id));
        }
        return list;
    }
    

    /**
     * Gets a full list of StructureDefinition objects. Not especially performant.
     * 
     * @return 
     */
    public List<IBaseResource> getAllResourcesOfType(ResourceType resourceType) {
        LOG.info("Getting all resources of type: " + resourceType.name());
        return FileCache.getResources(resourceType);
    }
    
    /**
     * Gets a full list of names for the web view of /StructureDefinition requests.
     * 
     * @return 
     */
    public List<String> getAllResourceNames(ResourceType resourceType) {
        LOG.info("Getting all Resource Names for type: " + resourceType.name());
        return FileCache.getResourceNameList(resourceType);
    }
    
    /**
     * Gets a full list of names grouped by base resource for the web view 
     * of /StructureDefinition requests.
     * 
     * @return 
     */
    public HashMap<String, List<ResourceEntity>> getAllResourceNamesByBaseResource(ResourceType resourceType) {
        LOG.info("Getting all Resource Names by base resource");
        return FileCache.getGroupedNameList(resourceType);
    }
    
    /**
     * Gets a full list of resource names grouped by the broad category of the resource
     * for the web view of /[ResourceType] requests.
     */
    @Override
	public HashMap<String, List<ResourceEntity>> getAllResourceNamesByCategory(ResourceType resourceType) {
    	LOG.info("Getting all Resource Names by category");
        return FileCache.getGroupedNameList(resourceType);
	}
    

    /**
     * This is the method to search by name, e.g. name:contains=Patient
     * 
     * @param theNamePart
     * @return a list of IDs of matching resources
     */
    public List<String> getAllResourceIDforResourcesMatchingNamePattern(ResourceType resourceType, String theNamePart) {
        LOG.info("Getting all StructureDefinition Names containing: " + theNamePart + " in their name");
        
        LOG.info("Getting full list of profiles first");
        List<ResourceEntity> resourceList = FileCache.getResourceList();
        
        LOG.info("Now filtering the list to those matching our criteria");
        ArrayList<String> matches = new ArrayList<String>();
        
        String pattern = "(.*)" + theNamePart + "(.*)";
        
        for (ResourceEntity entry : resourceList) {
        	
        	String resourceName = entry.getResourceName();
        	
        	// Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(resourceName);
            if (m.find()) {
               matches.add(entry.getResourceID());
            }
        }
        LOG.info("Returning matches");
        return matches;
    }

    /**
     * Gets a specific ValueSet specified by id.
     * 
     * @param id
     * @return 
     */
    public ValueSet getSingleValueSetByID(String id) {
    	ResourceEntity entry = FileCache.getSingleResourceByID(id);
    	File path = entry.getResourceFile();
    	LOG.info("Getting ValueSet with id=" + id + " looking for file: " + path.getAbsolutePath());
        
    	ValueSet foundValSet = (ValueSet)FHIRUtils.loadResourceFromFile(path);
        return foundValSet;
    }

    /**
     * Gets a specific ValueSet specified by name.
     * 
     * @param name
     * @return 
     */
    public ValueSet getSingleValueSetByName(String name) {
    	ResourceEntity entry = FileCache.getSingleResourceByName(name);
    	File path = entry.getResourceFile();
    	LOG.info("Getting ValueSet with name=" + name + " looking for file: " + path.getAbsolutePath());
        
    	ValueSet foundValSet = (ValueSet)FHIRUtils.loadResourceFromFile(path);
        return foundValSet;
    }
    
    /**
     * This is the method to do a search based on name, ie to find where
     * name:contains=[parameter]
     * 
     * @param theNamePart
     * @return a List of matched ValueSet objects
     */
    public List<ValueSet> getValueSetMatchByName(String theNamePart) {
        LOG.info("Getting ValueSets with name=" + theNamePart);
        List<ValueSet> list = new ArrayList<ValueSet>();
        List<String> matchingNames = getAllMatchedValueSetNames(theNamePart);

        for(String name : matchingNames) {
            list.add(getSingleValueSetByName(name));
        }
        return list;
    }

    /**
     * This is the method to search by name, e.g. name:contains=Patient
     * 
     * @param theNamePart
     * @return 
     */
    public List<String> getAllMatchedValueSetNames(String theNamePart) {
        LOG.info("Getting all ValueSet Names containing: " + theNamePart + " in their name");
        
        LOG.info("Getting full list of ValueSets first");
        List<String> valSetList = FileCache.getResourceNameList(ResourceType.VALUESET);
        
        LOG.info("Now filtering the list to those matching our criteria");
        ArrayList<String> matches = new ArrayList<String>();
        
        String pattern = "(.*)" + theNamePart + "(.*)";
        
        for (String valSetName : valSetList) {
        	// Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(valSetName);
            if (m.find()) {
               matches.add(valSetName);
            }
        }
        LOG.info("Returning matches");
        return matches;
    }

    public List<String> getAllValueSetNames() {
        LOG.info("Getting all ValueSet Names");
        List<String> valSetList = FileCache.getResourceNameList(ResourceType.VALUESET);
        
        return valSetList;
    }
}
