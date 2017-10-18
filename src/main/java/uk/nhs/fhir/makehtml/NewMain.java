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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.FullFhirURL;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.util.UrlValidator;

/**
 * @author tim.coates@hscic.gov.uk
 */
public class NewMain {
    private static final Logger LOG = Logger.getLogger(NewMain.class.getName());

	// Set on startup. Path to folder containing extension files.
	private static String suppliedResourcesFolderPath = null;
	
	public static String getSuppliedResourcesFolderPath() {
		return suppliedResourcesFolderPath;
	}
    
    // force any RendererError errors to throw an exception and stop rendering
	public static final boolean STRICT = false;
	static {
		RendererError.STRICT = STRICT;
	}
	
	// convert any links with host fhir.hl7.org.uk into relative links
	public static final boolean FHIR_HL7_ORG_LINKS_LOCAL = true;
	static {
		FullFhirURL.FHIR_HL7_ORG_LINKS_LOCAL = FHIR_HL7_ORG_LINKS_LOCAL;
	}
	
	// send requests to linked external pages and check the response. If false, use cached values where necessary. 
	public static final boolean TEST_LINK_URLS = false;
	static {
		FullFhirURL.TEST_LINK_URLS = TEST_LINK_URLS;
	}
	
    private final Path inputDirectory;
    private final String outPath;
    private final String newBaseURL;
    
    public NewMain(Path inputDirectory, String outPath) {
    	this(inputDirectory, outPath, null);
    }
    
    public NewMain(Path inputDirectory, String outPath, String newBaseURL) {
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
            suppliedResourcesFolderPath = resourcesPath;
            
            if (!inputDir.endsWith(File.separator)) {
            	inputDir += File.separator;
            }
            if (!outputDir.endsWith(File.separator)) {
            	outputDir += File.separator;
            }
            
            NewMain instance = new NewMain(Paths.get(inputDir), outputDir, newBaseURL);
            
            instance.process();
        }
    }

    /**
     * Process a directory of Profile files.
     *
     * @param directoryPath
     */
    public void process() {
    	FhirFileRegistry fhirFileRegistry = new FhirResourceCollector(inputDirectory).collect();

        FileProcessor fileProcessor = new FileProcessor(fhirFileRegistry);
        try {
        	for (Map.Entry<File, WrappedResource<?>> e : fhirFileRegistry) {
	        	File sourceFile = e.getKey();
				WrappedResource<?> parsedResource = e.getValue();
				fileProcessor.processFile(outPath, newBaseURL, sourceFile, parsedResource);
	        }
	        
	        if (TEST_LINK_URLS) {
	        	new UrlValidator().testUrls(FhirURL.getLinkUrls());
	            UrlValidator.logSuccessAndFailures();
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
