package uk.nhs.fhir.render;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.FullFhirURL;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.event.AbstractRendererEventHandler;
import uk.nhs.fhir.event.EventHandler;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.LoggedRenderingException;
import uk.nhs.fhir.event.RendererLoggingEventHandler;
import uk.nhs.fhir.load.FhirFileParser;
import uk.nhs.fhir.load.RootedXmlFileFinder;
import uk.nhs.fhir.util.FhirFileRegistry;
import uk.nhs.fhir.util.FhirFileUtils;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.UrlValidator;

public class FhirProfileRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(FhirProfileRenderer.class.getName());
	
	private final RendererFileLocator rendererFileLocator;
    private final Optional<String> newBaseURL;
    private final Set<String> permittedMissingExtensionPrefixes;
    private final AbstractRendererEventHandler eventHandler;
    private boolean continueOnFail = false;
    private boolean allowCopyOnError = false;
    private final Optional<Set<String>> localQdomains;
<<<<<<< HEAD:profile-renderer-lib/src/main/java/uk/nhs/fhir/render/FhirProfileRenderer.java

=======
    private final Optional<String> repositoryName;
    private final Optional<String> repositoryBranch;
    private final Optional<String> httpCacheDirectory;
    
>>>>>>> feature/git-history-view:src/main/java/uk/nhs/fhir/render/NewMain.java
    public void setContinueOnFail(boolean continueOnFail) {
    	this.continueOnFail = continueOnFail;
    }
    
    public void setAllowCopyOnError(boolean allowCopyOnError) {
    	this.allowCopyOnError = allowCopyOnError;
    }

<<<<<<< HEAD:profile-renderer-lib/src/main/java/uk/nhs/fhir/render/FhirProfileRenderer.java
	public FhirProfileRenderer(Path inputDirectory, Path outputDirectory, Optional<Set<String>> permittedMissingExtensionPrefixes, AbstractRendererEventHandler errorHandler) {
		this(inputDirectory, outputDirectory, Optional.empty(), permittedMissingExtensionPrefixes, errorHandler, Optional.empty());
=======
	public NewMain(Path inputDirectory, Path outputDirectory,
							Optional<Set<String>> permittedMissingExtensionPrefixes,
							Optional<String> repositoryName,
							Optional<String> repositoryBranch,
							Optional<String> httpCacheDirectory,
							AbstractRendererEventHandler errorHandler) {
		this(inputDirectory, outputDirectory, Optional.empty(),permittedMissingExtensionPrefixes,
				repositoryName, repositoryBranch, httpCacheDirectory,
				errorHandler, Optional.empty());
>>>>>>> feature/git-history-view:src/main/java/uk/nhs/fhir/render/NewMain.java
	}
	
	public FhirProfileRenderer(RendererCliArgs args) {
		this(
			args.getInputDir(),
			args.getOutputDir(),
			args.getNewBaseUrl(),
			args.getAllowedMissingExtensionPrefixes(),
			args.getRepositoryName(),
			args.getRepositoryBranch(),
			args.getHttpCacheDirectory(),
			new RendererLoggingEventHandler(),
			args.getLocalDomains());
	}
    
	public FhirProfileRenderer(
		Path inputDirectory, 
		Path outPath, 
		Optional<String> newBaseURL, 
		Optional<Set<String>> permittedMissingExtensionPrefixes,
		Optional<String> repositoryName,
		Optional<String> repositoryBranch,
		Optional<String> httpCacheDirectory,
		AbstractRendererEventHandler errorHandler, 
		Optional<Set<String>> localQdomains) 
	{
		this.rendererFileLocator = new DefaultRendererFileLocator(inputDirectory, makeRenderedArtefactTempDirectory(), outPath);
		this.newBaseURL = newBaseURL;
		this.permittedMissingExtensionPrefixes = permittedMissingExtensionPrefixes.orElse(Sets.newHashSet());
		this.eventHandler = errorHandler;
		this.localQdomains = localQdomains.map(qdomains -> (Set<String>)ImmutableSet.copyOf(qdomains));
		this.repositoryName = repositoryName;
		this.repositoryBranch = repositoryBranch;
		this.httpCacheDirectory = httpCacheDirectory;
	}
<<<<<<< HEAD:profile-renderer-lib/src/main/java/uk/nhs/fhir/render/FhirProfileRenderer.java
=======

	/**
     * Main entry point.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	RendererCliArgs cliArgs = new RendererCliArgsParser().parseArgs(args);
    	if (cliArgs == null) {
    		return;
    	}
    	
    	NewMain instance = new NewMain(cliArgs.getInputDir(), cliArgs.getOutputDir(), cliArgs.getNewBaseUrl(),
    			cliArgs.getAllowedMissingExtensionPrefixes(),
    			cliArgs.getRepositoryName(), cliArgs.getRepositoryBranch(), cliArgs.getHttpCacheDirectory(),
    			new RendererLoggingEventHandler(), cliArgs.getLocalDomains());
        instance.process();
    }

>>>>>>> feature/git-history-view:src/main/java/uk/nhs/fhir/render/NewMain.java
	private static Path makeRenderedArtefactTempDirectory() {
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

        if (newBaseURL.isPresent()) {
        	LOG.info("Using new base URL: " + newBaseURL.get());
        }
    	
    	Path rawArtefactDirectory = rendererFileLocator.getRawArtefactDirectory();
    	LOG.info("Finding resources in " + rawArtefactDirectory.toString());

    	// ensure that local URLs are correctly configured
    	Set<String> originalQDomains = FhirURL.getLocalQDomains();
    	if (localQdomains.isPresent()) {
    		FhirURL.setLocalQDomains(localQdomains.get());
    	}
    	final Set<String> oldPermittedMissingExtensionPrefixes = RendererContext.forThread().getPermittedMissingExtensionPrefixes();
		RendererContext.forThread().setPermittedMissingExtensionPrefixes(permittedMissingExtensionPrefixes);
    	
    	try {
	    	FhirFileRegistry fhirFileRegistry = new FhirFileRegistry();
			RendererContext rendererContext = RendererContext.forThread();
			rendererContext.setFhirFileRegistry(fhirFileRegistry);
			EventHandler oldEventHandler = EventHandlerContext.forThread();
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
							String filename = rendererContext.getCurrentSource().getAbsolutePath().substring(rawArtefactDirectory.toString().length());
	        				fileProcessor.processFile(rendererFileLocator,
	        							repositoryName, repositoryBranch, httpCacheDirectory, filename, newBaseURL);
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
	        	
	        	if (eventHandler.isDeferred() 
	        	  && (!succeeded || eventHandler.foundWarnings())) {
	        		LOG.info("Displaying event messages");
	        		
	        		eventHandler.displayOutstandingEvents();
	        	}
	    		
	        	if (succeeded || allowCopyOnError) {
	        		if (succeeded) {
	        			LOG.info("Rendering succeeded, copying rendered artefacts");
	        		} else if (allowCopyOnError) {
	        			LOG.info("Rendering failed for some files - copying rendered artefacts anyway since allowCopyOnError set");
	        		}
	        		
	        		copyExamples(fhirFileRegistry);
	        		copyGeneratedArtefacts();
	        	} else {
	        		LOG.warn("At least one error was encountered and allowCopyOnError is not set - not copying rendered resources");
	        	}
	        	
	        	// if an error is thrown while copying the files, this gets skipped so they can be recovered if necessary
	        	LOG.info("Deleting temporary files");
	        	deleteTempFiles();
	        	
	        } catch (Exception e) {
	        	throw new IllegalStateException("Renderer failed", e);
	        } finally {
	        	// reinstate the old event handler so we don't lose logging etc.
	        	EventHandlerContext.setForThread(oldEventHandler);
	        	RendererContext.forThread().setPermittedMissingExtensionPrefixes(oldPermittedMissingExtensionPrefixes);
	        }
	        
	        
	        if (FullFhirURL.TEST_LINK_URLS) {
	        	new UrlValidator().testUrls(FhirURL.getLinkUrls());
	            UrlValidator.logSuccessAndFailures();
	        }
        
    	} finally {
            // restore previous state
            FhirURL.setLocalQDomains(originalQDomains);
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
