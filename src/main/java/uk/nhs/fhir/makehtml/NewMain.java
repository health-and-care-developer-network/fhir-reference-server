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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.FullFhirURL;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.error.FhirErrorHandler;
import uk.nhs.fhir.makehtml.render.RendererContext;
import uk.nhs.fhir.util.FhirFileUtils;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.UrlValidator;

/**
 * @author tim.coates@hscic.gov.uk
 */
public class NewMain {
    private static final Logger LOG = LoggerFactory.getLogger(NewMain.class.getName());
    
    // force any RendererError errors to throw an exception and stop rendering
	public static final boolean STRICT = false;
	static {
		RendererErrorConfig.STRICT = STRICT;
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
	
	private final RendererFileLocator rendererFileLocator;
    private final String newBaseURL;
    private final FhirErrorHandler errorHandler;
    private boolean continueOnFail = false;
    
    public void setContinueOnFail(boolean continueOnFail) {
    	this.continueOnFail = continueOnFail;
    }

	public NewMain(Path inputDirectory, Path outputDirectory, FhirErrorHandler errorHandler) {
		this(inputDirectory, outputDirectory, null, errorHandler);
	}
    
	public NewMain(Path inputDirectory, Path outPath, String newBaseURL, FhirErrorHandler errorHandler) {
		this.rendererFileLocator = new DefaultRendererFileLocator(inputDirectory, makeRenderedArtefactTempDirectory(), outPath);
		this.newBaseURL = newBaseURL;
		this.errorHandler = errorHandler;
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
            	LOG.info("Using new base URL: " + newBaseURL);
            	newBaseURL = args[2];
            }
            
            NewMain instance = new NewMain(Paths.get(inputDir), Paths.get(outputDir), newBaseURL, new LoggingErrorHandler());
            instance.process();
        }
    }

	static Path makeRenderedArtefactTempDirectory() {
		try {
			return FhirFileUtils.makeTempDir("fhir-renderer-tmp-" + System.currentTimeMillis(), true);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

    /**
     * Process a directory of Profile files.
     *
     * @param directoryPath
     */
    public void process() {
    	Path rawArtefactDirectory = rendererFileLocator.getRawArtefactDirectory();
    	LOG.info("Finding resources in " + rawArtefactDirectory.toString());
		FhirFileRegistry fhirFileRegistry = new FhirResourceCollector(rawArtefactDirectory).collect();
		RendererContext context = new RendererContext(fhirFileRegistry, errorHandler);
		
        FileProcessor fileProcessor = new FileProcessor(context);
        try {
        	for (Map.Entry<File, WrappedResource<?>> e : fhirFileRegistry) {

	        	context.setCurrentSource(e.getKey());
				context.setCurrentParsedResource(e.getValue());
				
        		try {
					fileProcessor.processFile(rendererFileLocator, newBaseURL);
        		} catch (Exception error) {
        			// If we have an event handler, we can carry on
        			errorHandler.error(Optional.empty(), Optional.of(error));
        			if (!continueOnFail) {
        				break;
        			} else {
        				throw error;
        			}
        		}

        		context.setCurrentSource(null);
        		context.setCurrentParsedResource(null);
	        }

    		boolean succeeded = !errorHandler.foundErrors();
    		
    		if (!succeeded) {
        		LOG.info("Rendering failed, displaying event messages");
        	} 
    		
    		errorHandler.displayOutstandingEvents();
    		
        	if (succeeded) {
        		LOG.info("Rendering succeeded, copying rendered artefacts");
        		copyExamples(fhirFileRegistry);
        		copyGeneratedArtefacts();
        	}
        	
        	// if there is an error while copying the files, this gets skipped so they can be recovered if necessary
        	LOG.info("Deleting temporary files");
        	deleteTempFiles();
        	
        } catch (Exception e) {
        	throw new IllegalStateException("Renderer failed", e);
        }
        
        if (TEST_LINK_URLS) {
        	new UrlValidator().testUrls(FhirURL.getLinkUrls());
            UrlValidator.logSuccessAndFailures();
        }
    }

	private void copyExamples(FhirFileRegistry fhirFileRegistry) {
		
		for (Map.Entry<File, IBaseResource> entry : fhirFileRegistry.getUnsupportedFhirResources().entrySet()) {

			String className = entry.getValue().getClass().getName();
			
			FhirVersion exampleVersion = null;
			if (className.contains("dstu2")) {
				exampleVersion = FhirVersion.DSTU2;
			} else if (className.contains("stu3")) {
				exampleVersion = FhirVersion.STU3;
			} else {
				LOG.error("Don't know what FHIR version a " + className + " resource is.");
			}
			
			if (exampleVersion != null) {
				
				Path outputDir = 
					rendererFileLocator.getRenderingTempOutputDirectory()
						.resolve(exampleVersion.toString())
						.resolve("Examples");
				
				outputDir.toFile().mkdirs();
				
				Path output = outputDir.resolve(entry.getKey().getName());
		    	
				LOG.debug("Copying example file to " + output.toString());
		    	
				try {
					FileUtils.copyFile(entry.getKey(), output.toFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void copyGeneratedArtefacts() throws IOException {
		Path generationTempDirectory = rendererFileLocator.getRenderingTempOutputDirectory();
		Path outputDirectory = rendererFileLocator.getRenderingFinalOutputDirectory();
		
		FileUtils.copyDirectory(generationTempDirectory.toFile(), outputDirectory.toFile());
	}

	private void deleteTempFiles() {
		File tempDir = rendererFileLocator.getRenderingTempOutputDirectory().toFile();
		
		try {
			FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
