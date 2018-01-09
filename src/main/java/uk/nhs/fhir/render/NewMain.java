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
package uk.nhs.fhir.render;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.FullFhirURL;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.event.AbstractRendererEventHandler;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.LoggedRenderingException;
import uk.nhs.fhir.event.RendererLoggingEventHandler;
import uk.nhs.fhir.load.FhirFileParser;
import uk.nhs.fhir.load.RootedXmlFileFinder;
import uk.nhs.fhir.util.FhirFileRegistry;
import uk.nhs.fhir.util.FhirFileUtils;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.UrlValidator;

/**
 * @author tim.coates@hscic.gov.uk
 */
public class NewMain {
    private static final Logger LOG = LoggerFactory.getLogger(NewMain.class.getName());
	
	private final RendererFileLocator rendererFileLocator;
    private final String newBaseURL;
    private final AbstractRendererEventHandler eventHandler;
    private boolean continueOnFail = false;
    private boolean allowCopyOnError = false;
    
    public void setContinueOnFail(boolean continueOnFail) {
    	this.continueOnFail = continueOnFail;
    }
    
    public void setAllowCopyOnError(boolean allowCopyOnError) {
    	this.allowCopyOnError = allowCopyOnError;
    }

	public NewMain(Path inputDirectory, Path outputDirectory, AbstractRendererEventHandler errorHandler) {
		this(inputDirectory, outputDirectory, null, errorHandler);
	}
    
	public NewMain(Path inputDirectory, Path outPath, String newBaseURL, AbstractRendererEventHandler errorHandler) {
		this.rendererFileLocator = new DefaultRendererFileLocator(inputDirectory, makeRenderedArtefactTempDirectory(), outPath);
		this.newBaseURL = newBaseURL;
		this.eventHandler = errorHandler;
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
            	newBaseURL = args[2];
            	LOG.info("Using new base URL: " + newBaseURL);
            }
            
            NewMain instance = new NewMain(Paths.get(inputDir), Paths.get(outputDir), newBaseURL, new RendererLoggingEventHandler());
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

    	FhirFileRegistry fhirFileRegistry = new FhirFileRegistry();
		RendererContext rendererContext = RendererContext.forThread();
		rendererContext.setFhirFileRegistry(fhirFileRegistry);
    	EventHandlerContext.setForThread(eventHandler);
		List<File> potentialFhirFiles = new RootedXmlFileFinder(rawArtefactDirectory).findFilesRecursively();
    	
		FhirFileParser parser = new FhirFileParser();
		
		for (File potentialFhirFile : potentialFhirFiles) {
			rendererContext.setCurrentSource(potentialFhirFile);
			rendererContext.setCurrentParsedResource(Optional.empty());

			IBaseResource parsedFile;
			try {
				parsedFile = parser.parseFile(potentialFhirFile);
			} catch (Exception e) {
				eventHandler.log("Skipping file " + potentialFhirFile.getAbsolutePath() + " - HAPI parsing failed - " + e.getMessage(), Optional.of(e));
				continue;
			}

			try {
				WrappedResource<?> wrappedResource = WrappedResource.fromBaseResource(parsedFile);
				rendererContext.setCurrentParsedResource(Optional.of(wrappedResource));
			} catch (Exception e) {
				eventHandler.ignore("Failed to create WrappedResource from " + potentialFhirFile.getPath(), Optional.of(e));
				// if wrapping failed, leave 'current parsed resource' as null
			}

			try {
				fhirFileRegistry.register(potentialFhirFile, parsedFile);
			} catch (Exception e) {
				try {
					eventHandler.error(Optional.of("Error adding file " + potentialFhirFile.getAbsolutePath() + " to registry"), Optional.of(e));
				} catch (LoggedRenderingException lre) {}
			}
		}
    	
        FileProcessor fileProcessor = new FileProcessor();
        try {
        	for (Map.Entry<File, WrappedResource<?>> e : fhirFileRegistry) {
        		rendererContext.setCurrentSource(e.getKey());
        		rendererContext.setCurrentParsedResource(Optional.of(e.getValue()));
				
        		boolean causedException = false;
        		
        		try {
        			try {
						fileProcessor.processFile(rendererFileLocator, newBaseURL);
	        		} catch (LoggedRenderingException loggedError) {
	        			// Already passed to the event handler - just rethrow
	        			throw loggedError;
	        		} catch (Exception error) {
	        			// Needs to be passed to the event handler so that it can be logged.
	        			eventHandler.error(Optional.empty(), Optional.of(error));
	        		}
        		} catch (LoggedRenderingException loggedError) {
        			causedException = true;
        		} 
        			
        		
    			if (causedException 
    			  && !continueOnFail) {
    				break;
    			}

        		rendererContext.clearCurrent();
	        }

    		boolean succeeded = !eventHandler.foundErrors();
    		
        	if (succeeded || allowCopyOnError) {
        		if (succeeded) {
        			LOG.info("Rendering succeeded, copying rendered artefacts");
        		} else if (allowCopyOnError) {
        			LOG.info("Rendering failed for some files - copying rendered artefacts anyway since allowCopyOnError set");
        		}
        		
        		copyExamples(fhirFileRegistry);
        		copyGeneratedArtefacts();
        	} 
        	
        	if (!succeeded || eventHandler.foundWarnings()) {
        		LOG.info("Displaying event messages");
        		
        		eventHandler.displayOutstandingEvents();
        	}
        	
        	// if there is an error while copying the files, this gets skipped so they can be recovered if necessary
        	LOG.info("Deleting temporary files");
        	deleteTempFiles();
        	
        } catch (Exception e) {
        	throw new IllegalStateException("Renderer failed", e);
        }
        
        if (FullFhirURL.TEST_LINK_URLS) {
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
				
				File outputDirFile = outputDir.toFile();
				if (!outputDirFile.exists() && !outputDirFile.mkdirs()) {
		        	throw new IllegalStateException("Failed to create directory [" + outputDir.toString() + "]");
		        }
				
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
