package uk.nhs.fhir.datalayer;

import static uk.nhs.fhir.enums.ResourceType.IMPLEMENTATIONGUIDE;
import static uk.nhs.fhir.enums.ResourceType.OPERATIONDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.STRUCTUREDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.VALUESET;
import static uk.nhs.fhir.enums.ResourceType.EXAMPLES;
import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;
import static uk.nhs.fhir.datalayer.DataLoaderMessages.addMessage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.primitive.IdDt;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ArtefactType;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.DateUtils;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PropertyReader;

public class VersionedFilePreprocessor {
	
	private static final Logger LOG = Logger.getLogger(VersionedFilePreprocessor.class.getName());
	private static String fileExtension = PropertyReader.getProperty("fileExtension");
	

	protected static void copyFHIRResourcesIntoVersionedDirectory(FHIRVersion fhirVersion, ResourceType resourceType) throws IOException {
		
		//profileLoadMessages.clear();
		LOG.info("Starting pre-processor to convert files into versioned files prior to loading into the server for " + fhirVersion);
		addMessage("--------------------------------------------------------------------------------------");
		addMessage("Loading " + resourceType + " files from disk: " + DateUtils.printCurrentDateTime());
		
		// Check the versioned directory exists, and create it if not
		String versioned_path = resourceType.getVersionedFilesystemPath(fhirVersion);
		FileUtils.forceMkdir(new File(versioned_path));
		
		// Now, look in the root path for this resource type to see if we have any files to process
        ArrayList<ResourceEntity> newFileList = new ArrayList<ResourceEntity>();
        String path = resourceType.getFilesystemPath(fhirVersion);
        File folder = new File(path);
            File[] fileList = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(fileExtension);
                }
            });
        
        if (fileList != null) {
	        for (File thisFile : fileList) {
	            if (thisFile.isFile()) {
	                LOG.fine("Pre-processing " + resourceType + " resource from file: " + thisFile.getName());
	                
	                String name = null;
	                String resourceID = null;
	                VersionNumber versionNo = null;
	                
	                try {
	                	if (fhirVersion.equals(FHIRVersion.DSTU2)) {
			                if (resourceType == STRUCTUREDEFINITION) {
			                	StructureDefinition profile = (StructureDefinition)FHIRUtils.loadResourceFromFile(fhirVersion, thisFile);
			                    name = profile.getName();
			                	resourceID = getResourceIDFromURL(profile.getUrl(), name);
			                    versionNo = new VersionNumber(profile.getVersion());
			                } else if (resourceType == VALUESET) {
			                	ValueSet profile = (ValueSet)FHIRUtils.loadResourceFromFile(fhirVersion, thisFile);
			                	name = profile.getName();
			                	resourceID = getResourceIDFromURL(profile.getUrl(), name);
			                	versionNo = new VersionNumber(profile.getVersion());
			                } else if (resourceType == OPERATIONDEFINITION) {
			                	OperationDefinition operation = (OperationDefinition)FHIRUtils.loadResourceFromFile(fhirVersion, thisFile);
			                	name = operation.getName();
			                    resourceID = getResourceIDFromURL(operation.getUrl(), name);
			                    versionNo = new VersionNumber(operation.getVersion());
			                } else if (resourceType == IMPLEMENTATIONGUIDE) {
			                	ImplementationGuide guide = (ImplementationGuide)FHIRUtils.loadResourceFromFile(fhirVersion, thisFile);
			                	name = guide.getName();
			                    resourceID = getResourceIDFromURL(guide.getUrl(), name);
			                    versionNo = new VersionNumber(guide.getVersion());
			                }
	                	} else if (fhirVersion.equals(FHIRVersion.STU3)) {
	                		if (resourceType == STRUCTUREDEFINITION) {
	                			org.hl7.fhir.dstu3.model.StructureDefinition profile =
	                					(org.hl7.fhir.dstu3.model.StructureDefinition)FHIRUtils.loadResourceFromFile(fhirVersion, thisFile);
			                    name = profile.getName();
			                	resourceID = getResourceIDFromURL(profile.getUrl(), name);
			                    versionNo = new VersionNumber(profile.getVersion());
			                } else if (resourceType == VALUESET) {
			                	org.hl7.fhir.dstu3.model.ValueSet profile =
			                			(org.hl7.fhir.dstu3.model.ValueSet)FHIRUtils.loadResourceFromFile(fhirVersion, thisFile);
			                	name = profile.getName();
			                	resourceID = getResourceIDFromURL(profile.getUrl(), name);
			                	versionNo = new VersionNumber(profile.getVersion());
			                } else if (resourceType == OPERATIONDEFINITION) {
			                	org.hl7.fhir.dstu3.model.OperationDefinition operation =
			                			(org.hl7.fhir.dstu3.model.OperationDefinition)FHIRUtils.loadResourceFromFile(fhirVersion, thisFile);
			                	name = operation.getName();
			                    resourceID = getResourceIDFromURL(operation.getUrl(), name);
			                    versionNo = new VersionNumber(operation.getVersion());
			                } else if (resourceType == IMPLEMENTATIONGUIDE) {
			                	org.hl7.fhir.dstu3.model.ImplementationGuide guide =
			                			(org.hl7.fhir.dstu3.model.ImplementationGuide)FHIRUtils.loadResourceFromFile(fhirVersion, thisFile);
			                	name = guide.getName();
			                    resourceID = getResourceIDFromURL(guide.getUrl(), name);
			                    versionNo = new VersionNumber(guide.getVersion());
			                }
	                	}
	                } catch (Exception ex) {
	                	LOG.severe("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " error: " + ex.getMessage() + " - IGNORING");
	                	ex.printStackTrace();
	                }
	                
	                if (versionNo == null) {
	                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - Version number was missing or invalid");
	                	LOG.severe("Unable to process file as it is has an invalid version: " + thisFile.getName());
	                } else if (!versionNo.isValid()) {
	                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - Version number was missing or invalid");
	                	LOG.severe("Unable to process file as it is has an invalid version: " + thisFile.getName());
	                } else if (resourceID == null) {
	                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - No resource ID provided in the URL");
	                	LOG.severe("Unable to process file as it is has an invalid resource ID: " + thisFile.getName());
	                } else {
		                // Now, try to build a new versioned filename and copy the file to it
                    	String newFilename = resourceID + "-versioned-" + versionNo + ".xml";
                    	
                    	LOG.info("Copying new profile into versioned directory with new filename: " + newFilename);
                    	addMessage("  - Copying new " + resourceType + " into versioned directory with new filename: " + newFilename);
                    	File newFile = new File(versioned_path + "/" + newFilename);
                    	FileUtils.copyFile(thisFile, newFile);
                    	
                    	// And also copy other resources (diffs, details, bindings, etc).
                    	copyOtherResources(thisFile, newFile);
	                }
	            }
	        }
        }
        
        addMessage("Finished pre-processing " + resourceType + " files from disk.");
        addMessage("--------------------------------------------------------------------------------------");
    }
	
	/**
	 * If the FHIR resoutce also has other generated resources (e.g. details view, diff, bindings, etc.) then also
	 * copy those into the relevant versioned directory along with our resource
	 * 
	 * @param oldFile Original filename
	 * @param newFile New filename of resource
	 */
	protected static void copyOtherResources(File oldFile, File newFile) {
		
		String oldDir = oldFile.getParent();
		String oldName = FileLoader.removeFileExtension(oldFile.getName());
		File resourceDir = new File(oldDir + "/" + oldName);
		//System.out.println(resourceDir.getAbsolutePath());
		
		String newDir = newFile.getParent();
		String newName = FileLoader.removeFileExtension(newFile.getName());
		File targetDir = new File(newDir + "/" + newName);
		//System.out.println(targetDir.getAbsolutePath());
		
		if(resourceDir.exists() && resourceDir.isDirectory()) { 
			try {
				// Create target dir
				FileUtils.forceMkdir(targetDir);
				
				// Now, loop through and copy any files into the target directory
	            File[] fileList = resourceDir.listFiles();
	            if (fileList != null) {
	    	        for (File thisFile : fileList) {
	    	        	FileUtils.copyFileToDirectory(thisFile, targetDir);
	    	        }
	            }
			} catch (IOException e) {
				LOG.severe("Unable to copy supporting resources!");
				e.printStackTrace();
			}
		}
	}

}
