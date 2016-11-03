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

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.parser.DataFormatException;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PropertyReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tim Coates
 */
public class FilesystemIF implements Datasource {
    private static final Logger LOG = Logger.getLogger(FilesystemIF.class.getName());

    private String profilePath = PropertyReader.getProperty("profilePath");
    private String examplesPath = PropertyReader.getProperty("examplesPath");
    

    /**
     * Gets a specific one
     * @param name
     * @return 
     */
    public StructureDefinition getSingleStructureDefinitionByName(String name) {
    	String filename = FileLoader.cleanFilename(name);
    	LOG.info("Getting StructureDefinitions with name=" + name +
        			" looking for file: " + profilePath + "/" + filename);
        
    	StructureDefinition foundDocRef = FHIRResourceHandler.loadProfileFromFile(filename);
        return foundDocRef;
    }

    /**
     * This is the method to do a search based on name, ie to find where
     * name:contains=[parameter]
     * 
     * @param theNamePart
     * @return 
     */
    public List<StructureDefinition> getMatchByName(String theNamePart) {
        LOG.info("Getting StructureDefinitions with name=" + theNamePart);
        List<StructureDefinition> list = new ArrayList<StructureDefinition>();
        return list;
    }

    /**
     * Gets a full list of StructureDefinition objects. Not especially performant, and
     * could certainly be cached in memory to improve performance and reduce disk io.
     * 
     * @return 
     */
    public List<StructureDefinition> getAll() {
        LOG.info("Getting all StructureDefinitions");
        return FileCache.getProfiles();
    }
    
    /**
     * Gets a full list of names for the web view of /StructureDefinition requests.
     * 
     * @return 
     */
    public List<String> getAllNames() {
        LOG.info("Getting all StructureDefinition Names");
        return FileCache.getNameList();
    }


    /**
     * This is the method to search by name, e.g. name:contains=Patient
     * 
     * @param theNamePart
     * @return 
     */
    public List<String> getAllNames(String theNamePart) {
        LOG.info("Getting all StructureDefinition Names containing: " + theNamePart + " in their name");
        
        List<String> profileList = FileCache.getNameList();
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
        return matches;
    }
}
