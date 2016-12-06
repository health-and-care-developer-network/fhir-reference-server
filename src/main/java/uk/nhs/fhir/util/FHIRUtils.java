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
import ca.uhn.fhir.parser.DataFormatException;

public class FHIRUtils {
	private static final Logger LOG = Logger.getLogger(FHIRUtils.class.getName());
	
	private static FhirContext ctx = FhirContext.forDstu2();
	private static String profilePath = PropertyReader.getProperty("profilePath");
    private static String examplesPath = PropertyReader.getProperty("examplesPath");
	
	
	public static StructureDefinition loadProfileFromFile(final String filename) {
        return loadProfileFromFile(new File(profilePath + "/" + filename));
    }
	
	public static StructureDefinition loadProfileFromFile(final File file) {
		String resource = FileLoader.loadFile(file);
		//System.out.println("FILE: " + resource);
        StructureDefinition profile = null;
        try {
        	profile =
        			(StructureDefinition) ctx.newXmlParser().parseResource(resource);
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
}
