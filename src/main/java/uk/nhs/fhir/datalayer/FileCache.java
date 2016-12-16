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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PropertyReader;

/**
 * Holds an in-memory cache of the profiles from the filesystem, as well as the list of profile names.
 *
 * @author Adam Hatherly
 */
public class FileCache {
    private static final Logger LOG = Logger.getLogger(FileCache.class.getName());

    // Singleton object to act as a cache of the files in the profiles and valueset directories
    private static List<String> profileFileList = null;
    private static List<String> ValueSetFileList = null;
    private static List<StructureDefinition> profileList = null;
    private static List<ValueSet> ValueSetList = null;

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
    public static List<StructureDefinition> getProfiles() {
        if(updateRequired()) {
            updateCache();
        }
        return profileList;
    }
    
    /**
     * Method to get the cached set of StructureDefinition names
     * 
     * @return 
     */
    public static List<String> getStructureDefinitionNameList() {
        if(updateRequired()) {
            updateCache();
        }
        return profileFileList;
    }
    
    public static HashMap<String, List<String>> getGroupedNameList() {
        if(updateRequired()) {
            updateCache();
        }
        
        LOG.info("Creating HashMap");
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        try {
            for(StructureDefinition sd : profileList) {
                String base = sd.getConstrainedType();
                String name = sd.getName();
                if(result.containsKey(base)) {
                    List<String> entry = result.get(base);
                    entry.add(name);
                } else {
                    List<String> entry = new ArrayList<String>();
                    entry.add(name);
                    result.put(base, entry);
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
     * Method to get all of the cached ValueSets
     * 
     * @return 
     */
    public static List<ValueSet> getValueSets() {
        if(updateRequired()) {
            updateCache();
        }
        return ValueSetList;
    }
    
    /**
     * Method to gte the cached list of ValueSet names
     * 
     * @return 
     */
    static List<String> getValueSetNameList() {
        if(updateRequired()) {
            updateCache();
        }
        return ValueSetFileList;
    }
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Updating section is in here">
    private static boolean updateRequired() {
        long currentTime = System.currentTimeMillis();
        if(profileList == null || (currentTime > (lastUpdated + updateInterval))) {
            LOG.info("Cache needs updating");
            return true;
        }
        LOG.info("Using Cache");
        return false;
    }
    
    private synchronized static void updateCache() {
        if(updateRequired()) {
            lastUpdated = System.currentTimeMillis();
            LOG.info("Updating cache from fliesystem");
            ArrayList<String> newFileList = new ArrayList<String>();
            ArrayList<StructureDefinition> newProfileList = new ArrayList<StructureDefinition>();
            File folder = new File(profilePath);
            File[] files = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(fileExtension);
                }
            });
            
            for(int i = 0; i < files.length; i++) {
                if(files[i].isFile()) {
                    LOG.info("Reading profile file into cache: " + files[i].getName());
                    
                    // Add it to the name list
                    String name = files[i].getName();
                    newFileList.add(FileLoader.removeFileExtension(name));
                    
                    // Add the profile itself
                    StructureDefinition profile = FHIRUtils.loadProfileFromFile(files[i]);
                    newProfileList.add(profile);
                }
            }
            
            profileList = newProfileList;
            profileFileList = newFileList;
            
            newFileList = new ArrayList<String>();
            ArrayList<ValueSet> newValueSetList = new ArrayList<ValueSet>();
            folder = new File(valueSetPath);
            files = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(fileExtension);
                }
            });
            
            for(int i = 0; i < files.length; i++) {
                if(files[i].isFile()) {
                    LOG.info("Reading ValueSet file into cache: " + files[i].getName());
                    
                    // Add it to the name list
                    String name = files[i].getName();
                    newFileList.add(FileLoader.removeFileExtension(name));
                    
                    // Add the profile itself
                    ValueSet valSet = FHIRUtils.loadValueSetFromFile(files[i]);
                    newValueSetList.add(valSet);
                }
            }
            
            ValueSetList = newValueSetList;
            ValueSetFileList = newFileList;
        }
    }
//</editor-fold>
    

}
