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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.FullFhirURL;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.util.FhirFileUtils;
import uk.nhs.fhir.util.UrlValidator;

/**
 * @author tim.coates@hscic.gov.uk
 */
public class NewMain {
    private static final Logger LOG = LoggerFactory.getLogger(NewMain.class.getName());
    
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
	
	private final RendererFileLocator rendererFileLocator;
    private final String newBaseURL;
    private final RendererErrorHandler errorHandler;

	public NewMain(Path inputDirectory, Path outputDirectory) {
		this(inputDirectory, makeRenderedArtefactTempDirectory(), outputDirectory, null);
	}

	public NewMain(Path inputDirectory, Path outputDirectory, String newBaseURL) {
		this(inputDirectory, makeRenderedArtefactTempDirectory(), outputDirectory, newBaseURL);
	}
    
    public NewMain(Path inputDirectory, Path tempDirectory, Path outPath, String newBaseURL) {
    	this(new DefaultRendererFileLocator(inputDirectory, tempDirectory, outPath), newBaseURL);
    }

    public NewMain(RendererFileLocator renderingFileLocator) {
		this(renderingFileLocator, null, null);
	}

	public NewMain(RendererFileLocator renderingFileLocator, String newBaseURL) {
		this(renderingFileLocator, newBaseURL, null);
	}
	
	public NewMain(RendererFileLocator renderingFileLocator, RendererErrorHandler errorHandler) {
		this(renderingFileLocator, null, errorHandler);
	}

	public NewMain(RendererFileLocator renderingFileLocator, String newBaseURL, RendererErrorHandler errorHandler) {
		this.rendererFileLocator = renderingFileLocator;
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
            
            NewMain instance = new NewMain(Paths.get(inputDir), Paths.get(outputDir), newBaseURL);
            instance.process();
        }
    }

	static Path makeRenderedArtefactTempDirectory() {
		Path tempDirectory;
		try {
			tempDirectory = FhirFileUtils.makeTempDir("fhir-renderer-tmp", true);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return tempDirectory;
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

        FileProcessor fileProcessor = new FileProcessor(fhirFileRegistry);
        try {
        	for (Map.Entry<File, WrappedResource<?>> e : fhirFileRegistry) {
        		try {
		        	File sourceFile = e.getKey();
					WrappedResource<?> parsedResource = e.getValue();
					fileProcessor.processFile(rendererFileLocator, newBaseURL, sourceFile, parsedResource);
        		} catch (Exception error) {
        			// If we have an error handler, we can carry on
        			if (errorHandler == null) {
        				throw error;
        			} else {
        				errorHandler.recordError(e.getKey(), e.getValue(), error);
        			}
        		}
	        }
        	
        	if (errorHandler != null
        	  && errorHandler.foundErrors()) {
        		LOG.info("Rendering failed, displaying error messages");
        		errorHandler.displayErrors();
        	} else {
        		LOG.info("Rendering succeeded, copying rendered artefacts");
        		copyGeneratedArtefacts();
        	}
        	
        	// if there is an error copying the files, this gets skipped so they can be recovered if necessary
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
