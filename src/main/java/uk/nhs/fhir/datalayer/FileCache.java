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
package uk.nhs.fhir.datalayer;

import static uk.nhs.fhir.datalayer.DataLoaderMessages.addMessage;
import static uk.nhs.fhir.enums.ResourceType.EXAMPLES;
import static uk.nhs.fhir.enums.ResourceType.IMPLEMENTATIONGUIDE;
import static uk.nhs.fhir.enums.ResourceType.OPERATIONDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.STRUCTUREDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.VALUESET;
import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact.OrderByWeight;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ArtefactType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PropertyReader;

/**
 * Holds an in-memory cache of metadata about FHIR resources loaded from the filesystem.
 *
 * @author Adam Hatherly
 */
public class FileCache {
    private static final Logger LOG = Logger.getLogger(FileCache.class.getName());

    // Singleton object to act as a cache of the files in the profiles and valueset directories
    private static List<ResourceEntityWithMultipleVersions> resourceList = null;
    private static HashMap<String, ExampleResources> examplesList = null;
    private static HashMap<String, ResourceEntity> examplesListByName = null;
    
    private static long lastUpdated = 0;
    private static long updateInterval = Long.parseLong(PropertyReader.getProperty("cacheReloadIntervalMS"));
    private static String fileExtension = PropertyReader.getProperty("fileExtension");


    /**
     * Method to get the cached set of Resource names of the specified type
     * 
     * @return 
     */
    public static List<String> getResourceNameList(ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
        HashSet<String> names = new HashSet<String>();
        for(ResourceEntityWithMultipleVersions entry : resourceList) {
        	if (entry.getLatest().getResourceType() == resourceType)
        		if (!names.contains(entry.getLatest().getResourceName()))
        			names.add(entry.getLatest().getResourceName());
        }
        ArrayList<String> nameList = new ArrayList(names);
        return nameList;
    }
    
    /**
     * Get a list of all extensions to show in the extensions registry
     * @return
     */
    public static List<ResourceEntity> getExtensions()  {
    	List<ResourceEntity> results = new ArrayList<ResourceEntity>();
    	if(updateRequired()) {
            updateCache();
        }
		for(ResourceEntityWithMultipleVersions entry : resourceList) {
        	if (entry.getLatest().isExtension())
        		results.add(entry.getLatest());
        }
		Collections.sort(results);
        return results;
	}
    
    
    /**
     * Return resource list grouped into sensible groups
     * @param resourceType
     * @return
     */
    public static HashMap<String, List<ResourceEntity>> getGroupedNameList(ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
        
        LOG.info("Creating HashMap");
        HashMap<String, List<ResourceEntity>> result = new HashMap<String, List<ResourceEntity>>();
        try {
            for(ResourceEntityWithMultipleVersions entry : resourceList) {
            	if (entry.getLatest().getResourceType() == resourceType) {
	            	boolean isExtension = entry.getLatest().isExtension();
	                String group = entry.getLatest().getDisplayGroup();
	                
	                // Don't include extensions
	                if (!isExtension) {
		                if(result.containsKey(group)) {
		                    List<ResourceEntity> resultEntry = result.get(group);
		                    resultEntry.add(entry.getLatest());
		                } else {
		                    List<ResourceEntity> resultEntry = new ArrayList<ResourceEntity>();
		                    resultEntry.add(entry.getLatest());
		                    result.put(group, resultEntry);
		                }
	                }
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("ERROR: " + e.getMessage());
        }
        LOG.info("Generated HashMap: " + result.toString());
        return result;
    }
    
    
    /**
     * Method to get all of the resources of this type (latest versions)
     * 
     * @return 
     */
    public static List<IBaseResource> getResources(ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
        // Load each resource file and put them in a list to return
        ArrayList<IBaseResource> allFiles = new ArrayList<IBaseResource>();
        for (ResourceEntityWithMultipleVersions entry : resourceList) {
        	if (entry.getLatest().getResourceType() == resourceType) {
        		IBaseResource vs = FHIRUtils.loadResourceFromFile(entry.getLatest().getResourceFile());
        		allFiles.add(vs);
        	}
        }
        return allFiles;
    }
    
    private static boolean updateRequired() {
        long currentTime = System.currentTimeMillis();
        if(resourceList == null || (currentTime > (lastUpdated + updateInterval))) {
            LOG.fine("Cache needs updating");
            return true;
        }
        LOG.info("Using Cache");
        return false;
    }
    
    private synchronized static void updateCache() {
        if(updateRequired()) {
        	DataLoaderMessages.clearProfileLoadMessages();
            lastUpdated = System.currentTimeMillis();
            LOG.fine("Updating cache from filesystem");
            
            // Load StructureDefinitions
            ArrayList<ResourceEntityWithMultipleVersions> newList = cacheFHIRResources(STRUCTUREDEFINITION);
            
            // Add ValueSets
            newList.addAll(cacheFHIRResources(VALUESET));
            
            // Add operations
            newList.addAll(cacheFHIRResources(OPERATIONDEFINITION));

            // Add ImplementationGuides
            newList.addAll(cacheFHIRResources(IMPLEMENTATIONGUIDE));
            
            // Add examples
            HashMap<String, ExampleResources> newExamplesList = cacheExamples();
            
            // Swap out for our new list
            resourceList = newList;
            
            // And also for examples, but in this case also build a second list keyed on name for faster retrieval
            examplesList = newExamplesList;
            examplesListByName = buildExampleListByName(newExamplesList);
            
            printCacheContent();
        }
    }
    
    /**
     * Takes the in-memory index used for finding examples for specific profile IDs and flips it to
     * return an index keyed on the example filename
     * @param oldList
     * @return
     */
    private static HashMap<String, ResourceEntity> buildExampleListByName(HashMap<String, ExampleResources> oldList) {
    	HashMap<String, ResourceEntity> newList = new HashMap<String, ResourceEntity>();
    	for (String key : oldList.keySet()) {
    		ExampleResources examples = oldList.get(key);
    		for (ResourceEntity example : examples) {
    			newList.put(example.getResourceName(), example);
    		}
    	}
    	return newList;
    }
    
    private static ArrayList<ResourceEntityWithMultipleVersions> cacheFHIRResources(ResourceType resourceType){
    	
    	// Call pre-processor to copy files into the versioned directory
    	try {
    		VersionedFilePreprocessor.copyFHIRResourcesIntoVersionedDirectory(resourceType);
    	} catch (IOException e) {
    		LOG.severe("Unable to pre-process files into versioned directory! - error: " + e.getMessage());
    	}
    	
    	LOG.info("Started loading resources into cache");
		addMessage("Started loading " + resourceType + " resources into cache");
    	
        // Now, read the resources from the versioned path into our cache
    	ArrayList<ResourceEntityWithMultipleVersions> newFileList = new ArrayList<ResourceEntityWithMultipleVersions>();
        String path = resourceType.getVersionedFilesystemPath();
        File folder = new File(path);
            File[] fileList = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(fileExtension);
                }
            });
        
        if (fileList != null) {
	        for (File thisFile : fileList) {
	            if (thisFile.isFile()) {
	                LOG.fine("Reading " + resourceType + " ResourceEntity into cache: " + thisFile.getName());
	                
	                String name = null;
	                String resourceID = null;
	                boolean extension = false;
	                String baseType = "Other";
	                String displayGroup = null;
	                boolean example = false;
	                VersionNumber versionNo = null;
	                String status = null;
	                String url = null;
	                String extensionCardinality = null;
	                ArrayList<String> extensionContexts = null;
	                String extensionDescription = null;
	                
	                try {
		                if (resourceType == STRUCTUREDEFINITION) {
		                	StructureDefinition profile = (StructureDefinition)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = profile.getName();
		                	extension = (profile.getBase().equals("http://hl7.org/fhir/StructureDefinition/Extension"));
		                    
		                	if (!extension) {
		                		baseType = profile.getConstrainedType();
		                	} else {
		                		// Extra metadata for extensions
		                		int min = profile.getSnapshot().getElementFirstRep().getMin();
		                		String max = profile.getSnapshot().getElementFirstRep().getMax();
		                		extensionCardinality = min + ".." + max;
		                		
		                		extensionContexts = new ArrayList<String>();
		                		List<StringDt> contextList = profile.getContext();
		                		for (StringDt context : contextList) {
		                			extensionContexts.add(context.getValueAsString());
		                		}
		                		
		                		extensionDescription = profile.getDifferential().getElementFirstRep().getShort();
		                		
		                		List<ElementDefinitionDt> diffElements = profile.getDifferential().getElement();
		                		boolean isSimple = false;
		                		if (diffElements.size() == 3) {
		                			if (diffElements.get(1).getPath().equals("Extension.url")) {
		                				isSimple = true;
		                				// It is a simple extension, so we can also find a type
		                				List<Type> typeList = diffElements.get(2).getType();
		                				if (typeList.size() == 1) {
		                					baseType = typeList.get(0).getCode();
		                				} else {
		                					baseType = "(choice)";
		                				}
		                			}
		                		}
		                		if (!isSimple) {
		                			baseType = "(complex)";
		                		}
		                	
		                	}
		                    url = profile.getUrl();
		                    resourceID = getResourceIDFromURL(url, name);
		                    displayGroup = baseType;
		                    versionNo = new VersionNumber(profile.getVersion());
		                    status = profile.getStatus();
		                } else if (resourceType == VALUESET) {
		                	displayGroup = "Code List";
		                	ValueSet profile = (ValueSet)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = profile.getName();
		                	url = profile.getUrl();
		                	resourceID = getResourceIDFromURL(url, name);
		                	if (FHIRUtils.isValueSetSNOMED(profile)) {
		                		displayGroup = "SNOMED CT Code List";
		                	}
		                	versionNo = new VersionNumber(profile.getVersion());
		                	status = profile.getStatus();
		                } else if (resourceType == OPERATIONDEFINITION) {
		                	OperationDefinition operation = (OperationDefinition)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = operation.getName();
		                	url = operation.getUrl();
		                    resourceID = getResourceIDFromURL(url, name);
		                    displayGroup = "Operations";
		                    versionNo = new VersionNumber(operation.getVersion());
		                    status = operation.getStatus();
		                } else if (resourceType == IMPLEMENTATIONGUIDE) {
		                	ImplementationGuide guide = (ImplementationGuide)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = guide.getName();
		                	url = guide.getUrl();
		                    resourceID = getResourceIDFromURL(url, name);
		                    displayGroup = "Implementation Guides";
		                    versionNo = new VersionNumber(guide.getVersion());
		                    status = guide.getStatus();
		                }
		                
	                	// Load into the main cache for profiles
		                ArrayList<SupportingArtefact> artefacts = processSupportingArtefacts(thisFile, resourceType);
		                // Sort artefacts by weight so they display in the correct order
		                Collections.sort(artefacts, new OrderByWeight());
		                
		                ResourceEntity newEntity = new ResourceEntity(name, thisFile, resourceType, extension, baseType,
								displayGroup, example, resourceID, versionNo, status, artefacts, extensionCardinality,
								extensionContexts, extensionDescription);
		                
		                addToResourceList(newFileList,newEntity);
		                
		                addMessage("  - Loading " + resourceType + " resource with ID: " + resourceID + " and version: " + versionNo);

	                } catch (Exception ex) {
	                	LOG.severe("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
	                	addMessage("[!] Error loading " + resourceType + " resource from file : " + thisFile.getAbsolutePath() + " message: " + ex.getMessage());
	                }
	            }
	        }
        }
        
        // Sort our collection into alpha order by resource name
        //Collections.sort(newFileList);
        LOG.fine("Finished reading resources into cache");
        return newFileList;
    }
    
    private static HashMap<String, ExampleResources> cacheExamples(){
    	
    	LOG.info("Started loading example resources into cache");
		addMessage("Started loading example resources into cache");
    	
        // Now, read the resources into our cache
		HashMap<String, ExampleResources> examplesList = new HashMap<String, ExampleResources>();
        String path = EXAMPLES.getFilesystemPath();
        File folder = new File(path);
            File[] fileList = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(fileExtension);
                }
            });
        
        if (fileList != null) {
	        for (File thisFile : fileList) {
	            if (thisFile.isFile()) {
	                LOG.fine("Reading example ResourceEntity into cache: " + thisFile.getName());
	                
	                String resourceID = null;
	                
	                try {
	                	IResource exampleResource = (IResource)FHIRUtils.loadResourceFromFile(thisFile);
	                	IdDt id = exampleResource.getId();
	                    resourceID = id.getIdPart();
	                    
	                    // Find the profile resource ID the example relates to
	                    List<? extends IPrimitiveType<String>> profiles = exampleResource.getMeta().getProfile();
	                    if (profiles.isEmpty()) {
	                    	LOG.severe("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - no profile was specified in the example!");
                    		addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " no profile was specified in the example!");
	                    }
	                    for (IPrimitiveType<String> profile : profiles) {
	                    	String profileStr = profile.getValueAsString();
	                    	if (profileStr != null) {
	                    		if (profileStr.contains("_history")) {
	                    			LOG.severe("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - versioned profile URLs not supported!");
	                    			addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " versioned profile URLs not supported!");
	                    		} else {
	                    			String[] profileParts = profileStr.split("/");
	                    			if (profileParts.length < 3) {
	                    				LOG.severe("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - profile URL invalid: " + profileStr);
		                    			addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " - profile URL invalid: " + profileStr);
	                    			} else {
	                    				// We seem to have a valid profile - add to our cache
	            	                    String profileResourceID = profileParts[profileParts.length-2] + "/" + 
	            	                    							profileParts[profileParts.length-1];

	            	                    // Load the examples into a different in-memory cache for later look-up
	            	                    ResourceEntity newEntity = new ResourceEntity(thisFile.getName(), thisFile, EXAMPLES, false, null,
	            								null, true, resourceID, null, null, null, null, null, null);
	            		                
	            	                    if (examplesList.containsKey(profileResourceID)) {
	            	                    	examplesList.get(profileResourceID).add(newEntity);
	            	                    } else {
	            	                    	ExampleResources e = new ExampleResources();
	            	                    	e.add(newEntity);
	            	                    	examplesList.put(profileResourceID, e);
	            	                    }
	            	                    
	            		                addMessage("  - Loading example resource with ID: " + resourceID + " as an example of resource with ID: " + profileResourceID);
	                    			}
	                    		}
	                    	} else {
	                    		LOG.severe("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - no profile was specified in the example!");
	                    		addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " no profile was specified in the example!");
	                    	}
	                    }
		                
	                } catch (Exception ex) {
	                	LOG.severe("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
	                	addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " message: " + ex.getMessage());
	                }
	            }
	        }
        }
        LOG.fine("Finished reading example resources into cache");
        return examplesList;
    }
    
    private static ArrayList<SupportingArtefact> processSupportingArtefacts(File resourceFile, ResourceType resourceType) {
    	ArrayList<SupportingArtefact> artefacts = new ArrayList<SupportingArtefact>();
		
		String resourceFilename = FileLoader.removeFileExtension(resourceFile.getName());
		File dir = new File(resourceFile.getParent());
		File artefactDir = new File(dir.getAbsolutePath() + "/" + resourceFilename);
		LOG.info("Lookging for artefacts in directory:" + artefactDir.getAbsolutePath());
		
		if(artefactDir.exists() && artefactDir.isDirectory()) { 
			// Now, loop through and find any artefact files
            File[] fileList = artefactDir.listFiles();
            if (fileList != null) {
    	        for (File thisFile : fileList) {
    	        	// Add this to our list of artefacts (if we can identify what it is!
    	        	ArtefactType type = ArtefactType.getFromFilename(resourceType, thisFile.getName());
    	        	if (type != null) {
    	        		SupportingArtefact artefact = new SupportingArtefact(thisFile, type); 
    	        		artefacts.add(artefact);
    	        	}
    	        }
            }
		}
		return artefacts;
	}
    
    
    
    private static void addToResourceList(ArrayList<ResourceEntityWithMultipleVersions> list,
    										ResourceEntity entry) {
    	boolean found = false;
    	for (ResourceEntityWithMultipleVersions listItem : list) {
    		if (listItem.getResourceID().equals(entry.getResourceID())) {
    			// This is a new version of an existing resource - add the version
    			listItem.add(entry);
    			found = true;
    			LOG.info("Added new version to resource: " + entry.getResourceID());
    		}
    	}
		if (!found) {
			// This is a new resource we haven't seen before
			ResourceEntityWithMultipleVersions newEntry = new ResourceEntityWithMultipleVersions(entry);
			list.add(newEntry);
			LOG.info("Added new resource (first version found): " + entry.getResourceID());
		}
    }
    
    public static ResourceEntity getSingleResourceByID(IdDt theId) {
        if(updateRequired()) {
            updateCache();
        }
        
    	for (ResourceEntityWithMultipleVersions entry : resourceList) {
    		if (entry.getResourceID().equals(theId.getIdPart())) {
    			if (theId.hasVersionIdPart()) {
    				// Get a specific version
    				VersionNumber version = new VersionNumber(theId.getVersionIdPart());
    				LOG.info("Getting versioned resource with ID="+theId.getIdPart() + " and version="+version);
    				return entry.getSpecificVersion(version);
    			} else {
    				// Get the latest
    				return entry.getLatest();
    			}
    		}
    	}
    	return null;
    }
    
    public static ResourceEntityWithMultipleVersions getversionsByID(IdDt theId) {
        if(updateRequired()) {
            updateCache();
        }
        
    	for (ResourceEntityWithMultipleVersions entry : resourceList) {
    		if (entry.getResourceID().equals(theId.getIdPart())) {
    			return entry;
    		}
    	}
    	return null;
    }
    
    public static ResourceEntity getSingleResourceByName(String name) {
        if(updateRequired()) {
            updateCache();
        }
    	for (ResourceEntityWithMultipleVersions entry : resourceList) {
    		if (entry.getResourceName().equals(name)) {
    			return entry.getLatest();
    		}
    	}
    	return null;
    }

	public static List<ResourceEntity> getResourceList() {
		if(updateRequired()) {
            updateCache();
        }
		
		List<ResourceEntity> latestResourcesList = new ArrayList<ResourceEntity>();
		for (ResourceEntityWithMultipleVersions item : resourceList) {
			latestResourcesList.add(item.getLatest());
		}
		return latestResourcesList;
	}
	
	public static ExampleResources getExamples(String resourceTypeAndID) {
		if(updateRequired()) {
            updateCache();
        }
		return examplesList.get(resourceTypeAndID);
	}
	
	public static ResourceEntity getExampleByName(String resourceFilename) {
		if(updateRequired()) {
            updateCache();
        }
		return examplesListByName.get(resourceFilename);
	}

    
    private static void printCacheContent() {
    	addMessage(" ===  ===  ===   Cache Contents   === === ===");
    	LOG.info("Cache loaded - entries:");
    	for (ResourceEntityWithMultipleVersions entry : resourceList) {
    		LOG.info("  -> " + entry);
    		addMessage(entry.toString());
    	}
    	
    	LOG.info("Examples:");
    	addMessage("Examples:");
    	for (String exampleOfProfile : examplesList.keySet()) {
    		ExampleResources entries = examplesList.get(exampleOfProfile);
    		addMessage("Examples for profile: " + exampleOfProfile);
    		for (ResourceEntity entry : entries) {
	    		LOG.info("  -> " + entry);
	    		addMessage(entry.toString());
    		}
    	}
    	addMessage(" ===  ===  ===  End Cache Contents === === ===");
    }
}
