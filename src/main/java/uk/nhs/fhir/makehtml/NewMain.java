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
package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.nhs.fhir.makehtml.data.FhirURL;
import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.makehtml.prep.ImplementationGuidePreparer;
import uk.nhs.fhir.makehtml.prep.OperationDefinitionPreparer;
import uk.nhs.fhir.makehtml.prep.StructureDefinitionPreparer;
import uk.nhs.fhir.makehtml.prep.ValueSetPreparer;
import uk.nhs.fhir.makehtml.render.ResourceBuilder;

/**
 * @author tim.coates@hscic.gov.uk
 */
public class NewMain {
    private static final String fileExtension = ".xml";
    private static final Logger LOG = Logger.getLogger(NewMain.class.getName());
    
    // force any RendererError errors to throw an exception and stop rendering
	public static final boolean STRICT = false;
	
	// convert any links with host fhir.hl7.org.uk into relative links
	public static final boolean FHIR_HL7_ORG_LINKS_LOCAL = true;
	
	// send requests to linked external pages and check the response. If false, use cached values where necessary. 
	public static final boolean TEST_LINK_URLS = false;

    private final File inputDirectory;
    private final String outPath;
    private final String newBaseURL;
    
    private NewMain(File inputDirectory, String outPath, String newBaseURL) {
    	this.inputDirectory = inputDirectory;
    	this.outPath = outPath;
    	this.newBaseURL = newBaseURL;
    }

    /**
     * Main entry point.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
    	if((args.length == 2) || (args.length == 3)) {
			String inputDir = args[0];
            String outputDir = args[1];
            String newBaseURL = null;
            if (args.length == 3) {
            	LOG.log(Level.INFO, "Using new base URL: " + newBaseURL);
            	newBaseURL = args[2];
            }

            String resourcesPath = args[0];
            if (!resourcesPath.endsWith(File.separator)) {
            	resourcesPath += File.separator;
            }
            FhirIcon.setSuppliedResourcesFolderPath(resourcesPath);
            
            if (!inputDir.endsWith(File.separator)) {
            	inputDir += File.separator;
            }
            if (!outputDir.endsWith(File.separator)) {
            	outputDir += File.separator;
            }
            
            NewMain instance = new NewMain(new File(inputDir), outputDir, newBaseURL);
            
            ResourceBuilder resourceBuilder =
            	new ResourceBuilder(
            		new StructureDefinitionPreparer(),
            		new ValueSetPreparer(),
            		new OperationDefinitionPreparer(),
            		new ImplementationGuidePreparer());	
            
            FileProcessor fileProcessor =
    	    	new FileProcessor(resourceBuilder);
            
            instance.process(fileProcessor);
        }
    }

    /**
     * Process a directory of Profile files.
     *
     * @param directoryPath
     */
    private void process(FileProcessor fileProcessor) {
    	
        File[] allProfiles = inputDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(fileExtension);
            }
        });

        try {
	        for (File thisFile : allProfiles) {
	        	fileProcessor.processFile(outPath, newBaseURL, inputDirectory, thisFile);
	        }
	        
	        if (TEST_LINK_URLS) {
	        	new UrlTester().testUrls(FhirURL.getLinkUrls());
	            UrlTester.logSuccessAndFailures();
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
