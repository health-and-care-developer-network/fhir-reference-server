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

import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.makehtml.prep.ImplementationGuidePreparer;
import uk.nhs.fhir.makehtml.prep.OperationDefinitionPreparer;
import uk.nhs.fhir.makehtml.prep.StructureDefinitionPreparer;
import uk.nhs.fhir.makehtml.prep.ValueSetPreparer;

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class NewMain {
    private static final String fileExtension = ".xml";
    private static final Logger LOG = Logger.getLogger(NewMain.class.getName());
	public static final boolean STRICT = false;

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
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
