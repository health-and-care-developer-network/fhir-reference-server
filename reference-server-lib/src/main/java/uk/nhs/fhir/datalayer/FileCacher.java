package uk.nhs.fhir.datalayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.load.FhirFileParser;
import uk.nhs.fhir.load.FhirParsingFailedException;
import uk.nhs.fhir.load.XmlFileFinder;
import uk.nhs.fhir.util.AbstractFhirFileLocator;
import uk.nhs.fhir.util.FhirVersion;

/**
 * Takes a snapshot of the metadata for the currently available resources in the directory indicated by its resourceFileFinder.
 */
public class FileCacher {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileCacher.class);

    private static final XmlFileFinder resourceFileFinder = new XmlFileFinder();
    private static final FhirFileParser parser = new FhirFileParser();

	private final VersionedFilePreprocessor preprocessor;
	private AbstractFhirFileLocator fhirFileLocator;

	private final Map<FhirVersion, List<ResourceEntityWithMultipleVersions>> resourceListByFhirVersion = Maps.newConcurrentMap();
    private final Map<FhirVersion, Map<String, List<ResourceMetadata>>> examplesListByFhirVersion = Maps.newConcurrentMap();
    private final Map<FhirVersion, Map<String, ResourceMetadata>> examplesListByName = Maps.newConcurrentMap();
	
	public FileCacher(AbstractFhirFileLocator fhirFileLocator) {
		this.preprocessor = new VersionedFilePreprocessor(fhirFileLocator);
		this.fhirFileLocator = fhirFileLocator;
	}

	public Map<FhirVersion, List<ResourceEntityWithMultipleVersions>> getResourceListByFhirVersion() {
		return resourceListByFhirVersion;
	}

	public Map<FhirVersion, Map<String, List<ResourceMetadata>>> getExamplesListByFhirVersion() {
		return examplesListByFhirVersion;
	}

	public Map<FhirVersion, Map<String, ResourceMetadata>> getExamplesListByName() {
		return examplesListByName;
	}

	public void snapshotResourceMetadata() {
    	for (FhirVersion fhirVersion : FhirVersion.getSupportedVersions()) {
            cacheResources(fhirVersion);
            
        	copyExamplesToImportedFolder(fhirVersion);
            cacheExamples(fhirVersion);
    	}
	}
    
    /**
     * Takes the in-memory index used for finding examples for specific profile IDs and flips it to
     * return an index keyed on the example filename
     * @param oldList
     * @return
     */
    private Map<String, ResourceMetadata> buildExampleListByName(Map<String, List<ResourceMetadata>> oldList) {
    	Map<String, ResourceMetadata> resourceMap = Maps.newConcurrentMap();
    	
    	for (List<ResourceMetadata> entry : oldList.values()) {
    		for (ResourceMetadata resource : entry) {
    			if (resourceMap.containsKey(resource.getResourceName())) {
    				LOG.error("Multiple example resources have name \"" + resource.getResourceName() + "\": " + resource.getResourceID() + " and " + resourceMap.get(resource.getResourceName()).getResourceID());
    				LOG.error("Leaving out resource " + resource.getResourceID() + " from loaded examples (if it is also a supported resource, it should still appear)");
    			} else {
    				resourceMap.put(resource.getResourceName(), resource);
    			}
    		}
    	}
    	
    	return resourceMap;
    }

	private void cacheResources(FhirVersion fhirVersion) {
		List<ResourceEntityWithMultipleVersions> newList = Lists.newArrayList();
		
        for (ResourceType resourceType : ResourceType.typesForFhirVersion(fhirVersion)) {
        	newList.addAll(cacheFHIRResources(fhirVersion, resourceType));
        }
        
        resourceListByFhirVersion.put(fhirVersion, newList);
	}
    
    private List<ResourceEntityWithMultipleVersions> cacheFHIRResources(FhirVersion fhirVersion, ResourceType resourceType) {
    	
    	// Call pre-processor to copy files into the versioned directory
    	try {
    		preprocessor.copyFHIRResourcesIntoVersionedDirectory(fhirVersion, resourceType);
    	} catch (IOException e) {
    		LOG.error("Unable to pre-process files into versioned directory! - error: " + e.getMessage());
    	}
    	
    	LOG.debug("Started loading resources into cache");
    	DataLoaderMessages.addMessage("Started loading " + resourceType + " resources into cache");
    	
        // Now, read the resources from the versioned path into our cache
    	List<ResourceEntityWithMultipleVersions> newFileList = Lists.newArrayList();
    	String sourcePathForResourceAndVersion = fhirFileLocator.getDestinationPathForResourceType(resourceType, fhirVersion).toString();
    	LOG.debug("Reading pre-processed files from path: " + sourcePathForResourceAndVersion);
        List<File> fileList = resourceFileFinder.findFiles(sourcePathForResourceAndVersion);
        
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.debug("Reading " + resourceType + " ResourceEntity into cache: " + thisFile.getName());
                
                try {
                	IBaseResource parsedFile = parser.parseFile(thisFile);
                	if (FhirFileParser.isSupported(parsedFile)) {
						WrappedResource<?> wrappedResource = WrappedResource.fromBaseResource(parsedFile);
	                	
	                	if (wrappedResource.getImplicitFhirVersion().equals(fhirVersion)) {
	    					ResourceMetadata newEntity = wrappedResource.getMetadata(thisFile);
	    	                
	    	                addToResourceList(newFileList,newEntity);
	    	                
	    	                DataLoaderMessages.addMessage("  - Loading " + resourceType + " resource with ID: " + newEntity.getResourceID() + " and version: " + newEntity.getVersionNo());
	                	}
                	}

                } catch (Exception ex) {
                	LOG.error("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
                	DataLoaderMessages.addMessage("[!] Error loading " + resourceType + " resource from file : " + thisFile.getAbsolutePath() + " message: " + ex.getMessage());
                	ex.printStackTrace();
                }
            }
        }
        
        // Sort our collection into alpha order by resource name
        //Collections.sort(newFileList);
        LOG.debug("Finished reading resources into cache");
        return newFileList;
    }
    
    private void addToResourceList(List<ResourceEntityWithMultipleVersions> list,
    										ResourceMetadata entry) {
    	boolean found = false;
    	for (ResourceEntityWithMultipleVersions listItem : list) {
    		if (listItem.getResourceID().equals(entry.getResourceID())
    		  && listItem.getResourceType().equals(entry.getResourceType())) {
    			// This is a new version of an existing resource - add the version
    			listItem.add(entry);
    			found = true;
    			LOG.debug("Added new version to resource: " + entry.getResourceID());
    			break;
    		}
    	}
		if (!found) {
			// This is a new resource we haven't seen before
			ResourceEntityWithMultipleVersions newEntry = new ResourceEntityWithMultipleVersions(entry);
			list.add(newEntry);
			LOG.debug("Added new resource (first version found): " + entry.getResourceID());
		}
    }
    
    private void cacheExamples(FhirVersion fhirVersion) {
    	LOG.debug("Started loading example resources into cache");
    	DataLoaderMessages.addMessage("Started loading example resources into cache");
    	
        // Now, read the resources into our cache
		HashMap<String, List<ResourceMetadata>> examplesList = Maps.newHashMap();
		// We want resources that qualify as full implementations AND examples to be included in both places, so start from the source root.
        //String path = fhirFileLocator.getDestinationPathForResourceType(EXAMPLES, fhirVersion).toString();
		Path rootPath = fhirFileLocator.getSourceRoot(fhirVersion);
		List<File> fileList = rootPath.toFile().isDirectory() ?
			resourceFileFinder.findFilesRecursively(rootPath) :
			Lists.newArrayList();
		
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.debug("Reading example ResourceEntity into cache: " + thisFile.getName());
                
                String resourceID = null;
                
                try {
                	IBaseResource exampleResource = parser.parseFile(thisFile);
                	
                	// A supported file is any class we treat as part of a specification (StructureDefinition, ValueSet etc)
                	// Anything else we have successfully parsed, can be treated as an example
                	//if (!FhirFileParser.isSupported(exampleResource)) {
                		resourceID = exampleResource.getIdElement().getIdPart();
                    
	                    // Find the profile resource ID the example relates to
	                    List<? extends IPrimitiveType<String>> profiles = exampleResource.getMeta().getProfile();
	                    if (profiles.isEmpty()
	                      && thisFile.getAbsolutePath().contains("/" + ResourceType.EXAMPLES.getFolderName() + "/")) {
	                    	LOG.error("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - no profile was specified in the example!");
	                    	DataLoaderMessages.addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " no profile was specified in the example!");
	                    }
	                    for (IPrimitiveType<String> profile : profiles) {
	                    	String profileStr = profile.getValueAsString();
	                    	if (profileStr != null) {
	                    		if (profileStr.contains("_history")) {
	                    			LOG.error("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - versioned profile URLs not supported!");
	                    			DataLoaderMessages.addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " versioned profile URLs not supported!");
	                    		} else {
	                    			String[] profileParts = profileStr.split("/");
	                    			if (profileParts.length < 3) {
	                    				LOG.error("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - profile URL invalid: " + profileStr);
	                    				DataLoaderMessages.addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " - profile URL invalid: " + profileStr);
	                    			} else {
	                    				// We seem to have a valid profile - add to our cache
	            	                    String profileResourceID = profileParts[profileParts.length-2] + "/" + 
	            	                    							profileParts[profileParts.length-1];
	
	            	                    // Load the examples into a different in-memory cache for later look-up
	            	                    ResourceMetadata newEntity = new ResourceMetadata(thisFile.getName(), thisFile, ResourceType.EXAMPLES, false, Optional.empty(),
	            								null, true, resourceID, null, null, null, null, null, null, fhirVersion, null);
	            		                
	            	                    if (examplesList.containsKey(profileResourceID)) {
	            	                    	examplesList.get(profileResourceID).add(newEntity);
	            	                    } else {
	            	                    	List<ResourceMetadata> e = Lists.newArrayList();
	            	                    	e.add(newEntity);
	            	                    	examplesList.put(profileResourceID, e);
	            	                    }
	            	                    
	            	                    DataLoaderMessages.addMessage("  - Loading example resource with ID: " + resourceID + " as an example of resource with ID: " + profileResourceID);
	                    			}
	                    		}
	                    	} else {
	                    		LOG.warn("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - no profile was specified in the example!");
	                    		DataLoaderMessages.addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " no profile was specified in the example!");
	                    	}
	                    }
                	//}
	                
                } catch (FhirParsingFailedException | RuntimeException ex) {
                	LOG.error("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
                	DataLoaderMessages.addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " message: " + ex.getMessage());
                }
            }
        }
        LOG.debug("Finished reading example resources into cache");

        examplesListByFhirVersion.put(fhirVersion, examplesList);
        examplesListByName.put(fhirVersion, buildExampleListByName(examplesList));
    }

	void copyExamplesToImportedFolder(FhirVersion fhirVersion) {
		// Call pre-processor to copy files into the versioned directory
        Path renderedExamplesPath = fhirFileLocator.getSourcePathForResourceType(ResourceType.EXAMPLES, fhirVersion);
        Path importedExamplesPath = fhirFileLocator.getDestinationPathForResourceType(ResourceType.EXAMPLES, fhirVersion);
        
        File importedExamplesPathFile = importedExamplesPath.toFile();
		if (!importedExamplesPathFile.exists() && !importedExamplesPathFile.mkdirs()) {
        	throw new IllegalStateException("Failed to create directory [" + importedExamplesPath.toString() + "]");
        }
        
		List<File> renderedFiles = resourceFileFinder.findFiles(renderedExamplesPath.toFile());
        
        for (File renderedFile : renderedFiles) {
        	String fileName = renderedFile.getName();
			try {
				FileUtils.copyFile(renderedFile, importedExamplesPath.resolve(fileName).toFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}

}
