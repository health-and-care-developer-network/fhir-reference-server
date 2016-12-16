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
import java.util.logging.Logger;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.parser.DataFormatException;
import java.util.logging.Level;

public class FHIRUtils {

    /**
     * Constructor, never explicitly called, just sets the logging level to
     * that requested in config.properties
     */
    private FHIRUtils() {
        LOG.setLevel(Level.INFO);

        if(logLevel.equals("FINE")) {
            LOG.setLevel(Level.FINE);
        }
        if(logLevel.equals("OFF")) {
            LOG.setLevel(Level.OFF);
        }
    }

    private static final Logger LOG = Logger.getLogger(FHIRUtils.class.getName());

    private static FhirContext ctx = FhirContext.forDstu2();
    
    private static String profilePath = PropertyReader.getProperty("profilePath");
    private static String valueSetPath = PropertyReader.getProperty("valusetPath");
    private static String logLevel = PropertyReader.getProperty("logLevel");
    private static String examplesPath = PropertyReader.getProperty("examplesPath");

    /**
     * Method to load a StructureDefinition object from a specified filename.
     * 
     * @param filename Name of the file we want to load.
     * @return A StructureDefinition object (assuming file found) else null.
     */
    public static StructureDefinition loadProfileFromFile(final String filename) {
        return loadProfileFromFile(new File(profilePath + "/" + filename));
    }

    /**
     * Method to load a StructureDefinition object in from a specified File object.
     * @param file - File object
     * @return     - A StructureDefinition object
     */
    public static StructureDefinition loadProfileFromFile(final File file) {
        String resource = FileLoader.loadFile(file);
        StructureDefinition profile = null;
        try {
            profile = (StructureDefinition) ctx.newXmlParser().parseResource(resource);
            // Add an ID using the filename as the ID
            String id = FileLoader.removeFileExtension(file.getName());
            profile.setId(id);

        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (DataFormatException e) {
            e.printStackTrace();
        }
        LOG.info("Profile loaded - size: " + resource.length());
        return profile;
    }

    /**
     * Method to load a specified ValueSet from the file system.
     * 
     * @param filename Filename we're asking for.
     * @return A ValueSet (assuming it was found), else null.
     */
    public static ValueSet loadValueSetFromFile(String filename) {
        return loadValueSetFromFile(new File(valueSetPath + "/" + filename));
    }

    /**
     * Method to load a ValueSet file.
     * 
     * @param file File object pointing to the file we want to load
     * @return A ValueSet object
     */
    public static ValueSet loadValueSetFromFile(final File file) {
        String resource = FileLoader.loadFile(file);
        ValueSet vSet = null;
        try {
            vSet = (ValueSet) ctx.newXmlParser().parseResource(resource);
            // Add an ID using the filename as the ID
            String id = FileLoader.removeFileExtension(file.getName());
            vSet.setId(id);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (DataFormatException e) {
            e.printStackTrace();
        }
        LOG.info("ValueSet loaded - size: " + resource.length());
        return vSet;
    }

}
