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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * @param name
     * @return 
     */
    public StructureDefinition getSingleStructureDefinitionByName(String name) {
    	ResourceEntity entry = FileCache.getSingleResourceByName(name);
    	File path = entry.getResourceFile();
    	LOG.info("Getting StructureDefinition with name=" + name + " looking for file: " + path.getAbsolutePath());
        
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
    public List<StructureDefinition> getStructureDefinitionMatchByName(String theNamePart) {
        LOG.info("Getting StructureDefinitions with name=" + theNamePart);
        List<StructureDefinition> list = new ArrayList<StructureDefinition>();
        List<String> matchingNames = getAllStructureDefinitionNames(theNamePart);

        for(String name : matchingNames) {
            list.add(getSingleStructureDefinitionByName(name));
        }
        return list;
    }

    /**
     * Gets a full list of StructureDefinition objects. Not especially performant.
     * 
     * @return 
     */
    public List<StructureDefinition> getAllStructureDefinitions() {
        LOG.info("Getting all StructureDefinitions");
        return FileCache.getResources(ResourceType.STRUCTUREDEFINITION);
    }
    
    /**
     * Gets a full list of names for the web view of /StructureDefinition requests.
     * 
     * @return 
     */
    public List<String> getAllStructureDefinitionNames() {
        LOG.info("Getting all StructureDefinition Names");
        return FileCache.getResourceNameList(ResourceType.STRUCTUREDEFINITION);
    }
    
    /**
     * Gets a full list of names grouped by base resource for the web view 
     * of /StructureDefinition requests.
     * 
     * @return 
     */
    public HashMap<String, List<ResourceEntity>> getAllStructureDefinitionNamesByBaseResource() {
        LOG.info("Getting all StructureDefinition Names by base resource");
        return FileCache.getGroupedNameList(ResourceType.STRUCTUREDEFINITION);
    }
    
    /**
     * Gets a full list of valueset names grouped by the broad category of the valueset
     * for the web view of /ValueSet requests.
     */
    @Override
	public HashMap<String, List<ResourceEntity>> getAllValueSetNamesByCategory() {
    	LOG.info("Getting all ValueSet Names by category");
        return FileCache.getGroupedNameList(ResourceType.VALUESET);
	}
    

    /**
     * This is the method to search by name, e.g. name:contains=Patient
     * 
     * @param theNamePart
     * @return 
     */
    public List<String> getAllStructureDefinitionNames(String theNamePart) {
        LOG.info("Getting all StructureDefinition Names containing: " + theNamePart + " in their name");
        
        LOG.info("Getting full list of profiles first");
        List<String> profileList = FileCache.getResourceNameList(ResourceType.STRUCTUREDEFINITION);
        
        LOG.info("Now filtering the list to those matching our criteria");
        ArrayList<String> matches = new ArrayList<String>();
        
        String pattern = "(.*)" + theNamePart + "(.*)";
        
        for (String profileName : profileList) {
        	// Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(profileName);
            if (m.find()) {
               matches.add(profileName);
            }
        }
        LOG.info("Returning matches");
        return matches;
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

    @Override
    public List<ValueSet> getAllValueSets() {
    	return FileCache.getResources(ResourceType.VALUESET);
    }

    
    
    
	@Override
	public OperationDefinition getSingleOperationDefinitionByName(String name) {
    	ResourceEntity entry = FileCache.getSingleResourceByName(name);
    	File path = entry.getResourceFile();
    	LOG.info("Getting Operation with name=" + name + " looking for file: " + path.getAbsolutePath());
        
    	OperationDefinition foundOperation = (OperationDefinition)FHIRUtils.loadResourceFromFile(path);
        return foundOperation;
	}

	@Override
	public List<OperationDefinition> getAllOperations() {
		return FileCache.getResources(ResourceType.OPERATIONDEFINITION);
	}

	@Override
	public List<String> getAllOperationNames() {
        LOG.info("Getting all Operation Names");
        List<String> operationList = FileCache.getResourceNameList(ResourceType.OPERATIONDEFINITION);
        return operationList;
	}

	@Override
	public HashMap<String, List<ResourceEntity>> getAllOperationNamesByCategory() {
    	LOG.info("Getting all Operation Names by category");
        return FileCache.getGroupedNameList(ResourceType.OPERATIONDEFINITION);
	}

}
