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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.dstu2.resource.ValueSet.ComposeInclude;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PropertyReader;
import static uk.nhs.fhir.enums.ResourceType.*;

/**
 * Holds an in-memory cache of the profiles from the filesystem, as well as the list of profile names.
 *
 * @author Adam Hatherly
 */
public class FileCache {
    private static final Logger LOG = Logger.getLogger(FileCache.class.getName());

    // Singleton object to act as a cache of the files in the profiles and valueset directories
    private static List<ResourceEntity> profileFileList = null;
    private static List<ResourceEntity> ValueSetFileList = null;

    private static long lastUpdated = 0;
    private static long updateInterval = Long.parseLong(PropertyReader.getProperty("cacheReloadIntervalMS"));
    private static String profilePath = PropertyReader.getProperty("profilePath");
    private static String valueSetPath = PropertyReader.getProperty("valusetPath");
    private static String fileExtension = PropertyReader.getProperty("fileExtension");

    
//<editor-fold defaultstate="collapsed" desc="Methods to get cached StructureDefinitions and their names">
    
    /**
     * Method to get the cached set of StructureDefinitions
     * 
     * @return 
     */
    /*public static List<StructureDefinition> getProfiles() {
        if(updateRequired()) {
            updateCache();
        }
        return profileList;
    }*/
    
    /**
     * Method to get the cached set of StructureDefinition names
     * 
     * @return 
     */
    public static List<String> getStructureDefinitionNameList() {
        if(updateRequired()) {
            updateCache();
        }
        ArrayList<String> names = new ArrayList<String>();
        for(ResourceEntity sd : profileFileList) {
        	names.add(sd.getResourceName());
        }
        return names;
    }

    /**
     * Method to get the cached set of StructureDefinition names
     * 
     * @return 
     */
    public static List<String> getValueSetNameList() {
        if(updateRequired()) {
            updateCache();
        }
        ArrayList<String> names = new ArrayList<String>();
        for(ResourceEntity vs : ValueSetFileList) {
        	names.add(vs.getResourceName());
        }
        return names;
    }
    
    public static HashMap<String, List<ResourceEntity>> getGroupedNameList() {
        if(updateRequired()) {
            updateCache();
        }
        
        LOG.info("Creating HashMap");
        HashMap<String, List<ResourceEntity>> result = new HashMap<String, List<ResourceEntity>>();
        try {
            for(ResourceEntity sd : profileFileList) {
                boolean isExtension = sd.isExtension();
                //TODO: Show extensions differently?
                String group = sd.getDisplayGroup();
                String name = sd.getResourceName();
                if(result.containsKey(group)) {
                    List<ResourceEntity> entry = result.get(group);
                    entry.add(sd);
                } else {
                    List<ResourceEntity> entry = new ArrayList<ResourceEntity>();
                    entry.add(sd);
                    result.put(group, entry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("ERROR: " + e.getMessage());
        }
        LOG.info("Generated HashMap: " + result.toString());
        return result;
    }
    
    public static HashMap<String, List<ResourceEntity>> getGroupedValueSetNameList() {
        if(updateRequired()) {
            updateCache();
        }
        
        LOG.info("Creating HashMap");
        HashMap<String, List<ResourceEntity>> result = new HashMap<String, List<ResourceEntity>>();
        try {
            for(ResourceEntity vs : ValueSetFileList) {
            	String category = vs.getDisplayGroup();
            	String name = vs.getResourceName();
            	
            	if(result.containsKey(category)) {
                    List<ResourceEntity> entry = result.get(category);
                    entry.add(vs);
                } else {
                    List<ResourceEntity> entry = new ArrayList<ResourceEntity>();
                    entry.add(vs);
                    result.put(category, entry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("ERROR: " + e.getMessage());
        }
        LOG.info("Generated HashMap: " + result.toString());
        return result;
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Methods to get cached ValueSets and their names">
    
    /**
     * Method to get all of the ValueSets
     * 
     * @return 
     */
    public static List<ValueSet> getValueSets() {
        if(updateRequired()) {
            updateCache();
        }
        // Load each resource file and put them in a list to return
        ArrayList<ValueSet> allFiles = new ArrayList<ValueSet>();
        for (ResourceEntity entry : ValueSetFileList) {
        	ValueSet vs = FHIRUtils.loadValueSetFromFile(entry.getResourceFile());
        	allFiles.add(vs);
        }
        return allFiles;
    }
    
    /**
     * Method to get all of the StructureDefinitions
     * 
     * @return 
     */
    public static List<StructureDefinition> getProfiles() {
        if(updateRequired()) {
            updateCache();
        }
        // Load each resource file and put them in a list to return
        ArrayList<StructureDefinition> allFiles = new ArrayList<StructureDefinition>();
        for (ResourceEntity entry : profileFileList) {
        	StructureDefinition sd = FHIRUtils.loadProfileFromFile(entry.getResourceFile());
        	allFiles.add(sd);
        }
        return allFiles;
    }
    
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Updating section is in here">
    private static boolean updateRequired() {
        long currentTime = System.currentTimeMillis();
        if(profileFileList == null || (currentTime > (lastUpdated + updateInterval))) {
            LOG.fine("Cache needs updating");
            return true;
        }
        LOG.info("Using Cache");
        return false;
    }
    
    private synchronized static void updateCache() {
        if(updateRequired()) {
            lastUpdated = System.currentTimeMillis();
            LOG.fine("Updating cache from filesystem");
            
            //profileList = cacheProfileFiles();
            profileFileList = cacheFHIRResources(profilePath, STRUCTUREDEFINITION);
            
            //ValueSetList = cacheValueSetFiles();
            ValueSetFileList = cacheFHIRResources(valueSetPath, VALUESET);
        }
    }
    
    
    private static ArrayList<ResourceEntity> cacheFHIRResources(String path, ResourceType resourceType){
        ArrayList<ResourceEntity> newFileList = new ArrayList<ResourceEntity>();
        File folder = new File(path);
            File[] fileList = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(fileExtension);
                }
            });
            
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.fine("Reading " + resourceType + " ResourceEntity into cache: " + thisFile.getName());
                
                String name = null;
                String actualName = null;
                boolean extension = false;
                String baseType = "Other";
                String displayGroup = null;
                boolean example = false;
                
                try {
	                if (resourceType == STRUCTUREDEFINITION) {
	                	StructureDefinition profile = FHIRUtils.loadProfileFromFile(thisFile);
	                	name = profile.getName();
	                	extension = (profile.getBase().equals("http://hl7.org/fhir/StructureDefinition/Extension"));
	                    baseType = profile.getConstrainedType();
	                    actualName = getActualNameFromURL(profile.getUrl(), name);
	                    displayGroup = baseType;
	                } else if (resourceType == VALUESET) {
	                	displayGroup = "Code List";
	                	ValueSet profile = FHIRUtils.loadValueSetFromFile(thisFile);
	                	name = profile.getName();
	                	actualName = getActualNameFromURL(profile.getUrl(), name);
	                	if (FHIRUtils.isValueSetSNOMED(profile)) {
	                		displayGroup = "SNOMED CT Code List";
	                	}
	                }
	                newFileList.add(new ResourceEntity(name, thisFile, resourceType, extension, baseType,
	                										displayGroup, example, actualName));
                } catch (Exception ex) {
                	LOG.severe("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
                }
            }
        }
        
        // Sort our collection into alpha order by resource name
        Collections.sort(newFileList);
        
        return newFileList;
    }
    
    private static String getActualNameFromURL(String url, String def) {
    	// Find the actual name of the resource from the URL
        int idx = url.lastIndexOf('/');
        if (idx > -1) {
        	return url.substring(idx+1);
        } else {
        	// Can't find a real name in the URL!
        	return def;
        }
    }
    
    
    /*
    private static ArrayList<StructureDefinition> cacheProfileFiles() {
        ArrayList<StructureDefinition> newProfileList = new ArrayList<StructureDefinition>();
        File folder = new File(profilePath);
        File[] files = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(fileExtension);
            }
        });

        for(int i = 0; i < files.length; i++) {
            if(files[i].isFile()) {
                LOG.fine("Reading profile file into cache: " + files[i].getName());

                // Add the profile itself
                StructureDefinition profile = FHIRUtils.loadProfileFromFile(files[i]);
                newProfileList.add(profile);
            }
        }
        return newProfileList;
    }*/

    /*
    private static ArrayList<ValueSet> cacheValueSetFiles() {
        ArrayList<ValueSet> newProfileList = new ArrayList<ValueSet>();
        File folder = new File(valueSetPath);
        File[] files = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(fileExtension);
            }
        });

        for(int i = 0; i < files.length; i++) {
            if(files[i].isFile()) {
                LOG.fine("Reading profile file into cache: " + files[i].getName());

                // Add the profile itself
                ValueSet profile = FHIRUtils.loadValueSetFromFile(files[i]);
                newProfileList.add(profile);
            }
        }
        return newProfileList;
    }
    */
    public static ResourceEntity getSingleValueSetByName(String name) {
    	for (ResourceEntity vs : ValueSetFileList) {
    		if (vs.getResourceName().equals(name) || vs.getActualResourceName().equals(name)) {
    			return vs;
    		}
    	}
    	return null;
    }

    public static ResourceEntity getSingleProfileByName(String name) {
    	for (ResourceEntity entry : profileFileList) {
    		if (entry.getResourceName().equals(name) || entry.getActualResourceName().equals(name)) {
    			return entry;
    		}
    	}
    	return null;
    }

    
//</editor-fold>
    

}
